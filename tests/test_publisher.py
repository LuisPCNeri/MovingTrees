import json
from unittest.mock import Mock

import pytest
from jsonschema import ValidationError

from simulator.publisher import TelemetryPublisher
from simulator.registry import Vehicle


@pytest.fixture
def mqtt_client():
    return Mock()


@pytest.fixture
def publisher(mqtt_client):
    return TelemetryPublisher(
        mqtt_client,
        "localhost",
        1883,
        username="simulator",
        password="password",
    )


def test_publishes_schema_validated_payload_to_municipality_vehicle_topic(
    publisher, mqtt_client
):
    vehicle = Vehicle("MT-EB-01", "demo-municipality", "central-hub")

    publisher.publish(vehicle, "battery", {"battery_level": 42})

    topic, serialized_payload = mqtt_client.publish.call_args.args
    payload = json.loads(serialized_payload)
    assert topic == "telemetry/demo-municipality/MT-EB-01"
    assert mqtt_client.publish.call_args.kwargs["qos"] == 1
    assert payload["schema_version"] == 1
    assert payload["event_type"] == "battery"
    assert payload["vehicle_id"] == vehicle.serial
    assert payload["municipality"] == vehicle.municipality
    assert payload["data"] == {"battery_level": 42}


@pytest.mark.parametrize(
    ("event_type", "data"),
    [
        ("heartbeat", {}),
        ("battery", {"battery_level": 50}),
        (
            "gps",
            {
                "latitude": 38.7,
                "longitude": -9.1,
                "hub_id": "central-hub",
                "moving": False,
            },
        ),
        ("lock", {"locked": True}),
        ("fault", {"fault_type": "motor_overheat", "severity": "critical"}),
    ],
)
def test_validates_all_supported_event_payloads(publisher, mqtt_client, event_type, data):
    publisher.publish(
        Vehicle("MT-EB-01", "demo-municipality", "central-hub"),
        event_type,
        data,
    )

    assert mqtt_client.publish.call_count == 1
    payload = json.loads(mqtt_client.publish.call_args.args[1])
    publisher.validator.validate(payload)


def test_rejects_invalid_payload_before_publishing(publisher, mqtt_client):
    with pytest.raises(ValidationError):
        publisher.publish(
            Vehicle("MT-EB-01", "demo-municipality", "central-hub"),
            "battery",
            {"battery_level": 101},
        )

    mqtt_client.publish.assert_not_called()


def test_rejects_invalid_mqtt_topic_segments(publisher, mqtt_client):
    with pytest.raises(ValueError, match="topic segments"):
        publisher.publish(
            Vehicle("MT/EB/01", "demo-municipality", "central-hub"),
            "heartbeat",
            {},
        )

    mqtt_client.publish.assert_not_called()


def test_connect_sets_credentials_and_reconnect_backoff(publisher, mqtt_client):
    publisher.connect()

    mqtt_client.username_pw_set.assert_called_once_with("simulator", "password")
    mqtt_client.reconnect_delay_set.assert_called_once_with(min_delay=1, max_delay=30)
    mqtt_client.connect.assert_called_once_with("localhost", 1883)
    mqtt_client.loop_start.assert_called_once_with()

    publisher.close()
    mqtt_client.disconnect.assert_called_once_with()
    mqtt_client.loop_stop.assert_called_once_with()
