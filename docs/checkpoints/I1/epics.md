# Epics

## Epic01 - Smart Fleet and Hub Management
Provide a centralized fleet management system that allows the service operator to control the availability, distribution and operational condition of electric vehicles across different hubs.

The platform should support different vehicle categories, such as electric bicycles, cargo bikes and utility  (just examples, nothing decided yet), each with specific characteristics and capabilities. Operators should be able to manage the fleet lifecycle, monitor vehicle status and ensure that vehicles are correctly allocated to hubs.

**Scope:**
- Register and manage different types of electric vehicles.
- Define vehicle characteristics, including battery capacity, maximum load and vehicle type.
- Register and manage hubs located near parks and green spaces.
- Assign and transfer vehicles between hubs.
- Monitor vehicle availability and operational status.
- Manage vehicle lifecycle (available, reserved, in use, maintenance and retired).
- Activate and deactivate vehicles.
- Configure minimum and maximum fleet capacity per hub.

**Value:** Ensures that the municipality has a properly distributed and operational fleet, making vehicles available where field workers need them.

## Epic02 - Vehicle Discovery, Reservation and Trip Management
Provide field workers with a complete vehicle-sharing experience, allowing them to find suitable vehicles, make reservations and manage their trips across different urban green spaces.

MovingTrees should consider the specific requirements of outdoor maintenance activities. Vehicle selection should take into account battery level, load capacity and vehicle type, helping workers choose the most appropriate vehicle for their tasks.

**Scope:**
- Browse available vehicles by hub and vehicle category.
- View vehicle specifications, battery level and availability.
- Filter vehicles according to operational requirements.
- Recommend suitable vehicles based on required load capacity and estimated autonomy.
- Create scheduled reservations or start immediate usage.
- Prevent overlapping reservations and invalid bookings.
- Start trips using valid reservations.
- End trips at authorized hubs.
- Automatically update vehicle availability after each trip.
- Maintain trip history, including duration, distance and associated charges.
- Handle deposits and overtime charges through a simulated payment gateway.

**Value:** Simplifies daily mobility for municipal workers while ensuring that vehicles are used efficiently and according to their capabilities.

## Epic03 - Fleet Telemetry and Real-Time Vehicle Monitoring
Provide real-time visibility into the operational condition of the fleet through vehicle telemetry, allowing the platform to monitor vehicles and react to relevant events automatically.

A dedicated simulator will reproduce vehicle sensor data and communicate with the platform through an integration interface, such as MQTT. This information will allow the operator to monitor battery levels, location and vehicle conditions without requiring physical hardware.

**Scope:**
- Develop an independent vehicle telemetry simulator.
- Generate battery-level updates.
- Simulate GPS positions and vehicle movement between hubs.
- Monitor vehicle lock and unlock states.
- Generate fault events and abnormal operating conditions.
- Integrate telemetry data through MQTT or an equivalent communication interface.
- Update vehicle information based on incoming events.
- Detect vehicles with insufficient battery for new trips.
- Detect abnormal conditions that may affect vehicle availability.
- Maintain telemetry history for operational analysis.

**Value:** Provides the operator with visibility into the fleet's condition and enables the platform to make more informed decisions about vehicle availability and maintenance.

## Epic04 - Predictive Maintenance and Service Management
Provide a maintenance management system that ensures vehicles remain safe, operational and available to municipal workers.

The platform should allow the operator to manage maintenance requests, assign technicians and track repair progress. Telemetry events can also trigger maintenance alerts when a vehicle reports a fault or an abnormal condition.

**Scope:**
- Create maintenance tickets manually or automatically from telemetry events.
- Categorize incidents according to their type and severity.
- Assign maintenance tickets to technicians.
- Define maintenance priorities and SLA deadlines.
- Track ticket status and repair progress.
- Record inspections, repairs and battery replacements.
- Maintain a complete maintenance history for each vehicle.
- Automatically prevent the reservation of vehicles requiring maintenance.
- Monitor repair times and SLA compliance.
- Notify operators when maintenance deadlines are approaching or have been exceeded.


**Value:** Reduces vehicle downtime, improves fleet reliability and ensures that maintenance problems are addressed within the agreed service levels.

## Epic05 - Municipal Fleet Governance and Performance Analytics
Provide the municipality with a centralized management interface to define operational policies, monitor service performance and evaluate how effectively the MovingTrees fleet supports its green-space operations.

The client should be able to configure the conditions under which vehicles are used and analyse whether the service meets its operational expectations and contractual SLAs.

**Scope:**
- Define maximum trip durations and usage restrictions.
- Configure vehicle access according to user permissions.
- Define fleet allocation limits for each hub.
- Monitor fleet availability and utilization.
- Track the number of trips and reservations per day.
- Monitor vehicle distribution across hubs.
- Analyse maintenance costs and repair times.
- Monitor SLA compliance and service availability.
- Display technology metrics, including API latency, error rates and telemetry processing delays.
- Provide dashboards and reports to support operational decisions.

**Value:** Gives the municipality control over its mobility service and provides measurable information about fleet efficiency, service quality and operational performance.