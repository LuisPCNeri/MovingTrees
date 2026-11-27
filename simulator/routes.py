import json
import math
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class HubLocation:
    latitude: float
    longitude: float


def load_hub_locations(path):
    path = Path(path)
    try:
        records = json.loads(path.read_text(encoding="utf-8"))
    except OSError as error:
        raise ValueError(f"Unable to read route file {path}: {error}") from error
    except json.JSONDecodeError as error:
        raise ValueError(f"Invalid JSON in route file {path}: {error}") from error

    if not isinstance(records, list) or not records:
        raise ValueError("Route file must contain a non-empty array of hubs")

    locations = {}
    for index, record in enumerate(records):
        if not isinstance(record, dict):
            raise ValueError(f"Hub at index {index} must be a JSON object")
        municipality = record.get("municipality")
        hub = record.get("hub")
        latitude = record.get("latitude")
        longitude = record.get("longitude")
        if not isinstance(municipality, str) or not municipality.strip():
            raise ValueError(f"Hub at index {index} must have a municipality")
        if not isinstance(hub, str) or not hub.strip():
            raise ValueError(f"Hub at index {index} must have a hub identifier")
        if (
            isinstance(latitude, bool)
            or not isinstance(latitude, (int, float))
            or not math.isfinite(latitude)
            or not -90 <= latitude <= 90
        ):
            raise ValueError(f"Hub at index {index} has an invalid latitude")
        if (
            isinstance(longitude, bool)
            or not isinstance(longitude, (int, float))
            or not math.isfinite(longitude)
            or not -180 <= longitude <= 180
        ):
            raise ValueError(f"Hub at index {index} has an invalid longitude")

        key = (municipality.strip(), hub.strip())
        if key in locations:
            raise ValueError(f"Duplicate hub coordinates: {key[0]}/{key[1]}")
        locations[key] = HubLocation(float(latitude), float(longitude))

    municipalities = {municipality for municipality, _ in locations}
    for municipality in municipalities:
        hubs = [
            location
            for (city, _), location in locations.items()
            if city == municipality
        ]
        if len(hubs) < 2 or len(set(hubs)) != len(hubs):
            raise ValueError(
                f"Municipality {municipality} must have distinct coordinates "
                "for each hub"
            )
    return locations
