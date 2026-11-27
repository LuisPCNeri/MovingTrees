import json

import pytest

from simulator.registry import Vehicle, load_vehicles


def write_registry(tmp_path, records):
    path = tmp_path / "vehicles.json"
    path.write_text(json.dumps(records), encoding="utf-8")
    return path


def test_loads_registered_vehicles(tmp_path):
    path = write_registry(
        tmp_path,
        [
            {
                "serial": "MT-EB-01",
                "municipality": "demo-municipality",
                "hub": "central-hub",
            },
            {
                "serial": "MT-CB-01",
                "municipality": "demo-municipality",
                "hub": "botanical-hub",
            },
        ],
    )

    assert load_vehicles(path) == [
        Vehicle("MT-EB-01", "demo-municipality", "central-hub"),
        Vehicle("MT-CB-01", "demo-municipality", "botanical-hub"),
    ]


@pytest.mark.parametrize(
    "record",
    [
        {"municipality": "demo-municipality", "hub": "central-hub"},
        {"serial": "MT-EB-01", "hub": "central-hub"},
        {"serial": "MT-EB-01", "municipality": "demo-municipality"},
    ],
)
def test_rejects_missing_fields(tmp_path, record):
    with pytest.raises(ValueError):
        load_vehicles(write_registry(tmp_path, [record]))


def test_rejects_duplicate_serials(tmp_path):
    record = {
        "serial": "MT-EB-01",
        "municipality": "demo-municipality",
        "hub": "central-hub",
    }

    with pytest.raises(ValueError, match="Duplicate vehicle serial"):
        load_vehicles(write_registry(tmp_path, [record, record]))


def test_rejects_missing_registry_file(tmp_path):
    with pytest.raises(ValueError, match="Unable to read vehicle registry"):
        load_vehicles(tmp_path / "missing.json")


def test_default_seed_has_unique_vehicle_identifiers():
    from simulator.config import SimulatorConfig

    vehicles = load_vehicles(SimulatorConfig().vehicles_file)
    serials = [vehicle.serial for vehicle in vehicles]

    assert len(serials) == len(set(serials))
    assert {vehicle.serial for vehicle in vehicles[:10]} == {
        f"MT-EB-{index:02d}" for index in range(1, 11)
    }
