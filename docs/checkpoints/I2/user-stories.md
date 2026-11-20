# User Stories with Acceptance Criteria

## Epic01: User and Access Management

### US-01: Register users and manage municipal accounts

Epic: Epic01 - Persona: Francisca (Municipal Administrator) - Priority: Must - Scenario: SC-01

As a municipal administrator, I want to register users, assign them roles and teams, and manage their account activation, so that people in my municipality can access the platform according to their responsibilities.

**Acceptance criteria**

1. A user account is created
   - Given Francisca is authenticated as a municipal administrator,
   - When she registers a user with a name, credentials, role and team belonging to her municipality,
   - Then the account is created with status inactive,
   - And the user is associated with her municipality and the selected team.

2. Account activation enables access
   - Given a registered account is inactive,
   - When Francisca activates it,
   - Then the user can sign in,
   - And the permissions associated with the assigned role are applied.

3. Accounts can be updated and disabled
   - Given an existing active user belongs to Francisca's municipality,
   - When Francisca changes their role or deactivates their account,
   - Then the new permissions are applied immediately or access is denied,
   - And the user's past trips and maintenance reports remain in the history.

4. Municipal access restrictions are enforced
   - Given Francisca is managing users in her municipality,
   - When she attempts to access or modify an account belonging to another municipality,
   - Then the operation is rejected,
   - And no information about that account is disclosed.

### US-02: Sign in and access role-specific features

Epic: Epic01 - Persona: Paulo (Field Worker) - Priority: Must - Scenario: SC-02

As a field worker, I want to sign in with my account and access only the features and data associated with my role and municipality, so that I can use the platform securely.

**Acceptance criteria**

1. Successful sign-in
   - Given Paulo has an active field-worker account associated with the "Parks and Gardens" team,
   - When he signs in with correct credentials,
   - Then a session is created with his role, team and municipality,
   - And he sees only the field-worker features.

2. Failed sign-in
   - Given the credentials are wrong or the account is inactive,
   - When the user tries to sign in,
   - Then access is denied,
   - And the message does not reveal which credential was incorrect.

3. Role restrictions are enforced
   - Given Paulo is signed in with the field-worker role,
   - When he tries to access an operator-only page or action,
   - Then the operation is rejected,
   - And no restricted information is returned.

4. Municipal data restrictions are enforced
   - Given Paulo is signed in,
   - When he attempts to access data belonging to another municipality,
   - Then access is denied,
   - And only information associated with his municipality is returned.

---

## Epic02: Fleet and Vehicle Lifecycle Management

### US-03: Register vehicle types and vehicles

Epic: Epic02 - Persona: André (Fleet Operator) - Priority: Must - Scenario: SC-03

As a fleet operator, I want to register, edit and remove vehicle types and vehicles with their characteristics, so that the platform maintains an accurate description of the fleet.

**Acceptance criteria**

1. A vehicle type is created
   - Given André is authenticated as a fleet operator,
   - When he registers a vehicle type with battery capacity, maximum load and autonomy,
   - Then the type is stored in the catalogue,
   - And its characteristics can be reused by vehicles of that type.

2. A vehicle is registered
   - Given the selected vehicle type exists,
   - When André registers a vehicle with a unique serial number and assigns it to a hub,
   - Then the vehicle appears in the fleet inventory with status available,
   - And it is included in the operator fleet dashboard.

3. Vehicles can be edited and removed
   - Given a vehicle has no active reservation or trip,
   - When André edits its characteristics or removes it from the fleet,
   - Then the change is applied,
   - And its historical trips and maintenance records remain available.

4. Invalid data is rejected
   - Given André is registering a vehicle or vehicle type,
   - When he submits a duplicated serial number or a negative capacity, load or autonomy,
   - Then the record is not saved,
   - And the invalid field is identified.

### US-04: Manage the vehicle lifecycle

Epic: Epic02 - Persona: André (Fleet Operator) - Priority: Must - Scenario: SC-04

As a fleet operator, I want to manage vehicle activation and lifecycle states, so that the fleet status shown in the platform reflects the actual condition and availability of each vehicle.

**Acceptance criteria**

