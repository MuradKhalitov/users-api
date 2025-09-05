--liquibase formatted sql
--changeset Murad:002-create-users-table.sql
--preconditions onFail:CONTINUE onError:CONTINUE

CREATE TABLE users (
   uuid                 UUID PRIMARY KEY,
   fio                  VARCHAR(100) NOT NULL,
   phone_number         VARCHAR(20) NOT NULL,
   avatar               VARCHAR(255),
   role_id UUID NOT NULL REFERENCES roles(uuid) ON DELETE CASCADE
);