import math
import random

from simulator.generators.base import TelemetryGenerator


class FaultGenerator(TelemetryGenerator):
    event_type = "fault"
    publishes_every_tick = False
    ALLOWED_FAULT_TYPES = (
        "motor_overheat",
        "battery_failure",
        "gps_failure",
        "brake_failure",
        "lock_failure",
    )
    SEVERITIES = ("low", "medium", "high", "critical")

    def __init__(self, publisher, probability=0.01, seed=None):
        super().__init__(publisher)
        if not math.isfinite(probability) or not 0 <= probability <= 1:
            raise ValueError("Fault probability must be between 0 and 1")
        self.probability = probability
        self.random = random.Random(seed)

    def tick(self, vehicle, elapsed_seconds=1.0, context=None):
        if self.random.random() >= self.probability:
            return None

        data = {
            "fault_type": self.random.choice(self.ALLOWED_FAULT_TYPES),
            "severity": self.random.choice(self.SEVERITIES),
        }
        self._publish(vehicle, data)
        return data