1. Valid lifecycle transitions are accepted
   - Given a vehicle is available,
   - When a reservation is created and a trip subsequently starts and ends,
   - Then its status changes to reserved, then in use and finally available.
   - Given a vehicle is in maintenance or retired,
   - When an attempt is made to return it directly to available,
   - Then the transition is rejected.

2. Vehicles can be activated and deactivated
   - Given a vehicle is available,
   - When André deactivates it,
   - Then it cannot be reserved or displayed as bookable to field workers.
   - Given the vehicle is deactivated,
   - When André activates it again,
   - Then it becomes available if no other blocker exists.

3. Fleet status is visible
   - Given vehicles are in different lifecycle states,
   - When André opens the fleet dashboard,
   - Then he sees the number of vehicles per status,
   - And he can filter the fleet by status and hub.

4. Historical information is preserved
   - Given a vehicle has previous trips or maintenance records,
   - When its lifecycle status changes,
   - Then its historical information remains accessible.

---

## Epic03: Hub Management and Vehicle Distribution

### US-05: Register hubs and distribute vehicles

Epic: Epic03 - Persona: André (Fleet Operator) - Priority: Must - Scenario: SC-05

As a fleet operator, I want to register hubs near green spaces and assign or transfer vehicles between them, so that vehicles are available where field workers need them.

**Acceptance criteria**

1. A hub is registered
   - Given André is authenticated as a fleet operator,
   - When he registers a hub with its location and associated green space,
   - Then the hub is created and appears in the hub list.

2. Vehicles are assigned and transferred
   - Given a vehicle is assigned to "Parque Central",
   - When André transfers it to "Jardim Botânico",
   - Then the occupancy of both hubs is updated,
   - And the vehicle is displayed at the destination hub.

3. Invalid transfers are rejected
   - Given a vehicle is currently in use or the target hub does not exist,
   - When André tries to transfer it,
   - Then the transfer is rejected,
   - And the vehicle remains assigned to its original hub.

4. Municipal boundaries are respected
   - Given André manages the fleet of a specific municipality,
   - When he registers a hub or transfers a vehicle,
   - Then the operation is restricted to hubs and vehicles belonging to that municipality.

### US-06: Monitor hub occupancy

Epic: Epic03 - Persona: André (Fleet Operator) - Priority: Must - Scenario: SC-06

As a fleet operator, I want to monitor the occupancy of each hub against the limits configured by the municipality and receive alerts when those limits are exceeded, so that vehicles are distributed according to local needs.

**Acceptance criteria**

1. Occupancy is visible against configured limits
   - Given Francisca has defined minimum and maximum vehicle quantities for the hubs in her municipality,
   - When André opens the hub overview,
   - Then each hub displays its current vehicle count and configured occupancy range.

2. Occupancy breaches raise alerts
   - Given a hub has fewer vehicles than its minimum or more than its maximum,
   - When its occupancy is recalculated,
   - Then an alert is raised identifying the hub and the breached limit,
   - And André can see the alert in the operator dashboard.

3. Recovery is recorded
   - Given a hub was below or above its configured limit,
   - When a vehicle transfer brings its occupancy back within the allowed range,
   - Then the alert is cleared,
   - And both the breach and its resolution are recorded.

4. Municipal configurations remain independent
   - Given different municipalities have different hub occupancy limits,
   - When occupancy is evaluated,
   - Then each hub is checked against the rules of its own municipality.

---

## Epic04: Vehicle Discovery and Recommendation

### US-07: Browse and filter vehicles

Epic: Epic04 - Persona: Paulo (Field Worker) - Priority: Must - Scenario: SC-07

As a field worker, I want to browse available vehicles by hub and type and filter them by load capacity and autonomy, so that I can choose a vehicle suitable for my equipment and route.

**Acceptance criteria**

1. Available vehicles are listed
   - Given Paulo is signed in as a field worker,
   - When he opens the vehicle list for a hub and vehicle type,
   - Then available vehicles are displayed with their specifications, battery level and availability.

2. Operational filters work
   - Given vehicles at the selected hub have different load capacities and autonomy,
   - When Paulo filters by minimum load and minimum autonomy,
   - Then only vehicles meeting both requirements are displayed.

