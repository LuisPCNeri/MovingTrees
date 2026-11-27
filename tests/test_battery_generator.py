from unittest.mock import Mock

import pytest

from simulator.generators.battery import BatteryGenerator
from simulator.registry import Vehicle


VEHICLE = Vehicle("MT-EB-01", "demo-municipality", "central-hub")
MOVING = {"moving": True, "hub_id": None}
AT_HUB = {"moving": False, "hub_id": "central-hub"}


def test_initial_battery_is_repeatable_with_fixed_seed():
    first = BatteryGenerator(Mock(), seed=42).tick(
        VEHICLE,
        context={"gps": MOVING},
    )
    second = BatteryGenerator(Mock(), seed=42).tick(
        VEHICLE,
        context={"gps": MOVING},
    )

    assert first == second


def test_battery_remains_within_zero_and_one_hundred_percent():
    publisher = Mock()
    generator = BatteryGenerator(publisher, seed=42)

    depleted = generator.tick(
        VEHICLE,
        elapsed_seconds=1_000_000,
        context={"gps": MOVING},
    )
    charged = generator.tick(
        VEHICLE,
        elapsed_seconds=1_000_000,
        context={"gps": AT_HUB},
    )

    assert depleted["battery_level"] == 0
    assert 0 <= depleted["battery_level"] <= 100
    assert charged["battery_level"] == 100
    assert 0 <= charged["battery_level"] <= 100


def test_battery_decreases_monotonically_while_moving():
    generator = BatteryGenerator(Mock(), seed=42)
    levels = [
        generator.tick(
            VEHICLE,
            elapsed_seconds=60,
            context={"gps": MOVING},
        )["battery_level"]
        for _ in range(5)
    ]

    assert all(before > after for before, after in zip(levels, levels[1:]))


def test_recharge_rate_at_a_hub():
    generator = BatteryGenerator(Mock(), seed=42)
    generator.tick(VEHICLE, context={"gps": AT_HUB})
    starting_level = generator.levels[VEHICLE.serial]

    charged = generator.tick(
        VEHICLE,
        elapsed_seconds=10,
        context={"gps": AT_HUB},
    )

    assert charged["battery_level"] - starting_level == pytest.approx(0.2)
