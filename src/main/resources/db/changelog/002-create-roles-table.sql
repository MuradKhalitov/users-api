--liquibase formatted sql
--changeset Murad:002-create-roles-table.sql
--preconditions onFail:CONTINUE onError:CONTINUE

CREATE TABLE IF NOT EXISTS user_schema.roles (
     uuid UUID        PRIMARY KEY,
     role_name        VARCHAR(50) NOT NULL
);