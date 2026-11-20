# Scenarios

## Epic01: User and Access Management

### SC-01: Registering and activating a user

Related story: US-01 - Actors: Francisca (Municipal Administrator) - Epic: Epic01

- Given Francisca is authenticated as a municipal administrator for her municipality,
- When she registers Paulo with credentials, role field worker and the "Parks and Gardens" team belonging to her municipality,
- Then the account is stored with status inactive,
- And the team association and municipality are saved.
- And Francisca can only manage users belonging to her municipality.
- Given the account is inactive,
- When Francisca activates it,
- Then Paulo can sign in,
- And the field-worker permissions are applied to his session.

Alternative flow: role change

- Given Paulo has an active account belonging to Francisca's municipality,
- When Francisca changes his role to field technician,
- Then his permissions change on the next sign-in,
- And his previous trips remain in the history.

### SC-02: Signing in with role restrictions

Related story: US-02 - Actors: Paulo (Field Worker) - Epic: Epic01

- Given Paulo has an active field-worker account in the "Parks and Gardens" team,
- When he signs in with correct credentials,
- Then a session is created with his role and team,
- And he sees only the field-worker features.
- When he tries to open the operator maintenance queue,
- Then access is refused,
- And no operator data is returned.

Alternative flow: invalid credentials

- Given the credentials are wrong or the account is inactive,
- When the sign-in is attempted,
- Then access is denied,
- And the message does not reveal which credential was invalid.

## Epic02: Fleet and Vehicle Lifecycle Management

### SC-03: Registering a vehicle type and a vehicle

Related story: US-03 - Actors: André (Fleet Operator) - Epic: Epic02

- Given André is authenticated as a fleet operator,
- When he registers the "Cargo Bike" type with 500 Wh battery capacity, 60 kg maximum load and 48 km autonomy,
- Then the type is stored in the catalogue,
- When he registers vehicle "MT-CB-01" of that type at "Parque Central",
- Then the vehicle appears in the fleet inventory with status available,
- And it is counted in the operator fleet dashboard.
- Given "MT-CB-01" has no active reservation or trip,
- When André edits its characteristics and later removes it from the fleet,
- Then the changes are applied,
- And its past trips remain recorded.

Alternative flow: duplicated serial number

- Given vehicle "MT-CB-01" already exists,
- When André registers another vehicle with the same serial number,
- Then the vehicle is not created,
- And the duplicated serial number is identified.

### SC-04: Moving a vehicle through its lifecycle

Related story: US-04 - Actors: André (Fleet Operator) - Epic: Epic02

- Given vehicle "MT-EB-02" is available,
- When Paulo reserves it and starts a trip that later ends,
- Then its status changes to reserved, then in use and finally available,
- And the transitions appear in the operator fleet dashboard.
- Given André deactivates "MT-EB-02",
- When Paulo browses the hub,
- Then the vehicle is not bookable.
- When André activates it again and no blocker remains,
- Then the vehicle becomes available.

Alternative flow: invalid transition

- Given a vehicle has status retired,
- When an attempt is made to set its status directly to available,
- Then the transition is rejected,
- And the status remains retired.

## Epic03: Hub Management and Vehicle Distribution

### SC-05: Registering a hub and transferring a vehicle

Related story: US-05 - Actors: André (Fleet Operator) - Epic: Epic03

- Given André is authenticated as a fleet operator,
- When he registers "Jardim Botânico" with its location and associated green space,
- Then the hub is created and appears in the hub list.
- Given vehicle "MT-CB-01" is assigned to "Parque Central",
- When André transfers it to "Jardim Botânico",
- Then the occupancy of both hubs is updated,
- And the vehicle is discovered at "Jardim Botânico".

Alternative flow: vehicle in use

- Given "MT-CB-01" is currently in use,
- When André tries to transfer it,
- Then the transfer is rejected,
- And the vehicle stays at its original hub.

### SC-06: Reacting to a hub occupancy breach

Related story: US-06 - Actors: André (Fleet Operator) - Epic: Epic03

- Given Francisca configured a range of 2 to 5 vehicles for "Parque Central" in her municipality,
- And the hub currently holds one vehicle,
- When the occupancy is recalculated,
- Then an alert identifies "Parque Central" as below its minimum,
- And André sees the current count and the configured range in the hub view.
- When André transfers another vehicle to the hub, bringing the count to two,
- Then the alert is cleared,
- And the breach and the recovery are recorded.

