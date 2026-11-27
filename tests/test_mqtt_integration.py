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


ROOT = Path(__file__).resolve().parents[1]


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
            client.subscribe("simulator/vehicles/#")
            connected.set()

    def on_message(client, userdata, message):
        messages.put((time.monotonic(), message.topic, json.loads(message.payload)))

    subscriber = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
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
        time.sleep(4)
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
    return timestamps, vehicle_ids


def test_configured_rate_and_fleet_size_apply_after_restart(mosquitto):
    first_run, first_fleet = collect_run(10, mosquitto)
    second_run, second_fleet = collect_run(20, mosquitto)

    assert len(first_run) == pytest.approx(32, rel=0.25)
    assert first_fleet == {f"SIM-{index:04d}" for index in range(1, 11)}
    assert len(second_run) == pytest.approx(32, rel=0.25)
    assert second_fleet == {f"SIM-{index:04d}" for index in range(1, 21)}
