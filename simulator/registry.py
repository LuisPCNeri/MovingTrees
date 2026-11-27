import json
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class Vehicle:
    serial: str
    municipality: str
    hub: str


def load_vehicles(path):
    path = Path(path)
    try:
        records = json.loads(path.read_text(encoding="utf-8"))
    except OSError as error:
        raise ValueError(f"Unable to read vehicle registry {path}: {error}") from error
    except json.JSONDecodeError as error:
        raise ValueError(f"Invalid JSON in vehicle registry {path}: {error}") from error

    if not isinstance(records, list) or not records:
        raise ValueError("Vehicle registry must be a non-empty JSON array")

    vehicles = []
    serials = set()
    for index, record in enumerate(records):
        if not isinstance(record, dict):
            raise ValueError(f"Vehicle at index {index} must be a JSON object")

        values = {}
        for field in ("serial", "municipality", "hub"):
            value = record.get(field)
            if not isinstance(value, str) or not value.strip():
                raise ValueError(
                    f"Vehicle at index {index} must have a non-empty string {field}"
                )
            values[field] = value.strip()

        if values["serial"] in serials:
            raise ValueError(f"Duplicate vehicle serial: {values['serial']}")
        serials.add(values["serial"])
        vehicles.append(Vehicle(**values))

    return vehicles
