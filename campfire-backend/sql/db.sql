create
    database campfire;

CREATE TABLE `undo_log`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT,
    `branch_id`     bigint(20)   NOT NULL,
    `xid`           varchar(100) NOT NULL,
    `context`       varchar(128) NOT NULL,
    `rollback_info` longblob     NOT NULL,
    `log_status`    int(11)      NOT NULL,
    `log_created`   datetime     NOT NULL,
    `log_modified`  datetime     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;

-- seata server
-- -------------------------------- The script used when storeMode is 'db' --------------------------------
-- the table to store GlobalSession data
CREATE TABLE IF NOT EXISTS `global_table`
(
    `xid`                       VARCHAR(128) NOT NULL,
    `transaction_id`            BIGINT,
    `status`                    TINYINT      NOT NULL,
    `application_id`            VARCHAR(32),
    `transaction_service_group` VARCHAR(32),
    `transaction_name`          VARCHAR(128),
    `timeout`                   INT,
    `begin_time`                BIGINT,
    `application_data`          VARCHAR(2000),
    `gmt_create`                DATETIME,
    `gmt_modified`              DATETIME,
    PRIMARY KEY (`xid`),
    KEY `idx_status_gmt_modified` (`status`, `gmt_modified`),
    KEY `idx_transaction_id` (`transaction_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- the table to store BranchSession data
CREATE TABLE IF NOT EXISTS `branch_table`
(
    `branch_id`         BIGINT       NOT NULL,
    `xid`               VARCHAR(128) NOT NULL,
    `transaction_id`    BIGINT,
    `resource_group_id` VARCHAR(32),
    `resource_id`       VARCHAR(256),
    `branch_type`       VARCHAR(8),
    `status`            TINYINT,
    `client_id`         VARCHAR(64),
    `application_data`  VARCHAR(2000),
    `gmt_create`        DATETIME(6),
    `gmt_modified`      DATETIME(6),
    PRIMARY KEY (`branch_id`),
    KEY `idx_xid` (`xid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- the table to store lock data
CREATE TABLE IF NOT EXISTS `lock_table`
(
    `row_key`        VARCHAR(128) NOT NULL,
    `xid`            VARCHAR(128),
    `transaction_id` BIGINT,
    `branch_id`      BIGINT       NOT NULL,
    `resource_id`    VARCHAR(256),
    `table_name`     VARCHAR(32),
    `pk`             VARCHAR(36),
    `status`         TINYINT      NOT NULL DEFAULT '0' COMMENT '0:locked ,1:rollbacking',
    `gmt_create`     DATETIME,
    `gmt_modified`   DATETIME,
    PRIMARY KEY (`row_key`),
    KEY `idx_status` (`status`),
    KEY `idx_branch_id` (`branch_id`),
    KEY `idx_xid` (`xid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `distributed_lock`
(
    `lock_key`   CHAR(20)    NOT NULL,
    `lock_value` VARCHAR(20) NOT NULL,
    `expire`     BIGINT,
    primary key (`lock_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO `distributed_lock` (lock_key, lock_value, expire)
VALUES ('AsyncCommitting', ' ', 0);
INSERT INTO `distributed_lock` (lock_key, lock_value, expire)
VALUES ('RetryCommitting', ' ', 0);
INSERT INTO `distributed_lock` (lock_key, lock_value, expire)
VALUES ('RetryRollbacking', ' ', 0);
INSERT INTO `distributed_lock` (lock_key, lock_value, expire)
VALUES ('TxTimeoutCheck', ' ', 0);

create table account
(
    id           bigint primary key,
    username     varchar(64) unique                        not null,
    password     varchar(64)                               not null,
    email        varchar(128)                              not null,
    status       enum ('UNVERIFIED', 'VERIFIED', 'BANNED') not null default 'UNVERIFIED',
    avatar_url   varchar(256),
    description  varchar(1024),
    created_time timestamp                                 not null default CURRENT_TIMESTAMP
);

-- plain password: 123qweasd
insert into account(id, username, password, email, status)
    value (1683955606413684736, 'defaultRoot', '$2a$10$Rl3FW0MAxCvI2rlbLdtKQunFtLHmKgWVTfbsjcdwe9jua3H0JsRde',
           'default_root@campfire.com', 'VERIFIED');

create table role
(
    id           bigint primary key,
    name         varchar(64) unique not null,
    description  varchar(1024)      not null,
    created_time timestamp          not null default CURRENT_TIMESTAMP
);

insert into role(id, name, description) value (1682146237510324224, 'admin', 'manage the whole system');
insert into role(id, name, description) value (1682146416003125248, 'regular_user', 'regular user');

create table account_role
(
    account_id bigint not null,
    role_id    bigint not null,
    constraint UK_ACCOUNT_ROLE unique key (account_id, role_id)
);

insert into account_role value (1683955606413684736, 1682146237510324224);

create table contact
(
    id              bigint primary key,
    source_id       bigint                                      not null,
    source_username varchar(64)                                 not null,
    target_id       bigint                                      not null,
    target_username varchar(64)                                 not null,
    comment         varchar(2048)                               not null,
    status          enum ('WAITING', 'ACCEPT', 'DENY', 'BREAK') not null,
    created_time    timestamp                                   not null default CURRENT_TIMESTAMP
);

create table `group`
(
    id           bigint primary key,
    creator_id   bigint                                 not null,
    name         varchar(64)                            not null,
    description  varchar(4096)                          not null,
    status       enum ('ACTIVE', 'INACTIVE', 'DISMISS') not null default 'ACTIVE',
    created_time timestamp                              not null default CURRENT_TIMESTAMP
);

create table joining
(
    id         bigint primary key,
    account_id bigint                        not null,
    username   varchar(64)                   not null,
    group_id   bigint                        not null,
    role       enum ('LEADER', 'MEMBER')     not null,
    status     enum ('WAITING', 'IN', 'OUT') not null default 'IN',
    comment    varchar(2048),
    join_time  timestamp                              default CURRENT_TIMESTAMP,
    constraint UK_JOINING unique key (account_id, group_id)
);

create table post
(
    id           bigint primary key,
    creator_id   bigint                         not null,
    group_id     bigint                         not null,
    username     varchar(64)                    not null,
    title        varchar(512)                   not null,
    content      text(65535)                    not null,
    status       enum ('OPEN', 'CLOSE', 'HIDE') not null default 'OPEN',
    created_time timestamp                      not null default CURRENT_TIMESTAMP
);

create table note
(
    id           bigint primary key,
    creator_id   bigint                     not null,
    title        varchar(512)               not null,
    content      text(65535)                not null,
    visibility   enum ('PUBLIC', 'PRIVATE') not null default 'PRIVATE',
    updated_time timestamp                  not null default CURRENT_TIMESTAMP
);

create table comment
(
    id           bigint primary key,
    creator_id   bigint                not null,
    username     varchar(64)           not null,
    target_id    bigint                not null,
    target_type  enum ('POST', 'NOTE') not null,
    content      text(65535)           not null,
    status       enum ('OPEN', 'HIDE') not null default 'OPEN',
    created_time timestamp             not null default CURRENT_TIMESTAMP
);

create table task
(
    id           bigint primary key,
    creator_id   bigint                   not null,
    owner_type   enum ('GROUP', 'PERSON') not null,
    owner_id     bigint                   not null,
    title        varchar(512)             not null,
    content      text(65535)              not null,
    start_date   date                     not null,
    start_time   time                     not null,
    end_date     date                     not null,
    end_time     time                     not null,
    created_time timestamp                not null default CURRENT_TIMESTAMP
);

create table task_completion
(
    task_id        bigint      not null,
    account_id     bigint      not null,
    username       varchar(64) not null,
    comment        varchar(2048),
    completed_time timestamp   not null default CURRENT_TIMESTAMP,
    constraint UK_TASK_COMPLETION unique key (task_id, account_id)
);

create table group_directory
(
    id               bigint primary key,
    group_id         bigint unique             not null,
    max_size         varchar(16)               not null default '67108864',
    available_size   varchar(16)               not null default '67108864',
    upload_open_role enum ('LEADER', 'MEMBER') not null default 'LEADER'
);

create table group_file
(
    id                 bigint primary key,
    creator_id         bigint                  not null,
    username           varchar(64)             not null,
    group_directory_id bigint                  not null,
    display_name       varchar(128)            not null,
    filename           varchar(128)            not null,
    `size`             varchar(16)             not null,
    status             enum ('OPEN', 'FROZEN') not null default 'OPEN',
    created_time       timestamp               not null default CURRENT_TIMESTAMP
);

create table notification
(
    id           bigint primary key,
    title        varchar(512)               not null,
    content      text(65535)                not null,
    target_type  enum ('PUBLIC', 'PRIVATE') not null default 'PUBLIC',
    created_time timestamp                  not null default CURRENT_TIMESTAMP
);

create table account_notification
(
    notification_id bigint                  not null,
    target_id       bigint                  not null,
    read_status     enum ('READ', 'UNREAD') not null default 'UNREAD',
    constraint UK_ACCOUNT_NOTIFICATION unique key (notification_id, target_id)
);

create table group_chat
(
    id           bigint primary key,
    group_id     bigint unique not null,
    group_name   varchar(64)   not null,
    created_time timestamp     not null default CURRENT_TIMESTAMP
);

create table account_group_chat
(
    group_chat_id  bigint      not null,
    account_id     bigint      not null,
    username       varchar(64) not null,
    last_read_time timestamp   not null default CURRENT_TIMESTAMP,
    constraint UK_ACCOUNT_GROUP_CHAT unique key (group_chat_id, account_id)
);

create table group_message
(
    id            bigint primary key,
    group_chat_id bigint        not null,
    creator_id    bigint        not null,
    content       varchar(2048) not null,
    created_time  timestamp     not null default CURRENT_TIMESTAMP
);

create table contact_chat
(
    id           bigint primary key,
    created_time timestamp not null default CURRENT_TIMESTAMP
);

create table account_contact_chat
(
    contact_chat_id bigint      not null,
    account_id      bigint      not null,
    username        varchar(64) not null,
    last_read_time  timestamp   not null default CURRENT_TIMESTAMP,
    constraint UK_ACCOUNT_CONTACT_CHAT unique key (contact_chat_id, account_id)
);

create table contact_message
(
    id              bigint primary key,
    contact_chat_id bigint        not null,
    creator_id      bigint        not null,
    content         varchar(2048) not null,
    created_time    timestamp     not null default CURRENT_TIMESTAMP
);



