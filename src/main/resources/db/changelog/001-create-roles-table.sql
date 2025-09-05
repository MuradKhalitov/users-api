--liquibase formatted sql
--changeset Murad:001-create-roles-table.sql
--preconditions onFail:CONTINUE onError:CONTINUE

CREATE TABLE IF NOT EXISTS roles (
     uuid UUID        PRIMARY KEY,
     role_name        VARCHAR(50) NOT NULL
);