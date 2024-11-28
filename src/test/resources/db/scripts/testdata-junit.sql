INSERT INTO organization(municipality_id, organization_number, created, updated, organization_name, id)
VALUES ('2281', 1, now(), now(), NULL, 'cfcb03b1-7344-4352-9b72-7aebb1f235e1'),
       ('2281', 5535, now(), now(), 'SBK GA Planering', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b');


INSERT INTO checklist(municipality_id, id, organization_id, name, VERSION, life_cycle, created, updated, last_saved_by)
VALUES ('2281', '15764278-50c8-4a19-af00-077bfc314fd2', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'Checklista Elnät', 1, 'CREATED', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '25764278-50c8-4a19-af00-077bfc314fd2', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'Checklista för Vård och omsorg', 1, 'ACTIVE', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '35764278-50c8-4a19-af00-077bfc314fd2', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'Checklista som ska tas bort', 1, 'ACTIVE', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser');


INSERT INTO phase(municipality_id, id, name, body_text, time_to_complete, sort_order, created, updated, last_saved_by)
VALUES ('2281', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Första veckan', 'Dessa uppgifter ska göras första veckan', 'P1W', 1, '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Andra veckan', 'Dessa uppgifter ska göras andra veckan', 'P2W', 1, '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '3455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Första månaden', 'Dessa uppgifter ska göras första månaden', 'P1M', 1, '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser');


INSERT INTO task(id, heading, text, role_type, question_type, created, sort_order, updated, last_saved_by, checklist_id, phase_id)
VALUES ('aba82aca-f841-4257-baec-d745e3ab78bf', 'Fika', 'Bjud på hembakat fika', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '15764278-50c8-4a19-af00-077bfc314fd2', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('bba82aca-f841-4257-baec-d745e3ab78bf', 'Allergi', 'Har du några allergier?', 'NEW_EMPLOYEE', 'YES_OR_NO_WITH_TEXT', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '15764278-50c8-4a19-af00-077bfc314fd2', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('cba82aca-f841-4257-baec-d745e3ab78bf', 'Lunch', 'Boka upp social lunch med chefen', 'NEW_EMPLOYEE', 'COMPLETED_OR_NOT_RELEVANT_WITH_TEXT', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '15764278-50c8-4a19-af00-077bfc314fd2', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14');


INSERT INTO manager (created, updated, email, first_name, id, last_name, username)
VALUES (now(), now(), 'a.manager@5535.com', 'A Man', '02817ff3-632a-4228-9c31-25ad8124568c', 'Ager', 'aman0agr');


INSERT INTO employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, username, employment_position)
VALUES ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'a.employee@5535.com', 'A Emp', '1810c9c4-7281-44de-9930-426d9f065f4d', 'Loyee', '02817ff3-632a-4228-9c31-25ad8124568c', 'Cleaner', 'aemp0loyee', 'EMPLOYEE'),
       ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'B.employee@5535.com', 'B Emp', '8122705b-e0e6-4055-b301-eba21986e219', 'Loyee', '02817ff3-632a-4228-9c31-25ad8124568c', 'Loather', 'bemp0loyee', 'EMPLOYEE'),
       ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'B.employee@5535.com', 'C Emp', 'f0fd9029-d484-477a-8634-b5b7e0291d76', 'Loyee', '02817ff3-632a-4228-9c31-25ad8124568c', 'Bloater', 'cemp0loyee', 'EMPLOYEE');


INSERT INTO correspondence (attempts, sent, id, message_id, recipient, communication_channel, correspondence_status)
VALUES (1, now(), 'a1cef4fc-75f8-4dbe-a2d7-8530358c6789', '4c42ded9-773d-4807-bb89-de1faee3e231', 'a.manager@5535.com', 'EMAIL', 'SENT');


INSERT INTO employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id, employee_id, id)
VALUES ('2023-07-01', '2023-10-01', '2023-01-01', TRUE, '2023-01-01 12:00:00.000', '2023-01-01 12:00:00.000', 'a1cef4fc-75f8-4dbe-a2d7-8530358c6789', '1810c9c4-7281-44de-9930-426d9f065f4d', 'f853e2b1-a144-4305-b05e-ee8d6dc6d005'),
       ('2024-07-01', '2024-10-01', '2024-01-01', FALSE, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL, 'f0fd9029-d484-477a-8634-b5b7e0291d76', '223a076f-441d-4a30-b5d0-f2bfd5ab250b'),
       ('2023-07-01', '2023-10-01', '2023-01-01', FALSE, '2023-01-01 12:00:00.000', '2023-01-01 12:00:00.000', NULL, '8122705b-e0e6-4055-b301-eba21986e219', 'f5960058-fad8-4825-85f3-b0fdb518adc5');

INSERT INTO referred_checklist (employee_checklist_id, checklist_id)
VALUES ('f853e2b1-a144-4305-b05e-ee8d6dc6d005', '25764278-50c8-4a19-af00-077bfc314fd2'),
       ('223a076f-441d-4a30-b5d0-f2bfd5ab250b', '25764278-50c8-4a19-af00-077bfc314fd2'),
       ('f5960058-fad8-4825-85f3-b0fdb518adc5', '25764278-50c8-4a19-af00-077bfc314fd2');

INSERT INTO custom_task (sort_order, created, updated, last_saved_by, heading, id, employee_checklist_id, phase_id, `text`, question_type, role_type)
VALUES (0, '2024-01-03 12:00:00.000', '2024-01-03 12:00:00.000', 'someUser', 'Custom employee task', '1b3bfe66-0e6c-4e92-a410-7c620a5461f4', 'f853e2b1-a144-4305-b05e-ee8d6dc6d005', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Descriptive text for custom task', 'YES_OR_NO', 'NEW_EMPLOYEE');

INSERT INTO custom_sortorder (id, municipality_id, organization_number, component_type, component_id, position)
VALUES -- organizational unit 13
('01ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 13, 'PHASE', 'b3d3c566-527b-44b5-aa7b-ff2b52a8a920', 1),
('02ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 13, 'PHASE', 'c69fd0f9-b5d3-44c7-be0d-13b2fa254dd5', 2),
('03ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 13, 'TASK', '1bc21aff-af4e-4cdb-ac4d-c8b8c0b6ee76', 1),
('04ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 13, 'TASK', '98dc80a9-7395-400b-8570-1bdeb5e38195', 2),
('05ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 13, 'TASK', '7c49e4f0-6fc0-433c-89df-ac1460a64bd7', 3),
('06ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 13, 'TASK', '7121d85d-6eee-49b4-8f1d-db1e165a5c29', 1),
-- organizational unit 14
('14ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 14, 'TASK', '7121d85d-6eee-49b4-8f1d-db1e165a5c29', 1),
-- organizational unit 578
('07ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'PHASE', 'b3d3c566-527b-44b5-aa7b-ff2b52a8a920', 2),
('08ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'PHASE', 'c69fd0f9-b5d3-44c7-be0d-13b2fa254dd5', 1),
('09ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'TASK', '1bc21aff-af4e-4cdb-ac4d-c8b8c0b6ee76', 3),
('10ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'TASK', '98dc80a9-7395-400b-8570-1bdeb5e38195', 1),
('11ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'TASK', '7c49e4f0-6fc0-433c-89df-ac1460a64bd7', 2),
('12ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'TASK', 'ee5d73c0-db96-4a31-a8eb-2b5129f7428c', 1),
('13ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'TASK', '7121d85d-6eee-49b4-8f1d-db1e165a5c29', 2);
