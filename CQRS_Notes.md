# Notes about CQRS and Event Sourcing

Separate read- and write-side.

## DDD
- Bounded Context; piece of a bigger system. Every complex system can be decomposed into multiple bounded contexts.
Bounded context has its own (self-contained) domain model. Typically communicate via events.


- Command; originating from user or process manager.
Changes state of system. Imperative. Handled by single recipient. Asynchronous, no return value.

- Event; describes something that has happened in the system, e.g. an executed command.
Received by multiple subscribers.

- Process Manager; Coordinates behavior. Receives events and sends out commands based on simple logic. Implemented as state-machine, no business logic.

- Aggregate; cluster of domain objects treated as single unit. E.g. order and its items. Stored in separate table but
order items cannot exist without order (parent).


## General

Event Sourcing; Event describes the state change of _aggregate_. Event is saved in event store and can be replayed to re-create
the current state.

Use DTO's (Data Transfer Objects) to exchange data with the UI. Let UI get information from the 'read' side and send
commands to the 'write' side.

For sending events (and preventing two-phase commit and transactions) store updates in database. Have separate process that
listens for database changes and then triggers update event. Introduces short latency but more stable in the long run.
Alternative:
Events saved twice. Once for actual state, once as temp record to know which events to send around on the queue.


-> Key feature for the Event Store implementation; guarantee consistency between information stored and events send to other components.





## Storing data

Initially chosen for one database with normalized tables for storing 'write-side' data.
Views used for denormalizing the data for querying.
Keep queries on the read-side simple for fast querying. Structure tables correctly to make simple queries possible.


## Error handling

Distinguish between business faults and errors.
Errors can be re-tried.
Business faults should have pre-determined reaction.

Have version / timestamp in the events so out-of-order and duplication can be detected

Also use versions in read-model. So that when user does an update, the write-side can compare versions and throw
a concurrent modification exception if versions don't align.


## Advantages / disadvantages

- Due to split of system, read and write side can be optimized separately
- Small components which are easy to update / fix
- Many moving parts, makes it hard to debug the system as a whole
- Without transactions, much work needed to make as reliable
- Eventual consistency