3. Unsuitable vehicles are excluded
   - Given a vehicle has a battery level below the minimum configured for Paulo's municipality or has pending maintenance,
   - When Paulo browses or filters the hub,
   - Then that vehicle is not displayed among the bookable results.

4. Municipal restrictions are respected
   - Given Paulo's municipality restricts access to a particular vehicle type for his team,
   - When he browses the available vehicles,
   - Then vehicles of that type are not displayed as bookable.

### US-08: Receive a vehicle recommendation

Epic: Epic04 - Persona: Paulo (Field Worker) - Priority: Must - Scenario: SC-08

As a field worker, I want the platform to recommend a vehicle based on the load and distance of my task, so that I do not choose a vehicle that is unsuitable for the work.

**Acceptance criteria**

1. A suitable vehicle is recommended
   - Given Paulo enters the required load and estimated travel distance,
   - When he requests a recommendation at a hub,
   - Then available vehicles satisfying the load and autonomy requirements are ranked by suitability,
   - And each suggestion displays its maximum load and estimated autonomy.

2. No suitable vehicle is reported
   - Given no available vehicle satisfies the requested load or distance,
   - When Paulo requests a recommendation,
   - Then a message states that no suitable vehicle is available,
   - And the requirement that could not be satisfied is identified.

3. Municipal rules are respected
   - Given Francisca has restricted a vehicle type for Paulo's team,
   - When Paulo requests a recommendation,
   - Then vehicles of that type are not suggested.

4. Vehicle condition is considered
   - Given a vehicle has insufficient battery or an active maintenance restriction,
   - When recommendations are generated,
   - Then that vehicle is excluded from the results.

---

## Epic05: Reservations and Trips

### US-09: Reserve a vehicle without conflicts

Epic: Epic05 - Persona: Paulo (Field Worker) - Priority: Must - Scenario: SC-09

As a field worker, I want to schedule a vehicle reservation or start an immediate use, so that I can secure a suitable vehicle without conflicting with other workers' reservations.

**Acceptance criteria**

1. A reservation is created
   - Given a vehicle is available at Paulo's hub for the requested period,
   - When Paulo confirms a scheduled reservation or an immediate use,
   - Then a reservation is created,
   - And the vehicle status becomes reserved.

2. Overlapping and concurrent reservations are prevented
   - Given two workers request the same vehicle for overlapping periods at the same time,
   - When both requests are processed,
   - Then only one reservation is confirmed,
   - And the other request is rejected with a conflict message.

3. Invalid reservations are rejected
   - Given a vehicle is deactivated, has pending maintenance, has insufficient battery or its type is not allowed for Paulo's team,
   - When Paulo tries to reserve it,
   - Then the reservation is rejected,
   - And the reason is displayed.

4. Municipal rules are enforced
   - Given Paulo's municipality has configured a maximum trip duration,
   - When Paulo requests a reservation exceeding that duration,
   - Then the reservation is rejected,
   - And the applicable restriction is identified.

### US-10: Cancel or expire a reservation

Epic: Epic05 - Persona: Paulo (Field Worker) - Priority: Must - Scenario: SC-10

As a field worker, I want to cancel a reservation and have uncollected reservations expire automatically, so that vehicles are not unnecessarily blocked when they are not being used.

**Acceptance criteria**

1. A reservation can be cancelled
   - Given Paulo has a reservation that has not started,
   - When he cancels it,
   - Then the reservation is marked cancelled,
   - And the vehicle becomes available again if no other restriction exists.

2. An uncollected reservation expires
   - Given a reservation has not been picked up within the configured pickup time,
   - When the expiry check runs,
   - Then the reservation is marked expired,
   - And the vehicle becomes available again if no other restriction exists.

3. A started trip cannot be cancelled
   - Given a reservation has already originated an active trip,
   - When Paulo tries to cancel it,
   - Then the cancellation is rejected,
   - And the trip remains active.

4. Reservation history is preserved
   - Given a reservation is cancelled or expires,
   - When Paulo or an authorized operator consults its history,
   - Then its final status and relevant timestamps remain recorded.

