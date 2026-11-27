import logging
import os
import signal
import threading
import time

import paho.mqtt.client as mqtt

from simulator.config import SimulatorConfig
from simulator.publisher import TelemetryPublisher
from simulator.registry import load_vehicles

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def run(config, broker_host, broker_port, vehicles, mqtt_username, mqtt_password):
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.enable_logger(logger)
    publisher = TelemetryPublisher(
        client,
        broker_host,
        broker_port,
        username=mqtt_username,
        password=mqtt_password,
    )
    publisher.connect()

    stopped = threading.Event()
    signal.signal(signal.SIGTERM, lambda *_: stopped.set())
    signal.signal(signal.SIGINT, lambda *_: stopped.set())

    interval = 1 / config.publish_rate
    next_publish = time.monotonic()
    vehicle_index = 0
    try:
        while not stopped.is_set():
            delay = next_publish - time.monotonic()
            if delay > 0 and stopped.wait(delay):
                break

            vehicle = vehicles[vehicle_index]
            publisher.publish(
                vehicle,
                "heartbeat",
                {},
            )
            vehicle_index = (vehicle_index + 1) % len(vehicles)
            next_publish += interval

            if next_publish < time.monotonic():
                next_publish = time.monotonic() + interval
    finally:
        publisher.close()


def main():
    try:
        config = SimulatorConfig.from_env()
        vehicles = load_vehicles(config.vehicles_file)
        if config.vehicle_count > len(vehicles):
            raise ValueError(
                f"VEHICLE_COUNT ({config.vehicle_count}) exceeds the number "
                f"of registered vehicles ({len(vehicles)})"
            )
        vehicles = vehicles[: config.vehicle_count]
        broker_host = os.getenv("MQTT_HOST", "localhost")
        broker_port = int(os.getenv("MQTT_PORT", "1883"))
        if not 1 <= broker_port <= 65535:
            raise ValueError("MQTT_PORT must be between 1 and 65535")
        mqtt_username = os.getenv("MQTT_USERNAME", "simulator")
        mqtt_password = os.getenv("MQTT_PASSWORD", "demo-simulator-password")
    except ValueError as error:
        raise SystemExit(str(error)) from error

    run(
        config,
        broker_host,
        broker_port,
        vehicles,
        mqtt_username,
        mqtt_password,
    )


if __name__ == "__main__":
    main()
