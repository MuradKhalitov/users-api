--liquibase formatted sql

--changeset Murad:001-create-user-schema
--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'user_schema';

CREATE SCHEMA IF NOT EXISTS user_schema;

--rollback DROP SCHEMA IF EXISTS user_schema CASCADE;
