import json
from unittest.mock import Mock

import pytest

from simulator.generators.gps import GPSGenerator
from simulator.registry import Vehicle
from simulator.routes import HubLocation, load_hub_locations


ROUTE = {
    ("demo-municipality", "central-hub"): HubLocation(38.7223, -9.1393),
    ("demo-municipality", "botanical-hub"): HubLocation(38.7238, -9.1358),
}


@pytest.fixture
def vehicle():
    return Vehicle("MT-EB-01", "demo-municipality", "central-hub")


@pytest.fixture
def publisher():
    return Mock()


def test_loads_hub_coordinates(tmp_path):
    path = tmp_path / "routes.json"
    path.write_text(
        json.dumps(
            [
                {
                    "municipality": "demo-municipality",
                    "hub": "central-hub",
                    "latitude": 38.7,
                    "longitude": -9.1,
                },
                {
                    "municipality": "demo-municipality",
                    "hub": "botanical-hub",
                    "latitude": 38.71,
                    "longitude": -9.11,
                },
            ]
        ),
        encoding="utf-8",
    )

    assert load_hub_locations(path) == {
        ("demo-municipality", "central-hub"): HubLocation(38.7, -9.1),
        ("demo-municipality", "botanical-hub"): HubLocation(38.71, -9.11),
    }


def test_coordinates_stay_on_the_route(publisher, vehicle):
    generator = GPSGenerator(publisher, ROUTE, speed_meters_per_second=10)
    origin = ROUTE[("demo-municipality", "central-hub")]
    destination = ROUTE[("demo-municipality", "botanical-hub")]
    events = [generator.tick(vehicle, elapsed_seconds=1) for _ in range(5)]

    for event in events:
        progress = (
            (event["latitude"] - origin.latitude)
            / (destination.latitude - origin.latitude)
        )
        expected_longitude = origin.longitude + (
            destination.longitude - origin.longitude
        ) * progress
        assert 0 <= progress <= 1
        assert event["longitude"] == pytest.approx(expected_longitude, abs=1e-9)


def test_arrival_emits_destination_hub_id(publisher, vehicle):
    generator = GPSGenerator(publisher, ROUTE, speed_meters_per_second=10_000)

    event = generator.tick(vehicle, elapsed_seconds=1)

    assert event["hub_id"] == "botanical-hub"
    assert event["moving"] is False
    publisher.publish.assert_called_once_with(vehicle, "gps", event)


def test_no_teleporting_between_ticks(publisher, vehicle):
    generator = GPSGenerator(publisher, ROUTE, speed_meters_per_second=4)
    positions = [
        generator.tick(vehicle, elapsed_seconds=1)
        for _ in range(25)
    ]

    for current, following in zip(positions, positions[1:]):
        distance = generator._distance(
            HubLocation(current["latitude"], current["longitude"]),
            HubLocation(following["latitude"], following["longitude"]),
        )
        assert distance <= 4 + 1e-3


def test_arrival_and_return_route_are_continuous(publisher, vehicle):
    generator = GPSGenerator(
        publisher,
        ROUTE,
        speed_meters_per_second=100,
        hub_dwell_seconds=0,
    )

    for _ in range(10):
        arrived = generator.tick(vehicle)
        if arrived["hub_id"] is not None:
            break
    else:
        pytest.fail("Vehicle did not arrive at its destination")

    returned = generator.tick(vehicle)

    assert arrived["hub_id"] == "botanical-hub"
    assert returned["hub_id"] is None
    distance = generator._distance(
        HubLocation(arrived["latitude"], arrived["longitude"]),
        HubLocation(returned["latitude"], returned["longitude"]),
    )
    assert 0 < distance <= 100 + 1e-6


def test_hub_dwell_keeps_reporting_arrival_hub(publisher, vehicle):
    generator = GPSGenerator(
        publisher,
        ROUTE,
        speed_meters_per_second=100,
        hub_dwell_seconds=20,
    )

    arrived = generator.tick(vehicle, elapsed_seconds=10)
    first_dwell_tick = generator.tick(vehicle, elapsed_seconds=10)
    final_dwell_tick = generator.tick(vehicle, elapsed_seconds=10)
    departure = generator.tick(vehicle)

    assert arrived["hub_id"] == "botanical-hub"
    assert first_dwell_tick["hub_id"] == "botanical-hub"
    assert final_dwell_tick["hub_id"] == "botanical-hub"
    assert departure["hub_id"] is None
    assert departure["moving"] is True
