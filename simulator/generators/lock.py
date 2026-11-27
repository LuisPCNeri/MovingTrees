from simulator.generators.base import TelemetryGenerator


class LockGenerator(TelemetryGenerator):
    event_type = "lock"
    publishes_every_tick = False

    def __init__(self, publisher):
        super().__init__(publisher)
        self.locked = {}

    def tick(self, vehicle, elapsed_seconds=1.0, context=None):
        if context is None or "gps" not in context:
            raise ValueError("Lock generation requires the latest GPS state")

        gps_data = context["gps"]
        if gps_data is None:
            raise ValueError("Lock generation requires the latest GPS state")

        current_state = self.locked.setdefault(vehicle.serial, True)
        if gps_data["moving"]:
            next_state = False
        elif gps_data["hub_id"] is not None:
            next_state = True
        else:
            return None

        if next_state == current_state:
            return None

        self.locked[vehicle.serial] = next_state
        data = {"locked": next_state}
        self._publish(vehicle, data)
        return data