Alternative flow: above maximum

- Given "Parque Central" holds five vehicles, its maximum,
- When another vehicle is assigned to it,
- Then an alert identifies the hub as above its maximum capacity.

## Epic04: Vehicle Discovery and Recommendation

### SC-07: Finding a vehicle for a task

Related story: US-07 - Actors: Paulo (Field Worker) - Epic: Epic04

- Given Paulo is signed in as a field worker,
- And "Parque Central" has an e-bike with 80% battery and 15 kg load, a cargo bike with 60% battery and 60 kg load, and a tricycle with pending maintenance,
- When he opens the list of cargo bikes at that hub,
- Then the cargo bike is shown with its specifications, battery level and availability.
- When Paulo filters by minimum load of 50 kg and minimum autonomy of 30 km,
- Then only vehicles meeting both requirements are shown,
- And the tricycle with pending maintenance is not among the bookable results.

Alternative flow: battery below the minimum

- Given Francisca configured a minimum battery of 25% to book for her municipality,
- And a vehicle at the hub has 18% battery,
- When Paulo browses the hub,
- Then that vehicle is excluded from the bookable results.

### SC-08: Getting a recommendation for a route

Related story: US-08 - Actors: Paulo (Field Worker) - Epic: Epic04

- Given Paulo needs to carry 50 kg over an estimated distance of 12 km from "Parque Central",
- And the cargo bike "MT-CB-01" is available with 60 kg capacity and 48 km autonomy,
- When Paulo requests a recommendation,
- Then "MT-CB-01" is ranked first,
- And its load capacity and autonomy are shown.
- Given Francisca restricted utility tricycles to another team within Paulo's municipality,
- When Paulo requests the recommendation,
- Then no utility tricycle is suggested.

Alternative flow: no suitable vehicle

- Given Paulo enters a required load of 120 kg,
- When he requests a recommendation,
- Then a message states that no suitable vehicle exists,
- And the load requirement that could not be met is identified.

## Epic05: Reservations and Trips

### SC-09: Reserving the same vehicle concurrently

Related story: US-09 - Actors: Paulo and another field worker - Epic: Epic05

- Given vehicle "MT-CB-01" is available at "Parque Central" for the morning,
- When Paulo and another worker submit reservations for overlapping intervals at the same time,
- Then only one reservation is confirmed,
- And the other request is rejected with a conflict message.
- Given Paulo's reservation is confirmed from 10:00 to 11:00,
- When he starts an immediate use from that reservation,
- Then the vehicle status becomes reserved and then in use.

Alternative flow: vehicle not allowed

- Given Francisca's municipal rules do not allow Paulo's team to use utility tricycles,
- When Paulo tries to reserve one,
- Then the reservation is rejected,
- And the access rule is identified.

### SC-10: Cancelling and expiring reservations

Related story: US-10 - Actors: Paulo (Field Worker) - Epic: Epic05

- Given Paulo holds a reservation for "MT-EB-02" that has not started,
- When he cancels it,
- Then the reservation is marked cancelled,
- And the vehicle becomes available again.
- Given another reservation was not picked up within the configured pickup time,
- When the expiry check runs,
- Then the reservation is marked expired,
- And the vehicle becomes available again.

Alternative flow: trip already started

- Given a reservation already originated an active trip,
- When an attempt is made to cancel it,
- Then the cancellation is rejected,
- And the trip remains active.

### SC-11: Starting and ending a trip

Related story: US-11 - Actors: Paulo (Field Worker) - Epic: Epic05

- Given Paulo has an active reservation for "MT-CB-01" at "Parque Central",
- When he starts the trip,
- Then the vehicle status becomes in use,
- And a trip record is created with start time, origin hub and vehicle.
- Given Paulo rides 4 km to "Jardim Botânico", which is an authorized hub,
- When he ends the trip there,
- Then the trip is closed with end time, duration and distance,
- And the vehicle becomes available at that hub automatically.
- When another worker browses "Jardim Botânico",
- Then the vehicle appears as available without manual intervention.

Alternative flow: unauthorized hub

- Given Paulo is outside the authorized hubs,
- When he tries to end the trip,
- Then the action is rejected,
- And the trip stays open.

### SC-12: Reviewing trips and charges

Related story: US-12 - Actors: Paulo (Field Worker) - Epic: Epic05

