import math

from simulator.generators.base import TelemetryGenerator


class GPSGenerator(TelemetryGenerator):
    event_type = "gps"
    EARTH_RADIUS_METERS = 6_371_000

    def __init__(self, publisher, hub_locations, speed_meters_per_second=4.0):
        super().__init__(publisher)
        if not math.isfinite(speed_meters_per_second) or speed_meters_per_second <= 0:
            raise ValueError("GPS speed must be a positive finite number")
        self.hub_locations = hub_locations
        self.speed_meters_per_second = speed_meters_per_second
        self.states = {}

    def tick(self, vehicle, elapsed_seconds=1.0):
        if not math.isfinite(elapsed_seconds) or elapsed_seconds <= 0:
            raise ValueError("Elapsed time must be a positive finite number")

        state = self.states.get(vehicle.serial)
        if state is None:
            state = self._initial_state(vehicle)
            self.states[vehicle.serial] = state

        origin = self._location(vehicle.municipality, state["origin"])
        destination = self._location(vehicle.municipality, state["destination"])
        route_length = self._distance(origin, destination)
        progress = min(
            1.0,
            state["progress"]
            + self.speed_meters_per_second * elapsed_seconds / route_length,
        )
        latitude = origin.latitude + (destination.latitude - origin.latitude) * progress
        longitude = origin.longitude + (destination.longitude - origin.longitude) * progress
        arrived = progress >= 1.0

        data = {
            "latitude": latitude,
            "longitude": longitude,
            "hub_id": state["destination"] if arrived else None,
            "moving": not arrived,
        }
        self._publish(vehicle, data)

        if arrived:
            state["origin"] = state["destination"]
            state["destination"] = self._next_hub(
                vehicle.municipality,
                state["destination"],
            )
            state["progress"] = 0.0
        else:
            state["progress"] = progress

        return data

    def _initial_state(self, vehicle):
        self._location(vehicle.municipality, vehicle.hub)
        return {
            "origin": vehicle.hub,
            "destination": self._next_hub(vehicle.municipality, vehicle.hub),
            "progress": 0.0,
        }

    def _next_hub(self, municipality, current_hub):
        hubs = sorted(
            hub for city, hub in self.hub_locations if city == municipality
        )
        if len(hubs) < 2 or current_hub not in hubs:
            raise ValueError(
                f"Municipality {municipality} must have at least two configured hubs"
            )
        return hubs[(hubs.index(current_hub) + 1) % len(hubs)]

    def _location(self, municipality, hub):
        try:
            return self.hub_locations[(municipality, hub)]
        except KeyError as error:
            raise ValueError(
                f"No coordinates configured for hub {hub} in municipality {municipality}"
            ) from error

    @classmethod
    def _distance(cls, origin, destination):
        latitude_delta = math.radians(destination.latitude - origin.latitude)
        longitude_delta = math.radians(destination.longitude - origin.longitude)
        latitude_origin = math.radians(origin.latitude)
        latitude_destination = math.radians(destination.latitude)
        haversine = (
            math.sin(latitude_delta / 2) ** 2
            + math.cos(latitude_origin)
            * math.cos(latitude_destination)
            * math.sin(longitude_delta / 2) ** 2
        )
        return 2 * cls.EARTH_RADIUS_METERS * math.asin(math.sqrt(haversine))
