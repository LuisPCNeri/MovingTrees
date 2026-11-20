# Epics

## Epic01 - User and Access Management

Allow municipal workers, operators, technicians, client managers and admins to access the platform securely, each with the permissions of their role.

**Scope:**
- Register and authenticate users.
- Role-based access: field worker (end user), operator, field technician, client manager, platform admin.
- Associate each field worker with a municipal team/department.
- Admin manages user accounts, roles and account activation.
- Admin monitors whether the platform is working correctly and keeps the application running.

**Value:** Guarantees that each actor only sees and does what their role allows, which is the basis for applying the municipality's rules of use.

## Epic02 - Fleet and Vehicle Lifecycle Management

Allow the operator to register and control the fleet of electric bicycles, cargo bikes and utility tricycles.

**Scope:**
- Register vehicle types with their characteristics (battery capacity, maximum load, autonomy).
- Register, edit and remove vehicles.
- Manage the vehicle lifecycle (available, reserved, in use, maintenance, retired) with valid transitions only.
- Activate and deactivate vehicles.
- Operator dashboard with fleet status.

**Value:** Keeps an accurate, up-to-date view of every vehicle and its operational state.

## Epic03 - Hub Management and Vehicle Distribution

Manage the hubs located near parks and green spaces and the distribution of vehicles between them.

**Scope:**
- Register and manage hubs (location, associated green space).
- Assign vehicles to hubs and transfer them between hubs.
- Show the occupancy of each hub against its configured limits (limits are set by the client, Epic10).
- Alert the operator when a hub is below its minimum or above its maximum capacity.

**Value:** Ensures vehicles are available where field workers need them.

## Epic04 - Vehicle Discovery and Recommendation

Help field workers find the most suitable vehicle for their task.

**Scope:**
- Browse available vehicles by hub and vehicle type.
- View vehicle specifications, battery level and availability.
- Filter by operational requirements (load capacity, autonomy).
- Recommend vehicles based on the required load and the estimated distance of the task.
- Exclude vehicles with low battery or pending maintenance from the results.

**Value:** Workers choose a vehicle that can actually carry their equipment and complete the trip.

## Epic05 - Reservations and Trips

Let field workers book vehicles and manage their trips between green spaces.

**Scope:**
- Create scheduled reservations or start an immediate use.
- Prevent overlapping and invalid reservations (including concurrent bookings of the same vehicle).
- Cancel reservations; expire reservations that are not picked up in time.
- Start a trip from a valid reservation; end it only at an authorized hub.
- Update vehicle availability automatically after each trip.
- User dashboard with active and past trips (duration, distance, charges).

**Value:** Simple, reliable daily mobility for municipal workers.

## Epic06 - Payments (Deposits and Overtime Charges)

Process the financial side of each use through a test payment service, with no real money involved.

**Scope:**
- Charge a deposit when a reservation/use starts and release it when the vehicle is returned correctly.
- Calculate and charge overtime when a trip exceeds the maximum duration.
- Charges are billed to the account of the worker's team/department (not to the individual worker).
- Payment adapter isolated from the business logic; payment history per team/department, with the worker who made each trip.

**Value:** Encourages responsible use and timely returns, with costs traceable per team/department.

## Epic07 - Fleet Telemetry Simulator

Build an independent service, in its own container, that simulates vehicle sensors.

**Scope:**
- Generate battery-level updates.
- Simulate GPS positions and movement between hubs.
- Simulate lock/unlock states.
- Generate fault events and abnormal conditions.
- Publish telemetry through MQTT.
- Configurable rate and number of vehicles (for load tests).

**Value:** Provides realistic fleet data without real hardware.

## Epic08 - Telemetry Integration and Real-Time Monitoring

Consume the telemetry in the platform and react to it.

**Scope:**
- Consume telemetry through a single port/adapter, replaceable by another simulator with no change to the business logic.
- Update vehicle state from incoming events.
- Block reservations of vehicles with insufficient battery.
- Detect faults/abnormal conditions and make the vehicle unavailable.
- Store telemetry history.
- Show live vehicle status to the operator.

**Value:** Availability decisions are based on the vehicle's real condition.

## Epic09 - Maintenance Management

Ensure vehicles are repaired within the deadlines agreed with the municipality.

**Scope:**
- Create maintenance tickets manually or automatically from telemetry faults.
- Categorize tickets by type and severity, with a maximum repair deadline per severity.
- Assign tickets to technicians; technician records inspections, repairs and battery swaps and closes the ticket.
- Track ticket status; maintenance history per vehicle.
- Notify operators when a repair deadline is near or exceeded.
- Field workers report problems with a vehicle (description and optional photo), which creates a maintenance ticket.

**Value:** Less time with vehicles out of service and repairs done within the agreed deadlines.

## Epic10 - Client Rules of Use

Allow the municipality (client manager) to define how the fleet may be used.

**Scope:**
- Define maximum trip duration and minimum battery to book.
- Define which teams/roles may use which vehicle types.
- Define minimum and maximum number of vehicles per hub.
- Define deposit and overtime values.

**Value:** The municipality controls the service according to its own policies.

## Epic11 - Performance Indicators and Dashboards

Give the municipality measurable information about the service.

**Scope:**
- Service indicators: fleet usage, share of vehicles ready to use (against the agreed target), trips per day, time to repair, distribution per hub.
- System indicators: API latency, error rate, delay in receiving vehicle data.
- Client dashboard with filters by period and hub.

**Value:** Evidence-based decisions about the fleet and the contract.