- Given Paulo has one active trip and three closed trips on different dates and hubs,
- When he opens his trip dashboard,
- Then active trips are listed separately from past trips,
- And each trip shows duration, distance and charges.
- When he filters by the last week and by "Parque Central",
- Then only the matching trips are shown.
- Given one trip included overtime,
- When Paulo opens its details,
- Then the deposit, overtime and total charge are shown separately.

## Epic06: Payments (Deposits and Overtime Charges)

### SC-13: Charging a deposit and overtime to a team

Related story: US-13 - Actors: Francisca (Municipal Administrator) - Epic: Epic06

- Given Francisca configured a deposit and an overtime value for her municipality,
- And Paulo belongs to the "Parks and Gardens" team,
- When his reservation starts,
- Then the deposit is registered against the team's account by the test payment adapter,
- And the record identifies Paulo and the vehicle.
- Given Paulo closes the trip later than the configured maximum duration,
- When the payment step runs,
- Then the deposit is released,
- And the overtime charge is billed to the team account.
- Given the test adapter is replaced by another test implementation,
- When a new deposit is processed,
- Then the business logic is unchanged,
- And no real money is transferred.

### SC-14: Consulting payment history per team

Related story: US-14 - Actors: Francisca (Municipal Administrator) - Epic: Epic06

- Given deposits and overtime charges have been registered for teams within Francisca's municipality,
- When Francisca opens the payment history,
- Then each movement shows team, worker, vehicle, date, type and amount.
- And no payment records from other municipalities are visible to Francisca.
- When she filters by the "Parks and Gardens" team and the last month,
- Then only that team's movements for that period are shown.
- When she opens one movement's details,
- Then it is identified as a test transaction with no real monetary effect.

## Epic07: Fleet Telemetry Simulator

### SC-15: Publishing telemetry from the simulator

Related story: US-15 - Actors: Telemetry Simulator - Epic: Epic07

- Given the simulator container is running for ten configured vehicles,
- When it publishes telemetry through MQTT,
- Then battery-level updates and GPS positions are emitted,
- And movement between hubs, lock/unlock states and fault events are emitted.
- Given André changes the publication rate and the number of simulated vehicles to twenty,
- When the simulator is restarted,
- Then events are published at the new rate for twenty vehicles.
- Given the platform is stopped and restarted,
- When the simulator is running,
- Then it continues publishing events independently.

## Epic08: Telemetry Integration and Real-Time Monitoring

### SC-16: Consuming telemetry through a replaceable adapter

Related story: US-16 - Actors: André (Fleet Operator) - Epic: Epic08

- Given vehicle "MT-EB-02" is registered,
- When the simulator publishes a battery update of 42% through the telemetry adapter,
- Then the vehicle state is updated to 42%,
- And the event is stored in telemetry history.
- Given André is viewing the live fleet status,
- When the next telemetry event arrives,
- Then the displayed status changes without a manual refresh.
- Given the telemetry adapter is replaced by another simulator implementation,
- When that source publishes events,
- Then the business logic is unchanged and the vehicle state still updates.

### SC-17: Blocking a vehicle after low battery or a fault

Related story: US-17 - Actors: André (Fleet Operator) - Epic: Epic08

- Given Francisca configured a minimum battery of 25% to book for her municipality,
- And "MT-EB-02" has 40% battery,
- When telemetry reports 18%,
- Then the vehicle is flagged as unavailable,
- And new reservations are blocked.
- Given a fault event arrives for "MT-CB-01",
- When the platform processes it,
- Then that vehicle becomes unavailable and André is notified.
- Given "MT-EB-02" later reports 60% with no open fault,
- When the event is processed,
- Then the vehicle becomes available again,
- And the change is recorded.

## Epic09: Maintenance Management

### SC-18: Creating and tracking a maintenance ticket

Related story: US-18 - Actors: André (Fleet Operator) - Epic: Epic09

- Given André is authenticated as a fleet operator,
- When he registers a high-severity brake problem on "MT-CB-01",
- Then a ticket is created with status open,
- And a repair deadline is assigned according to the high severity.
- When André assigns it to Pedro and the ticket moves to in progress, resolved and closed,
- Then each transition is recorded with a timestamp,
- And the history is attached to the vehicle.
- Given the deadline is near and the ticket is not closed,
- When the deadline check runs,
- Then André is notified with the ticket reference and the remaining time.

