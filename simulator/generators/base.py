class TelemetryGenerator:
    event_type = None
    publishes_every_tick = True

    def __init__(self, publisher):
        self.publisher = publisher

    def tick(self, vehicle, elapsed_seconds=1.0, context=None):
        raise NotImplementedError

    def _publish(self, vehicle, data):
        return self.publisher.publish(vehicle, self.event_type, data)
