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

## Introduction

This application is a distributed, micro-service application based on Spring Cloud.

### Beginning

![beginning01](./imgs/beginning01.png)

![beginning01](./imgs/beginning02.png)

![beginning01](./imgs/beginning03.png)

### Group Activities

![group_activities_01](./imgs/group_activities_01.png)

### Group or Personal Task Arrangment

Integrated with [react-big-calendar](https://github.com/jquense/react-big-calendar)

![task_arrangement_01](./imgs/task_arrangement_01.png)

![task_arrangement_02](./imgs/task_arrangement_02.png)

### Notes

Integrated with [Editor.js](https://github.com/codex-team/editor.js)

![note_01](./imgs/note_01.png)

### System Notifications

![system_notifications_01](./imgs/system_notifications_01.png)

### Instant Message Service (Distributed)

![instant_message01](./imgs/instant_message01.png)

![instant_message02](./imgs/instant_message02.png)

### Email Service

System can send email including content like account verification or password reset.
