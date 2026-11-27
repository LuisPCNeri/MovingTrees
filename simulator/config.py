import math
import os
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class SimulatorConfig:
    publish_rate: float = 1.0
    vehicle_count: int = 10
    vehicles_file: str = str(Path(__file__).with_name("vehicles.json"))
    routes_file: str = str(Path(__file__).with_name("routes.json"))
    fault_probability: float = 0.01

    @classmethod
    def from_env(cls):
        rate_value = os.getenv("PUBLISH_RATE", "1")
        count_value = os.getenv("VEHICLE_COUNT", "10")

        try:
            publish_rate = float(rate_value)
        except ValueError as error:
            raise ValueError("PUBLISH_RATE must be a positive number") from error
        if not math.isfinite(publish_rate) or publish_rate <= 0:
            raise ValueError("PUBLISH_RATE must be a positive number")

        try:
            vehicle_count = int(count_value)
        except ValueError as error:
            raise ValueError("VEHICLE_COUNT must be a positive integer") from error
        if vehicle_count <= 0:
            raise ValueError("VEHICLE_COUNT must be a positive integer")

        probability_value = os.getenv("FAULT_PROBABILITY", "0.01")
        try:
            fault_probability = float(probability_value)
        except ValueError as error:
            raise ValueError(
                "FAULT_PROBABILITY must be a number between 0 and 1"
            ) from error
        if not math.isfinite(fault_probability) or not 0 <= fault_probability <= 1:
            raise ValueError("FAULT_PROBABILITY must be a number between 0 and 1")

        vehicles_file = os.getenv(
            "VEHICLES_FILE", str(Path(__file__).with_name("vehicles.json"))
        )
        routes_file = os.getenv(
            "ROUTES_FILE", str(Path(__file__).with_name("routes.json"))
        )
        return cls(
            publish_rate=publish_rate,
            vehicle_count=vehicle_count,
            vehicles_file=vehicles_file,
            routes_file=routes_file,
            fault_probability=fault_probability,
        )
