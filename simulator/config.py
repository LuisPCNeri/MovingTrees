import math
import os
from dataclasses import dataclass


@dataclass(frozen=True)
class SimulatorConfig:
    publish_rate: float = 1.0
    vehicle_count: int = 10

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

        return cls(publish_rate=publish_rate, vehicle_count=vehicle_count)
