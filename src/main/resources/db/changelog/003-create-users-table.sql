--liquibase formatted sql
--changeset Murad:003-create-users-table.sql
--preconditions onFail:CONTINUE onError:CONTINUE

CREATE TABLE IF NOT EXISTS user_schema.users (
   uuid                 UUID PRIMARY KEY,
   fio                  VARCHAR(100) NOT NULL,
   phone_number         VARCHAR(20) NOT NULL,
   avatar               VARCHAR(255),
   role_id UUID NOT NULL REFERENCES user_schema.roles(uuid) ON DELETE CASCADE
);