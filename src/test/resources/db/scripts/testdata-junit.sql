INSERT INTO organization (organization_number, created, updated, organization_name, id)
VALUES (1, now(), now(), null, 'cfcb03b1-7344-4352-9b72-7aebb1f235e1'),
       (5535, now(), now(), 'SBK GA Planering', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b');

INSERT INTO checklist(id, organization_id, name, version, role_type, life_cycle, created, updated)
VALUES ('15764278-50c8-4a19-af00-077bfc314fd2', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'Checklista Elnät', 1, 'EMPLOYEE', 'CREATED',
        '2019-01-01 00:00:00',
        '2019-01-01 00:00:00'),
       ('25764278-50c8-4a19-af00-077bfc314fd2', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'Checklista för Vård och omsorg', 1, 'EMPLOYEE',
        'ACTIVE', '2019-01-01 00:00:00',
        '2019-01-01 00:00:00'),
       ('35764278-50c8-4a19-af00-077bfc314fd2', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'Cheflista', 1, 'MANAGER', 'CREATED',
        '2019-01-01 00:00:00',
        '2019-01-01 00:00:00');

INSERT INTO phase(id, name, body_text, time_to_complete, role_type, sort_order, created, updated,
                  checklist_id)
VALUES ('1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Första veckan',
        'Dessa uppgifter ska göras första veckan', 'P1W', 'EMPLOYEE', 1, '2019-01-01 00:00:00',
        '2019-01-01 00:00:00', '15764278-50c8-4a19-af00-077bfc314fd2'),
       ('2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Första veckan',
        'Dessa uppgifter ska göras andra veckan', 'P2W', 'EMPLOYEE', 1, '2019-01-01 00:00:00',
        '2019-01-01 00:00:00', '15764278-50c8-4a19-af00-077bfc314fd2'),
       ('3455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Första veckan',
        'Dessa uppgifter ska göras första månaden', 'P1M', 'EMPLOYEE', 1, '2019-01-01 00:00:00',
        '2019-01-01 00:00:00', '15764278-50c8-4a19-af00-077bfc314fd2');

INSERT INTO task(id, heading, text, role_type, question_type, created, sort_order, updated,
                 phase_id)
VALUES ('aba82aca-f841-4257-baec-d745e3ab78bf', 'Fika', 'Bjud på hembakat fika', 'EMPLOYEE',
        'YES_OR_NO', '2019-01-01 00:00:00', 1,
        '2019-01-01 00:00:00', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('bba82aca-f841-4257-baec-d745e3ab78bf', 'Allergi', 'Har du några allergier?', 'EMPLOYEE',
        'YES_OR_NO_WITH_TEXT', '2019-01-01 00:00:00', 1,
        '2019-01-01 00:00:00', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('cba82aca-f841-4257-baec-d745e3ab78bf', 'Lunch', 'Boka upp social lunch med chefen',
        'EMPLOYEE', 'COMPLETED_OR_NOT_RELEVANT_WITH_TEXT', '2019-01-01 00:00:00', 1,
        '2019-01-01 00:00:00', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14');
        
insert into manager (created, updated, email, first_name, id, last_name, user_name)
values (now(), now(), 'a.manager@5535.com', 'A Man', '02817ff3-632a-4228-9c31-25ad8124568c', 'Ager', 'aman0agr');

insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, user_name, role_type)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'a.employee@5535.com', 
        'A Emp', '1810c9c4-7281-44de-9930-426d9f065f4d', 'Loyee', '02817ff3-632a-4228-9c31-25ad8124568c', 'Cleaner', 'aemp0loyee', 'EMPLOYEE'),
       ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'B.employee@5535.com', 
        'B Emp', '8122705b-e0e6-4055-b301-eba21986e219', 'Loyee', '02817ff3-632a-4228-9c31-25ad8124568c', 'Loather', 'bemp0loyee', 'EMPLOYEE'),
       ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'B.employee@5535.com', 
        'C Emp', 'f0fd9029-d484-477a-8634-b5b7e0291d76', 'Loyee', '02817ff3-632a-4228-9c31-25ad8124568c', 'Bloater', 'cemp0loyee', 'EMPLOYEE');

insert into correspondence (attempts, sent, id, message_id, recipient, communication_channel, correspondence_status)
values (1, now(), 'a1cef4fc-75f8-4dbe-a2d7-8530358c6789', '4c42ded9-773d-4807-bb89-de1faee3e231', 'a.manager@5535.com', 'EMAIL', 'SENT');
        
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, checklist_id, correspondence_id, employee_id, id)
values ('2023-07-01', '2023-10-01', '2023-01-01', true, '2023-01-01 12:00:00.000', '2023-01-01 12:00:00.000', '25764278-50c8-4a19-af00-077bfc314fd2', 'a1cef4fc-75f8-4dbe-a2d7-8530358c6789', 
        '1810c9c4-7281-44de-9930-426d9f065f4d', 'f853e2b1-a144-4305-b05e-ee8d6dc6d005'),
       ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', '25764278-50c8-4a19-af00-077bfc314fd2', null, 
        'f0fd9029-d484-477a-8634-b5b7e0291d76', '223a076f-441d-4a30-b5d0-f2bfd5ab250b'),
       ('2023-07-01', '2023-10-01', '2023-01-01', false,  '2023-01-01 12:00:00.000', '2023-01-01 12:00:00.000', '25764278-50c8-4a19-af00-077bfc314fd2', null, 
        '8122705b-e0e6-4055-b301-eba21986e219', 'f5960058-fad8-4825-85f3-b0fdb518adc5');