### US-11: Start and end a trip

Epic: Epic05 - Persona: Paulo (Field Worker) - Priority: Must - Scenario: SC-11

As a field worker, I want to start a trip from a valid reservation and end it at an authorized hub, so that the platform records my journey and makes the vehicle available again.

**Acceptance criteria**

1. A trip starts from a valid reservation
   - Given Paulo has a valid reservation for a vehicle,
   - When he starts the trip,
   - Then the vehicle status becomes in use,
   - And a trip record is created with the start time, origin hub and vehicle.

2. A trip ends at an authorized hub
   - Given Paulo has an active trip and is at an authorized hub,
   - When he ends the trip,
   - Then the trip is closed with its end time, duration and distance,
   - And the vehicle is assigned to that hub.

3. Invalid trip actions are rejected
   - Given Paulo has no valid reservation or is outside an authorized hub,
   - When he tries to start or end a trip,
   - Then the action is rejected,
   - And the reason is displayed.

4. Availability is updated automatically
   - Given a trip has been successfully closed,
   - When another worker browses the destination hub,
   - Then the vehicle appears as available if no other restriction exists.

### US-12: Review active and past trips

Epic: Epic05 - Persona: Paulo (Field Worker) - Priority: Should - Scenario: SC-12

As a field worker, I want a dashboard showing my active and past trips and their charges, so that I can review my journeys and associated costs.

**Acceptance criteria**

1. Active and past trips are displayed
   - Given Paulo has one active trip and several closed trips,
   - When he opens his trip dashboard,
   - Then active trips are listed separately from past trips,
   - And each trip displays its duration, distance and charges when available.

2. Trips can be filtered
   - Given trips exist on different dates and hubs,
   - When Paulo filters by period or hub,
   - Then only the matching trips are displayed.

3. Charge details are available
   - Given a trip includes a deposit or overtime charge,
   - When Paulo opens its details,
   - Then the deposit, overtime and total charge are displayed separately.

4. Users can only access their own trips
   - Given Paulo is signed in,
   - When he opens his trip history,
   - Then only trips associated with his account are displayed.

---

## Epic06: Payments (Deposits and Overtime Charges)

### US-13: Charge deposits and overtime through a test adapter

Epic: Epic06 - Persona: Francisca (Municipal Administrator) - Priority: Must - Scenario: SC-13

As a municipal administrator, I want to configure deposits and overtime charges for my municipality and have them processed through a test payment adapter, so that usage costs are traceable without involving real money.

**Acceptance criteria**

1. Municipal payment values can be configured
   - Given Francisca is authenticated as a municipal administrator,
   - When she configures deposit and overtime values,
   - Then the values are stored for her municipality,
   - And they do not affect other municipalities.

2. A deposit is charged to the team's account
   - Given a reservation or immediate use starts for a worker belonging to Francisca's municipality,
   - When the payment step runs,
   - Then the configured deposit is registered against the worker's team account through the test adapter,
   - And the record identifies the worker, team and vehicle.

3. Deposit release and overtime
   - Given a trip is closed at an authorized hub,
   - When the payment step runs,
   - Then the deposit is released,
   - And any overtime beyond the municipality's configured maximum duration is charged to the team's account.

4. The payment adapter is replaceable
   - Given the test payment adapter is replaced by another test implementation,
   - When a deposit or charge is processed,
   - Then the business logic remains unchanged,
   - And no real money is transferred.

### US-14: Consult payment history per team

Epic: Epic06 - Persona: Francisca (Municipal Administrator) - Priority: Should - Scenario: SC-14

As a municipal administrator, I want to consult payment history by team or department within my municipality, so that I can monitor service costs and identify which workers generated each charge.

**Acceptance criteria**

1. Team payment history is displayed
   - Given deposits and overtime charges have been registered for teams in Francisca's municipality,
   - When she opens the payment history,
   - Then each movement displays the team, worker, vehicle, date, type and amount.

2. History can be filtered
   - Given movements exist for different teams and periods,
   - When Francisca filters by team and period,
   - Then only the matching movements are displayed.

