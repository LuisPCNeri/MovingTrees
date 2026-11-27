class TelemetryGenerator:
    event_type = None

    def __init__(self, publisher):
        self.publisher = publisher

    def tick(self, vehicle, elapsed_seconds=1.0):
        raise NotImplementedError

    def _publish(self, vehicle, data):
        return self.publisher.publish(vehicle, self.event_type, data)
