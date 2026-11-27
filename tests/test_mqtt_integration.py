import json
import os
import queue
import socket
import subprocess
import sys
import threading
import time
from pathlib import Path

import paho.mqtt.client as mqtt
import pytest
from jsonschema import Draft202012Validator, FormatChecker

from simulator.config import SimulatorConfig
from simulator.registry import load_vehicles


ROOT = Path(__file__).resolve().parents[1]
REGISTERED_SERIALS = [
    vehicle.serial for vehicle in load_vehicles(SimulatorConfig().vehicles_file)
]
SCHEMA_PATH = ROOT / "schemas" / "telemetry-event.schema.json"
SCHEMA_VALIDATOR = Draft202012Validator(
    json.loads(SCHEMA_PATH.read_text(encoding="utf-8")),
    format_checker=FormatChecker(),
)


@pytest.fixture(scope="module")
def mosquitto():
    project = f"mt165-test-{os.getpid()}"
    compose = ["docker", "compose", "-p", project, "-f", str(ROOT / "compose.yaml")]
    subprocess.run(
        [*compose, "up", "-d", "broker"],
        cwd=ROOT,
        check=True,
        stdout=subprocess.DEVNULL,
    )
    try:
        deadline = time.monotonic() + 30
        while time.monotonic() < deadline:
            try:
                with socket.create_connection(("127.0.0.1", 1883), timeout=0.2):
                    break
            except OSError:
                time.sleep(0.2)
        else:
            pytest.fail("Mosquitto did not start in time")
        yield
    finally:
        subprocess.run(
            [*compose, "down", "--volumes", "--remove-orphans"],
            cwd=ROOT,
            check=False,
            stdout=subprocess.DEVNULL,
        )


def collect_run(vehicle_count, mosquitto):
    messages = queue.Queue()
    connected = threading.Event()

    def on_connect(client, userdata, flags, reason_code, properties):
        if reason_code == 0:
            client.subscribe("telemetry/#")
            connected.set()

    def on_message(client, userdata, message):
        messages.put((time.monotonic(), message.topic, json.loads(message.payload)))

    subscriber = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    subscriber.username_pw_set("viewer", "demo-viewer-password")
    subscriber.on_connect = on_connect
    subscriber.on_message = on_message
    subscriber.connect("127.0.0.1", 1883)
    subscriber.loop_start()
    assert connected.wait(5)

    environment = os.environ.copy()
    environment.update(
        PUBLISH_RATE="8",
        VEHICLE_COUNT=str(vehicle_count),
        MQTT_HOST="127.0.0.1",
        MQTT_PORT="1883",
        MQTT_USERNAME="simulator",
        MQTT_PASSWORD="demo-simulator-password",
    )
    process = subprocess.Popen(
        [sys.executable, "-m", "simulator"],
        cwd=ROOT,
        env=environment,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.PIPE,
        text=True,
    )

    try:
        started = time.monotonic()
        time.sleep(8)
        process.terminate()
        _, stderr = process.communicate(timeout=5)
        assert process.returncode == 0, stderr
    finally:
        if process.poll() is None:
            process.kill()
            process.wait()
        subscriber.disconnect()
        subscriber.loop_stop()

    received = []
    while not messages.empty():
        received.append(messages.get_nowait())

    timestamps = [timestamp for timestamp, _, _ in received if timestamp >= started]
    vehicle_ids = {payload["vehicle_id"] for _, _, payload in received}
    event_types = {payload["event_type"] for _, _, payload in received}
    for _, topic, payload in received:
        SCHEMA_VALIDATOR.validate(payload)
        assert topic == f"telemetry/{payload['municipality']}/{payload['vehicle_id']}"
    return timestamps, vehicle_ids, event_types


def test_unauthorized_client_is_rejected(mosquitto):
    response = queue.Queue(maxsize=1)

    def on_connect(client, userdata, flags, reason_code, properties):
        response.put(reason_code == 0)

    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.username_pw_set("unauthorized", "incorrect-password")
    client.on_connect = on_connect
    client.connect("127.0.0.1", 1883)
    client.loop_start()
    try:
        assert response.get(timeout=5) is False
    finally:
        client.disconnect()
        client.loop_stop()


def test_configured_rate_and_fleet_size_apply_after_restart(mosquitto):
    first_run, first_fleet, first_types = collect_run(10, mosquitto)
    second_run, second_fleet, second_types = collect_run(20, mosquitto)

    assert len(first_run) == pytest.approx(64, rel=0.25)
    assert first_fleet == set(REGISTERED_SERIALS[:10])
    assert first_types == {"gps", "battery", "lock"}
    assert len(second_run) == pytest.approx(64, rel=0.25)
    assert second_fleet == set(REGISTERED_SERIALS[:20])
    assert second_types == {"gps", "battery", "lock"}
    assert first_fleet | second_fleet <= set(REGISTERED_SERIALS)
