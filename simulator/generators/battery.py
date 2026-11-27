import math
import random

from simulator.generators.base import TelemetryGenerator


class BatteryGenerator(TelemetryGenerator):
    event_type = "battery"
    INITIAL_MIN_PERCENT = 50.0
    INITIAL_MAX_PERCENT = 100.0

    def __init__(
        self,
        publisher,
        seed=None,
        speed_meters_per_second=4.0,
        drain_percent_per_kilometer=2.0,
        recharge_percent_per_second=0.02,
    ):
        super().__init__(publisher)
        rates = (
            speed_meters_per_second,
            drain_percent_per_kilometer,
            recharge_percent_per_second,
        )
        if any(not math.isfinite(rate) or rate <= 0 for rate in rates):
            raise ValueError("Battery rates must be positive finite numbers")
        self.random = random.Random(seed)
        self.speed_meters_per_second = speed_meters_per_second
        self.drain_percent_per_kilometer = drain_percent_per_kilometer
        self.recharge_percent_per_second = recharge_percent_per_second
        self.levels = {}

    def tick(self, vehicle, elapsed_seconds=1.0, context=None):
        if not math.isfinite(elapsed_seconds) or elapsed_seconds <= 0:
            raise ValueError("Elapsed time must be a positive finite number")
        if context is None or "gps" not in context:
            raise ValueError("Battery generation requires the latest GPS state")

        if vehicle.serial not in self.levels:
            self.levels[vehicle.serial] = self.random.uniform(
                self.INITIAL_MIN_PERCENT,
                self.INITIAL_MAX_PERCENT,
            )
        level = self.levels[vehicle.serial]
        gps_data = context["gps"]
        if gps_data["moving"]:
            distance_kilometers = (
                self.speed_meters_per_second * elapsed_seconds / 1000
            )
            level -= self.drain_percent_per_kilometer * distance_kilometers
        elif gps_data["hub_id"] is not None:
            level += self.recharge_percent_per_second * elapsed_seconds

        level = min(100.0, max(0.0, level))
        self.levels[vehicle.serial] = level
        data = {"battery_level": level}
        self._publish(vehicle, data)
        return data