3. Test transactions are identifiable
   - Given a movement was processed by the test adapter,
   - When Francisca views its details,
   - Then it is identified as a test transaction with no real monetary effect.

4. Municipal payment data is isolated
   - Given Francisca is consulting payment history,
   - When she opens or filters the records,
   - Then only transactions belonging to her municipality are accessible.

---

## Epic07: Fleet Telemetry Simulator

### US-15: Run the independent telemetry simulator

Epic: Epic07 - Persona: André (Fleet Operator) - Priority: Must - Scenario: SC-15

As a fleet operator, I want an independent simulator service that publishes vehicle sensor data through MQTT, so that the platform can be tested with realistic fleet data and load tests without requiring physical hardware.

**Acceptance criteria**

1. Sensor events are generated
   - Given the simulator is running for a configured set of vehicles,
   - When it publishes telemetry,
   - Then battery-level updates, GPS positions and movement between hubs are emitted,
   - And lock/unlock states and fault events are also emitted.

2. Publication rate and fleet size are configurable
   - Given the publication rate and number of simulated vehicles are configurable,
   - When the simulator is restarted with updated parameters,
   - Then events are published at the configured rate for the configured number of vehicles.

3. The simulator operates independently
   - Given the platform is stopped or restarted,
   - When the simulator is running,
   - Then it continues publishing events through MQTT independently of the application.

4. Municipal vehicle associations are respected
   - Given the simulator generates events for vehicles belonging to different municipalities,
   - When those events are published,
   - Then each event identifies the corresponding vehicle,
   - And the platform can associate it with the correct municipality.

---

## Epic08: Telemetry Integration and Real-Time Monitoring

### US-16: Consume telemetry and display live vehicle status

Epic: Epic08 - Persona: André (Fleet Operator) - Priority: Must - Scenario: SC-16

As a fleet operator, I want the platform to consume telemetry through a replaceable adapter and update the live vehicle view, so that fleet decisions are based on the latest available vehicle information.

**Acceptance criteria**

1. Incoming events update vehicle state
   - Given a telemetry event arrives for a registered vehicle,
   - When the platform consumes it through the telemetry adapter,
   - Then the vehicle state is updated,
   - And the event is stored in the telemetry history.

2. The adapter is replaceable
   - Given the telemetry adapter is replaced by another simulator implementation,
   - When events are published by the new source,
   - Then the business logic remains unchanged,
   - And vehicle states continue to be updated.

3. Live status is visible
   - Given André is viewing the live fleet status,
   - When new telemetry arrives,
   - Then the displayed vehicle information is updated without requiring a manual refresh.

4. Telemetry is associated with the correct municipality
   - Given a telemetry event refers to a registered vehicle,
   - When the event is processed,
   - Then it is associated with the municipality that owns the vehicle,
   - And it cannot modify vehicles belonging to another municipality.

### US-17: Block vehicles with low battery or faults

Epic: Epic08 - Persona: André (Fleet Operator) - Priority: Must - Scenario: SC-17

As a fleet operator, I want low battery levels and vehicle faults to make vehicles unavailable automatically, so that workers cannot reserve vehicles that are not fit for use.

**Acceptance criteria**

1. Low battery blocks reservations
   - Given a vehicle has a battery level above the minimum configured for its municipality,
   - When telemetry reports a level below that minimum,
   - Then the vehicle is flagged as unavailable,
   - And new reservations are blocked.

2. Faults make vehicles unavailable
   - Given a telemetry fault or abnormal condition is received,
   - When the platform processes the event,
   - Then the affected vehicle becomes unavailable,
   - And André is notified.

3. Recovery restores availability
   - Given the battery returns above the configured minimum and no open fault or maintenance restriction remains,
   - When the next telemetry event is processed,
   - Then the vehicle becomes available again,
   - And the change is recorded.

4. Municipal battery rules are respected
   - Given different municipalities have configured different minimum battery levels,
   - When telemetry is processed,
   - Then each vehicle is evaluated against the minimum battery level of its own municipality.

---

## Epic09: Maintenance Management

### US-18: Create and track maintenance tickets

