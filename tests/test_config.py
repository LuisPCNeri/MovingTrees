import pytest

from simulator.config import SimulatorConfig


def test_defaults(monkeypatch):
    monkeypatch.delenv("PUBLISH_RATE", raising=False)
    monkeypatch.delenv("VEHICLE_COUNT", raising=False)
    monkeypatch.delenv("VEHICLES_FILE", raising=False)
    monkeypatch.delenv("ROUTES_FILE", raising=False)

    assert SimulatorConfig.from_env() == SimulatorConfig(
        publish_rate=1.0,
        vehicle_count=10,
    )


def test_reads_registry_path(monkeypatch):
    monkeypatch.setenv("VEHICLES_FILE", "/data/fleet.json")

    assert SimulatorConfig.from_env().vehicles_file == "/data/fleet.json"


def test_reads_routes_path(monkeypatch):
    monkeypatch.setenv("ROUTES_FILE", "/data/routes.json")

    assert SimulatorConfig.from_env().routes_file == "/data/routes.json"


def test_reads_rate_and_vehicle_count(monkeypatch):
    monkeypatch.setenv("PUBLISH_RATE", "2.5")
    monkeypatch.setenv("VEHICLE_COUNT", "20")

    assert SimulatorConfig.from_env() == SimulatorConfig(
        publish_rate=2.5,
        vehicle_count=20,
    )


@pytest.mark.parametrize("value", ["", "abc", "0", "-1", "nan", "inf"])
def test_rejects_invalid_publish_rate(monkeypatch, value):
    monkeypatch.setenv("PUBLISH_RATE", value)
    monkeypatch.setenv("VEHICLE_COUNT", "10")

    with pytest.raises(ValueError, match="PUBLISH_RATE"):
        SimulatorConfig.from_env()


@pytest.mark.parametrize("value", ["", "abc", "0", "-1", "1.5"])
def test_rejects_invalid_vehicle_count(monkeypatch, value):
    monkeypatch.setenv("PUBLISH_RATE", "1")
    monkeypatch.setenv("VEHICLE_COUNT", value)

    with pytest.raises(ValueError, match="VEHICLE_COUNT"):
        SimulatorConfig.from_env()
