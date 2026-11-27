# Telemetry simulator

Run the simulator and Mosquitto with `docker compose up --build`.

| Environment variable | Default | Meaning |
| --- | ---: | --- |
| `PUBLISH_RATE` | `1` | Positive, finite number of events per second across the whole fleet |
| `VEHICLE_COUNT` | `10` | Positive integer number of simulated vehicles |

Set either variable before starting or restarting the simulator container to
apply the new value. Invalid values prevent the simulator from starting.

Run tests with `python -m pytest`. The MQTT integration test requires Docker
Compose and uses a temporary Mosquitto service.