### SC-19: Reporting a problem from the field

Related story: US-19 - Actors: Paulo (Field Worker) - Epic: Epic09

- Given Paulo finds a damaged brake on "MT-CB-01" at "Parque Central",
- When he submits the description "front brake loses pressure" and a photo,
- Then a maintenance ticket is created for that vehicle,
- And the report is linked to Paulo's account.
- When André opens the maintenance queue,
- Then he sees the description, the photo and the vehicle identification.

Alternative flow: missing description

- Given Paulo tries to submit a report without a description,
- When he sends it,
- Then the report is rejected,
- And the description field is required.

### SC-20: Closing a repair from a phone

Related story: US-20 - Actors: Pedro (Field Technician) - Epic: Epic09

- Given Pedro is signed in on his phone with two open tickets assigned,
- When he opens his ticket list,
- Then tickets are sorted by urgency and repair deadline,
- And each shows the fault, vehicle and hub location.
- Given he selected the ticket for "MT-CB-01",
- When he records an inspection and a brake replacement and closes the ticket,
- Then the intervention is saved in the ticket and the vehicle's maintenance history,
- And the vehicle becomes available again.
- Given Pedro has no tickets assigned,
- When he opens the list,
- Then a message states that there are no assigned repairs.

### SC-21: Opening a ticket automatically from a fault

Related story: US-21 - Actors: Telemetry Simulator and André (Fleet Operator) - Epic: Epic09

- Given no open ticket exists for "MT-EB-07",
- When the simulator publishes a critical "motor_overheat" fault,
- Then a ticket is created with type "motor", severity "critical" and the corresponding deadline,
- And André is notified.
- Given "MT-EB-07" is blocked by the open ticket,
- When Paulo tries to reserve it,
- Then the reservation is rejected.
- When another "motor_overheat" event arrives,
- Then no new ticket is created,
- And the event is added to the existing ticket's history.
- Given the ticket is closed,
- When no other blocker remains,
- Then the vehicle can be reserved again.

## Epic10: Client Rules of Use

### SC-22: Applying municipal rules of use

Related story: US-22 - Actors: Francisca (Municipal Administrator) and Paulo (Field Worker) - Epic: Epic10

- Given Francisca is authenticated as a municipal administrator for her municipality,
- When she saves a maximum trip duration of two hours, a minimum battery of 25%, a rule that utility tricycles are only for the Waste team, hub limits of 2 to 5 vehicles, and deposit and overtime values,
- Then the rules are stored for her municipality,
- And they are applied only to new bookings and charges belonging to that municipality.
- And these settings do not affect any other municipality.
- When Paulo tries to reserve a vehicle for three hours,
- Then the reservation is rejected,
- And the maximum trip duration rule is identified.
- When Paulo opens the vehicle list,
- Then utility tricycles are not offered to him.
- Given Francisca later changes the deposit value,
- When a new reservation is created,
- Then the new value is used,
- And previous transactions keep their original amount.

## Epic11: Performance Indicators and Dashboards

### SC-23: Consulting service indicators

Related story: US-23 - Actors: Francisca (Municipal Administrator) - Epic: Epic11

- Given the platform contains fleet, trip and maintenance data for several weeks,
- When Francisca opens her municipality's dashboard,
- Then it shows her municipality's fleet usage, the share of vehicles ready to use against the agreed target, trips per day, time to repair and distribution per hub.
- When she filters to "Jardim Botânico" and the last month,
- Then the indicators are recalculated using only data from her municipality for that hub and period.
- Given the agreed availability target is 90% and the ready-to-use share is 85%,
- When she compares the values,
- Then the dashboard indicates that the target is not met.

### SC-24: Monitoring service performance for a municipality

Related story: US-24 - Actors: Francisca (Municipal Administrator) - Epic: Epic11

- Given the platform has recorded API latency, error rate and telemetry reception delay associated with each municipality,
- When Francisca opens her municipality's system view,
- Then she sees the performance indicators relevant to her municipality,
- And she cannot access system metrics or operational data belonging exclusively to other municipalities.
- Given the error rate or telemetry reception delay affecting her municipality crosses its configured threshold,
- When the monitoring check runs,
- Then her municipality's service status is marked as degraded,
- And Francisca is notified.
- When she opens the alert details,
- Then she sees the affected service component and the recent errors relevant to her municipality.