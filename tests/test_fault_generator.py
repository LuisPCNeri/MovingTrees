import json
from unittest.mock import Mock
from pathlib import Path

import pytest

from simulator.generators.fault import FaultGenerator
from simulator.publisher import SCHEMA_PATH
from simulator.registry import Vehicle


VEHICLE = Vehicle("MT-EB-01", "demo-municipality", "central-hub")


def run_faults(seed, probability, count=1000):
    publisher = Mock()
    generator = FaultGenerator(publisher, probability=probability, seed=seed)
    events = [
        generator.tick(VEHICLE)
        for _ in range(count)
    ]
    return [event for event in events if event is not None], publisher


def test_fault_types_and_severities_are_from_the_shared_allowed_values():
    events, _ = run_faults(seed=42, probability=1, count=100)

    assert {event["fault_type"] for event in events} <= set(
        FaultGenerator.ALLOWED_FAULT_TYPES
    )
    assert {event["severity"] for event in events} <= set(FaultGenerator.SEVERITIES)


def test_fault_type_list_matches_shared_schema():
    schema = json.loads(Path(SCHEMA_PATH).read_text(encoding="utf-8"))
    fault_schema = next(
        branch
        for branch in schema["oneOf"]
        if branch["properties"]["event_type"]["const"] == "fault"
    )
    schema_types = fault_schema["properties"]["data"]["properties"]["fault_type"][
        "enum"
    ]

    assert list(FaultGenerator.ALLOWED_FAULT_TYPES) == schema_types


def test_fault_frequency_is_seeded_and_respects_probability():
    first_events, _ = run_faults(seed=17, probability=0.1)
    second_events, _ = run_faults(seed=17, probability=0.1)

    assert first_events == second_events
    assert 70 <= len(first_events) <= 130


def test_zero_probability_emits_no_faults():
    events, publisher = run_faults(seed=42, probability=0)

    assert events == []
    publisher.publish.assert_not_called()


@pytest.mark.parametrize("probability", [-0.1, 1.1, float("nan"), float("inf")])
def test_rejects_invalid_probability(probability):
    with pytest.raises(ValueError, match="Fault probability"):
        FaultGenerator(Mock(), probability=probability)