Epic: Epic09 - Persona: André (Fleet Operator) - Priority: Must - Scenario: SC-18

As a fleet operator, I want to create maintenance tickets with a fault type, severity and repair deadline, assign them to technicians and follow their progress, so that vehicle repairs are completed within the agreed time.

**Acceptance criteria**

1. A ticket is created with a deadline
   - Given André is authenticated as a fleet operator,
   - When he registers a vehicle problem with its type and severity,
   - Then a maintenance ticket is created with status open,
   - And a repair deadline is assigned according to the severity and applicable service rules.

2. Assignment and progress are tracked
   - Given an open maintenance ticket exists,
   - When André assigns it to Pedro and its status changes to in progress, resolved and closed,
   - Then each transition is recorded with a timestamp,
   - And the ticket history is associated with the vehicle.

3. Deadline risks generate notifications
   - Given a ticket is not closed and its deadline is approaching or has been exceeded,
   - When the deadline check runs,
   - Then André receives a notification identifying the ticket and its deadline status.

4. Maintenance data is municipality-specific
   - Given André manages vehicles belonging to a municipality,
   - When he creates or consults maintenance tickets,
   - Then he can only access tickets associated with vehicles belonging to that municipality.

### US-19: Report a vehicle problem from the field

Epic: Epic09 - Persona: Paulo (Field Worker) - Priority: Must - Scenario: SC-19

As a field worker, I want to report a vehicle problem with a description and an optional photo, so that the maintenance team knows what needs to be fixed.

**Acceptance criteria**

1. A problem report creates a ticket
   - Given Paulo is using a vehicle or finds a vehicle at a hub,
   - When he submits a description of the problem and optionally a photo,
   - Then a maintenance ticket is created for that vehicle,
   - And the report is linked to Paulo's account.

2. The report is visible to the operator
   - Given a field report has been submitted,
   - When André opens the maintenance queue,
   - Then he sees the description, the photo if provided and the vehicle identification.

3. Incomplete reports are rejected
   - Given Paulo submits a report without a description,
   - When he tries to send it,
   - Then the report is rejected,
   - And the description field is identified as required.

4. Reports are restricted to authorized vehicles
   - Given Paulo is signed in,
   - When he attempts to report a problem for a vehicle outside his municipality,
   - Then the operation is rejected.

### US-20: Close a repair from a phone

Epic: Epic09 - Persona: Pedro (Field Technician) - Priority: Must - Scenario: SC-20

As a field technician, I want to see my assigned tickets and register inspections, repairs and battery replacements on my phone, so that I can arrive prepared and return vehicles to service quickly.

**Acceptance criteria**

1. Assigned work is visible
   - Given Pedro is signed in as a field technician,
   - When he opens his ticket list,
   - Then open tickets assigned to him are sorted by urgency and repair deadline,
   - And each ticket displays the fault, vehicle and hub location.

2. Interventions are recorded
   - Given Pedro has selected an assigned ticket,
   - When he records an inspection, repair or battery replacement,
   - Then the intervention is saved in the ticket and the vehicle's maintenance history.

3. Closing a ticket restores the vehicle when appropriate
   - Given the ticket is the only restriction preventing a vehicle from being used,
   - When Pedro closes it,
   - Then the vehicle becomes available again,
   - And André can see the ticket closure.

4. Empty state is clear
   - Given Pedro has no assigned tickets,
   - When he opens his ticket list,
   - Then a message states that there are no assigned repairs.

5. Technician access is restricted
   - Given Pedro is assigned to a municipality,
   - When he accesses his maintenance queue,
   - Then he only sees tickets assigned to him and belonging to that municipality.

### US-21: Create maintenance tickets automatically from telemetry

Epic: Epic09 - Persona: André (Fleet Operator) - Priority: Must - Scenario: SC-21

As a fleet operator, I want telemetry faults to create maintenance tickets automatically, so that vehicle problems are handled even when no worker reports them manually.

**Acceptance criteria**

1. A fault opens a ticket
   - Given no open ticket exists for a particular vehicle and fault,
   - When a telemetry fault event arrives,
   - Then a maintenance ticket is created with a type, severity and deadline derived from the event,
   - And André is notified.

