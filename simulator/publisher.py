import json
from datetime import datetime, timezone
from pathlib import Path

from jsonschema import Draft202012Validator, FormatChecker


SCHEMA_PATH = (
    Path(__file__).resolve().parent.parent
    / "schemas"
    / "telemetry-event.schema.json"
)


class TelemetryPublisher:
    def __init__(
        self,
        client,
        broker_host,
        broker_port,
        username=None,
        password=None,
        schema_path=SCHEMA_PATH,
    ):
        self.client = client
        self.broker_host = broker_host
        self.broker_port = broker_port
        self.username = username
        self.password = password
        self.validator = Draft202012Validator(
            json.loads(Path(schema_path).read_text(encoding="utf-8")),
            format_checker=FormatChecker(),
        )
        self.started = False

    def connect(self):
        self.client.reconnect_delay_set(min_delay=1, max_delay=30)
        if self.username is not None:
            self.client.username_pw_set(self.username, self.password)
        self.client.connect(self.broker_host, self.broker_port)
        self.client.loop_start()
        self.started = True

    def publish(self, vehicle, event_type, data):
        topic = self._topic(vehicle)
        payload = {
            "schema_version": 1,
            "event_type": event_type,
            "timestamp": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
            "vehicle_id": vehicle.serial,
            "municipality": vehicle.municipality,
            "data": data,
        }
        self.validator.validate(payload)
        result = self.client.publish(
            topic,
            json.dumps(payload, separators=(",", ":")),
            qos=1,
        )
        return result

    def close(self):
        if self.started:
            self.client.disconnect()
            self.client.loop_stop()
            self.started = False

    @staticmethod
    def _topic(vehicle):
        segments = (vehicle.municipality, vehicle.serial)
        if any(not part or any(char in part for char in "/+#") for part in segments):
            raise ValueError(
                "Municipality and vehicle serial must be valid MQTT topic segments"
            )
        return f"telemetry/{vehicle.municipality}/{vehicle.serial}"
