# campfire
This is an application for people who want to study together.  Grouping, Joining, Planning, Taking Note, Task Arrangement.

## Before Starting

- RabbitMQ enable `rabbitmq_stomp` plugin

### Backend

- Update your MySQL, Redis, RabbitMQ Address.
- Spring Cloud Alibaba Seata is required to be started before starting services.

- `config-center`, modify the `spring.cloud.config.server.native.search-locations` to the location that stores the configuration files.

- `file` service, modify the file storage address in `file-storage`.

### Frontend

- Modify request address in `next.config.js`
- Modify `brokerURL` in `layout.js` at method `initWebSocket`

