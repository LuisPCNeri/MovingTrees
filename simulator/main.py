import json
import logging
import os
import signal
import threading
import time

import paho.mqtt.client as mqtt

from simulator.config import SimulatorConfig

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def run(config, broker_host, broker_port):
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
    client.enable_logger(logger)
    client.connect(broker_host, broker_port)
    client.loop_start()

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

            vehicle_id = f"SIM-{vehicle_index + 1:04d}"
            payload = json.dumps(
                {"vehicle_id": vehicle_id, "event": "heartbeat", "timestamp": time.time()}
            )
            client.publish(f"simulator/vehicles/{vehicle_id}", payload, qos=0)
            vehicle_index = (vehicle_index + 1) % config.vehicle_count
            next_publish += interval

            if next_publish < time.monotonic():
                next_publish = time.monotonic() + interval
    finally:
        client.disconnect()
        client.loop_stop()


def main():
    try:
        config = SimulatorConfig.from_env()
        broker_host = os.getenv("MQTT_HOST", "localhost")
        broker_port = int(os.getenv("MQTT_PORT", "1883"))
        if not 1 <= broker_port <= 65535:
            raise ValueError("MQTT_PORT must be between 1 and 65535")
    except ValueError as error:
        raise SystemExit(str(error)) from error

    run(config, broker_host, broker_port)


if __name__ == "__main__":
    main()