2. Repeated faults do not create duplicate tickets
   - Given an open ticket already exists for the same vehicle and fault,
   - When another event of the same type arrives,
   - Then no new ticket is created,
   - And the event is added to the existing ticket's history.

3. The vehicle is blocked until the problem is resolved
   - Given an automatic maintenance ticket is open,
   - When a worker tries to reserve the affected vehicle,
   - Then the reservation is rejected,
   - And the vehicle can only become available again when the ticket is closed and no other restriction remains.

4. Tickets are associated with the correct municipality
   - Given a telemetry fault concerns a vehicle belonging to a municipality,
   - When the automatic ticket is created,
   - Then it is associated with that municipality,
   - And only authorized operators from that municipality can manage it.

---

## Epic10: Municipal Rules of Use

### US-22: Configure municipal rules of use

Epic: Epic10 - Persona: Francisca (Municipal Administrator) - Priority: Must - Scenario: SC-22

As a municipal administrator, I want to configure the rules governing vehicle usage, hub capacity and charges in my municipality, so that MovingTrees reflects our specific operational needs and policies.

**Acceptance criteria**

1. Municipal rules can be configured
   - Given Francisca is authenticated as a municipal administrator,
   - When she configures a maximum trip duration, minimum battery level, allowed vehicle types per team or role, hub occupancy limits, deposit and overtime values,
   - Then the rules are stored for her municipality,
   - And they are applied to the relevant operations within that municipality.

2. Rules are enforced
   - Given Paulo's team is not allowed to use a particular vehicle type, or a request exceeds the maximum duration or violates the minimum battery requirement,
   - When Paulo tries to reserve or use the vehicle,
   - Then the operation is rejected,
   - And the violated rule is identified.

3. Rules are applied to the relevant components
   - Given hub limits and payment values have been configured,
   - When hub occupancy or payment operations are processed,
   - Then alerts and amounts reflect the configured values.

4. Changes do not affect previous transactions
   - Given Francisca changes a municipal rule,
   - When a new reservation or payment is created,
   - Then the updated value is applied,
   - And previous reservations and transactions retain the values that applied when they were created.

5. Municipal configurations are independent
   - Given Francisca manages the configuration of her municipality,
   - When she changes a rule,
   - Then the change affects only her municipality,
   - And it does not modify the configuration of any other municipality.

6. Configuration access is restricted
   - Given Francisca is authenticated as a municipal administrator,
   - When she attempts to configure another municipality's rules,
   - Then the operation is rejected.

---

## Epic11: Performance Indicators and Dashboards

### US-23: Consult municipal service indicators

Epic: Epic11 - Persona: Francisca (Municipal Administrator) - Priority: Must - Scenario: SC-23

As a municipal administrator, I want a dashboard showing the performance of the MovingTrees service in my municipality, with filters by period and hub, so that I can evaluate fleet usage and verify whether the service meets our operational targets.

**Acceptance criteria**

1. Municipal service indicators are displayed
   - Given the platform contains fleet, trip and maintenance data for Francisca's municipality,
   - When she opens her municipal dashboard,
   - Then it displays fleet utilization, vehicle availability against the agreed target, trips per day, average repair time and vehicle distribution per hub.

2. Indicators can be filtered
   - Given data exists for several periods and hubs,
   - When Francisca filters by period or hub,
   - Then the dashboard values are recalculated using only data matching her selection.

3. Agreed targets are visible
   - Given an availability target has been configured for Francisca's municipality,
   - When she compares the actual vehicle availability with that target,
   - Then the dashboard indicates whether the target has been met.

4. Municipal data is isolated
   - Given Francisca opens her municipal dashboard,
   - When the indicators are calculated,
   - Then only data belonging to her municipality is included,
   - And she cannot access the operational indicators of other municipalities.

5. Relevant technical indicators are available
   - Given the platform records API performance, error rates and telemetry reception delays associated with municipal operations,
   - When Francisca opens the technical indicators section,
   - Then she can consult the metrics relevant to the service delivered to her municipality,
   - And she cannot access global infrastructure metrics or information belonging exclusively to other municipalities.