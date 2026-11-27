import logging
import os
import signal
import threading
import time

import paho.mqtt.client as mqtt

from simulator.config import SimulatorConfig
from simulator.generators.battery import BatteryGenerator
from simulator.generators.gps import GPSGenerator
from simulator.generators.lock import LockGenerator
from simulator.publisher import TelemetryPublisher
from simulator.registry import load_vehicles
from simulator.routes import load_hub_locations

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def run(
    config,
    broker_host,
    broker_port,
    vehicles,
    hub_locations,
    mqtt_username,
    mqtt_password,
):
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
    gps_generator = GPSGenerator(publisher, hub_locations)
    battery_generator = BatteryGenerator(
        publisher,
        speed_meters_per_second=gps_generator.speed_meters_per_second,
    )
    lock_generator = LockGenerator(publisher)
    generators = [gps_generator, battery_generator, lock_generator]
    gps_states = {}

    stopped = threading.Event()
    signal.signal(signal.SIGTERM, lambda *_: stopped.set())
    signal.signal(signal.SIGINT, lambda *_: stopped.set())

    interval = 1 / config.publish_rate
    periodic_generators = sum(
        generator.publishes_every_tick for generator in generators
    )
    elapsed_seconds = config.vehicle_count * periodic_generators / config.publish_rate
    next_publish = time.monotonic()
    vehicle_index = 0
    generator_index = 0
    try:
        while not stopped.is_set():
            delay = next_publish - time.monotonic()
            if delay > 0 and stopped.wait(delay):
                break

            vehicle = vehicles[vehicle_index]
            generator = generators[generator_index]
            event_data = generator.tick(
                vehicle,
                elapsed_seconds=elapsed_seconds,
                context={"gps": gps_states.get(vehicle.serial)},
            )
            if generator is gps_generator:
                gps_states[vehicle.serial] = event_data
            if event_data is not None:
                next_publish += interval
                if next_publish < time.monotonic():
                    next_publish = time.monotonic() + interval
            generator_index += 1
            if generator_index == len(generators):
                generator_index = 0
                vehicle_index = (vehicle_index + 1) % len(vehicles)
    finally:
        publisher.close()


def main():
    try:
        config = SimulatorConfig.from_env()
        vehicles = load_vehicles(config.vehicles_file)
        hub_locations = load_hub_locations(config.routes_file)
        if config.vehicle_count > len(vehicles):
            raise ValueError(
                f"VEHICLE_COUNT ({config.vehicle_count}) exceeds the number "
                f"of registered vehicles ({len(vehicles)})"
            )
        vehicles = vehicles[: config.vehicle_count]
        for vehicle in vehicles:
            if (vehicle.municipality, vehicle.hub) not in hub_locations:
                raise ValueError(
                    f"No coordinates configured for hub {vehicle.hub} "
                    f"in municipality {vehicle.municipality}"
                )
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
        hub_locations,
        mqtt_username,
        mqtt_password,
    )


if __name__ == "__main__":
    main()
