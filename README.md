# Introduction

This project is based on the _Ticket Service Coding Challenge_ provided by Walmart Labs. The requirements include:

## Functional Requirements
* Find the number of seats available within the venue (Note: available seats are seats that are neither held nor reserved)
* Find and hold the best available seats on behalf of a customer (Note: each ticket hold should expire within a set number of seconds)
* Reserve and commit a specific group of held seats for a customer

## Design Requirements
* The ticket service implementation should be written in Java
* The solution and tests should build and execute entirely via the command line using either Maven or Gradle as the build tool
* A README file should be included in your submission that documents your assumptions and includes instructions for building the solution and executing the tests
* Implementation mechanisms such as disk-based storage, a REST API, and a front-end GUI are not required
* Implement a provided TicketService interface

# Running The Application
## Build
To build this project, simply navigate to the top level directory and run:

```mvn clean package```

This will build a new artifact into the `target` directory.

## Run

This project builds with a fully-executable `jar`. So simply navigate to the `target` directory and execute the artifact: 

```./ticket-system-0.0.1-SNAPSHOT.jar```

## Test

The tests are run as a part of the `package` command above, however the unit tests can be run apart from the build using:

```mvn clean test```

# Using the Application

## Overview
The ticket system is built to allow a user to view seats, hold seats, promote a hold to a reservation, and cancel a hold
vis the `TicketService`. 
 

## Quick Start (REST API & Postman)
To make this more interactive and demonstrable, I added a REST API which exposes the functionality of the TicketService. 

The easiest way to navigate the API is using Postman. I built a collection of Postman requests that can be found here: https://www.getpostman.com/collections/654e1e10c5bcec73e9e7

### Hold Seats
1. Execute `View Seat Map`. This will show a `String` representation of the venue ('O' is open; 'H' is held; 'R' is reserved).
1. Execute `Request Seats`. This will return a `SeatHold` object showing the current seats you have on hold. Copy the `id`.
1. Execute `View Seat Map` again. You will now see your seats on hold.

### Reserve Seats
1. Follow the instructions above to hold seats
1. Execute `Reserve Seats` with the `id` from step (2). This will return a `SeatReservation` object with a confirmation code. (Note: the `SeatHold` becomes invalid after ~60 seconds unless you override `timeout.hold`.)
1. Execute `View Seat Map` again. You will not see your seats have been reserved (and no longer at risk of being invalidated!). 

### Cancel Hold
1. Follow the instructions above to hold seats
1. Execute `Cancel Seats` with the `id` from the previous step. You will see a confirmation `String`.
1. Execute `View Seat Map` again. You will now see your hold is gone.
1. Execute `Admin - Holds`. You will see your hold is not present in the list.

### Admin
1. Follow instructions for holding and/or reserving seats
1. Execute `Admin - Holds` and `Admin - Reservations` to see the internal holds and reservations.

# Design

## Key Decisions

### Data Storage
I chose to use in-memory storage for this project mostly due to the clause in the requirements mentioning that 
disk-based storage was "not required" (and yes, I did ignore that clause by implementing an API - but I believe an API makes
this project much easier to evaluate). If this were to be an application which was to be used in a production environment,
I would likely (assuming no abnormal use-cases) implement a SQL-based data store to leverage the speed, simplicity, and
transactional nature of SQL.

### Implementation of SeatHold
`SeatHold` is implemented to maintain the relationship between users, seats, and hold metadata. The user is defined by the 
`customerEmail`, the seats defined by the immutable small-object `Seats`, and the hold metadata is the `creationTime` and `id`. 
A `Map` is used to maintain all existing `SeatHold` between user requests.

### Expiring SeatHolds
Once created, a `SeatHold` is expired after a specified amount of time. This has been implemented using a `Timer` and
`TimerTask` which is triggered once per second. Once triggered, `ExpireHoldsTask` iterated through each `SeatHold` within
the internal `Map` and removes any `SeatHold` that was created beyond the configurable expiration time. 

### Configurations
In order to provide more flexibility to the administrator of this application, I added a `ConfigurationProvider` which
reads system variables to configure the venue size and hold expiration time.

| Name        | System Property | Default |
|-------------|-----------------|---------|
| Venue rows  | `venue.rows`    | 9       |
| Venue seats per row  | `venue.seats`    | 33       |
| Hold timeout (sec)  | `timeout.hold`    | 60       |

_Example_:

```./ticket-system-0.0.1-SNAPSHOT.jar -Dvenue.rows=10 -Dvenue.seats=12 -Dtimeout.hold=120```

## Future Improvement

### API
Since the API was added for demonstration purposes, there are many improvements I available including: adding testing, 
usage of a unified response type, proper authentication, and security features.

### Multi-threading
This project is currently single-threaded but was designed for multi-threading. Depending on the expected amount of 
traffic, the project could benefit from a scalable thread pool which would better handle large amounts of traffic while
maintaining data reliability through the existing synchronization.

### Data Storage
Improvements could be made through the implementation of a database to store the holds and reservations instead of the 
current, in-memory storage approach. There are memory limitations to the current implementation that could be avoided
by the use of a disk-based or cloud-based storage solution.

### Seat Preference Option
Currently, the application assumes that everyone prefers to be front and center. An improvement would be to provide 
various "preferences" that the user could provide to select _their_ best seats.

### Direct Seat Selection
Users should have the ability to directly select their seats instead of relying on the application to select their 
seats.