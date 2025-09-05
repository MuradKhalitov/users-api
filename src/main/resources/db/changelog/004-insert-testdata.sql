--liquibase formatted sql
--changeset Murad:004-insert-testdata.sql
--preconditions onFail:CONTINUE onError:CONTINUE

INSERT INTO user_schema.roles (uuid, role_name)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'ROLE_USER'),
    ('22222222-2222-2222-2222-222222222222', 'ROLE_ADMIN');

INSERT INTO user_schema.users (uuid, fio, phone_number, avatar, role_id)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Иван Иванов', '+79001112233', 'https://github.com/MuradKhalitov/avatars/blob/main/1.jpg', '11111111-1111-1111-1111-111111111111'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Анна Смирнова', '+79002223344', 'https://github.com/MuradKhalitov/avatars/blob/main/2.jpeg', '22222222-2222-2222-2222-222222222222');
