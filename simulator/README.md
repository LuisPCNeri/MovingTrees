# Telemetry simulator

Run the simulator and Mosquitto with `docker compose up --build`.

| Environment variable | Default | Meaning |
| --- | ---: | --- |
| `PUBLISH_RATE` | `1` | Positive, finite number of events per second across the whole fleet |
| `VEHICLE_COUNT` | `10` | Positive integer number of simulated vehicles |
| `VEHICLES_FILE` | `./simulator/vehicles.json` | Host path to a JSON array of registered vehicles |

Set the variables before starting or restarting the simulator container to
apply new values. Invalid values prevent the simulator from starting.
Each vehicle entry must contain non-empty string fields `serial`,
`municipality`, and `hub`; serials must be unique. Vehicle count cannot exceed
the number of entries in the seed file. In the container, the file is mounted
at `/data/vehicles.json`.

Events use `telemetry/<municipality>/<serial>` and are validated against
`schemas/telemetry-event.schema.json` before publishing. The Compose setup uses
development-only MQTT credentials (`simulator` / `demo-simulator-password`
for publishing and `viewer` / `demo-viewer-password` for subscribing). If you
change the publisher password, update `mosquitto/passwd` to match.

Run tests with `python -m pytest`. The MQTT integration test requires Docker
Compose and uses a temporary Mosquitto service.
