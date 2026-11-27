from unittest.mock import Mock

from simulator.generators.lock import LockGenerator
from simulator.registry import Vehicle


VEHICLE = Vehicle("MT-EB-01", "demo-municipality", "central-hub")
MOVING = {"moving": True, "hub_id": None}
ARRIVED = {"moving": False, "hub_id": "botanical-hub"}


def test_unlocks_once_when_a_trip_starts():
    publisher = Mock()
    generator = LockGenerator(publisher)

    first = generator.tick(VEHICLE, context={"gps": MOVING})
    repeated = generator.tick(VEHICLE, context={"gps": MOVING})

    assert first == {"locked": False}
    assert repeated is None
    publisher.publish.assert_called_once_with(VEHICLE, "lock", {"locked": False})


def test_locks_at_hub_arrival_without_repeating_transition():
    publisher = Mock()
    generator = LockGenerator(publisher)
    generator.tick(VEHICLE, context={"gps": MOVING})

    arrival = generator.tick(VEHICLE, context={"gps": ARRIVED})
    dwell = generator.tick(VEHICLE, context={"gps": ARRIVED})

    assert arrival == {"locked": True}
    assert dwell is None
    assert publisher.publish.call_count == 2
    assert publisher.publish.call_args.args == (
        VEHICLE,
        "lock",
        {"locked": True},
    )


def test_starts_locked_and_does_not_unlock_at_a_hub():
    publisher = Mock()
    generator = LockGenerator(publisher)

    result = generator.tick(VEHICLE, context={"gps": ARRIVED})

    assert result is None
    assert generator.locked[VEHICLE.serial] is True
    publisher.publish.assert_not_called()
