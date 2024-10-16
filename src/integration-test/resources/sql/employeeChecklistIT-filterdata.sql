insert into organization (organization_number, created, updated, organization_name, id)
values (5535, now(), now(), 'Sub organization 5335', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b');

insert into checklist (version, created, updated, id, organization_id, life_cycle, name, role_type)
values (1, now(), now(), 'e20598a4-6b32-459e-8c15-febbd4c5868e',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'ACTIVE', 'Checklist for Sub organization 5335',
        'EMPLOYEE');

insert into manager (created, updated, email, first_name, id, last_name, user_name)
values (now(), now(), 'e.manager@55351.com', 'Chef', '149fa5bb-438d-47cf-ab1c-0664e9951210',
        'Pellesson', 'eman3agr'),
       (now(), now(), 'e.manager@55351.com', 'Kalle', '249fa5bb-438d-47cf-ab1c-0664e9951210',
        'Anka',
        'eman3agr');

insert into employee (start_date, created, updated, organization_id, department_id, email,
                      first_name, id, last_name, manager_id, title, user_name, role_type)
values ('2020-01-01', now(), now(), 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'e.employee@55351.com', 'André',
        'afd69468-bd32-4b84-a3b0-c5e1742a5a34', 'Svensson', '149fa5bb-438d-47cf-ab1c-0664e9951210',
        'Struggler', 'eemp5loyee', 'EMPLOYEE'),
       ('2021-01-01', now(), now(), 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'e.employee@55351.com', 'Andreas',
        'bfd69468-bd32-4b84-a3b0-c5e1742a5a34', 'Persson', '149fa5bb-438d-47cf-ab1c-0664e9951210',
        'Struggler', 'eemp5loyee', 'EMPLOYEE'),
       ('2022-01-01', now(), now(), 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'e.employee@55351.com', 'Andrea',
        'cfd69468-bd32-4b84-a3b0-c5e1742a5a34', 'Andersson', '149fa5bb-438d-47cf-ab1c-0664e9951210',
        'Struggler', 'eemp5loyee', 'EMPLOYEE'),
       ('2023-01-01', now(), now(), 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'e.employee@55351.com', 'Anders',
        'dfd69468-bd32-4b84-a3b0-c5e1742a5a34', 'Persson', '249fa5bb-438d-47cf-ab1c-0664e9951210',
        'Struggler', 'eemp5loyee', 'EMPLOYEE'),
       ('2024-01-01', now(), now(), 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'e.employee@55351.com', 'Lukas',
        'efd69468-bd32-4b84-a3b0-c5e1742a5a34', 'Larsson', '249fa5bb-438d-47cf-ab1c-0664e9951210',
        'Struggler', 'eemp5loyee', 'EMPLOYEE'),
       ('2025-01-01', now(), now(), 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'e.employee@55351.com', 'Lena',
        'ffd69468-bd32-4b84-a3b0-c5e1742a5a34', 'Pettersson',
        '249fa5bb-438d-47cf-ab1c-0664e9951210', 'Struggler', 'eemp5loyee', 'EMPLOYEE'),
       ('2026-01-01', now(), now(), 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'e.employee@55351.com', 'Peter',
        'gfd69468-bd32-4b84-a3b0-c5e1742a5a34', 'Fredriksson',
        '249fa5bb-438d-47cf-ab1c-0664e9951210', 'Struggler', 'eemp5loyee', 'EMPLOYEE');

insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated,
                        checklist_id, correspondence_id, employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000',
        '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL,
        'afd69468-bd32-4b84-a3b0-c5e1742a5a34', 'ada66ff0-d554-4501-a16d-1b0b6e1825c3'),
       ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000',
        '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL,
        'bfd69468-bd32-4b84-a3b0-c5e1742a5a34', 'bda66ff0-d554-4501-a16d-1b0b6e1825c3'),
       ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000',
        '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL,
        'cfd69468-bd32-4b84-a3b0-c5e1742a5a34', 'cda66ff0-d554-4501-a16d-1b0b6e1825c3'),
       ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000',
        '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL,
        'dfd69468-bd32-4b84-a3b0-c5e1742a5a34', 'dda66ff0-d554-4501-a16d-1b0b6e1825c3'),
       ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000',
        '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL,
        'efd69468-bd32-4b84-a3b0-c5e1742a5a34', 'eda66ff0-d554-4501-a16d-1b0b6e1825c3'),
       ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000',
        '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL,
        'ffd69468-bd32-4b84-a3b0-c5e1742a5a34', 'fda66ff0-d554-4501-a16d-1b0b6e1825c3'),
       ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000',
        '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL,
        'gfd69468-bd32-4b84-a3b0-c5e1742a5a34', 'gda66ff0-d554-4501-a16d-1b0b6e1825c3');

insert into delegate (email, id, manager_id, employee_checklist_id, party_id, user_name, first_name,
                      last_name)
values ('delegated.email@noreply.com', 'acfff6b0-d66f-4f09-a77c-7b02979fbe07',
        '149fa5bb-438d-47cf-ab1c-0664e9951210', 'ada66ff0-d554-4501-a16d-1b0b6e1825c3',
        'a33adee5-3f8f-4201-bfc5-f0e5ba8cd54f', 'dele0gate', 'John', 'Doe'),
       ('delegated.email@noreply.com', 'bcfff6b0-d66f-4f09-a77c-7b02979fbe07',
        '149fa5bb-438d-47cf-ab1c-0664e9951210', 'bda66ff0-d554-4501-a16d-1b0b6e1825c3',
        'b33adee5-3f8f-4201-bfc5-f0e5ba8cd54f', 'dele0gate', 'John', 'Doe');