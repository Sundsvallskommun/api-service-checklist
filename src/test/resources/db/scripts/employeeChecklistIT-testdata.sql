-- ======================================================================================
-- Common data used in all tests
-- ======================================================================================
-- --------------------------------------------------------------------------------------
-- Phases (common for all checklists)
-- --------------------------------------------------------------------------------------
insert into phase (municipality_id, sort_order, created, updated, last_saved_by, body_text, id, name, time_to_complete)
values ('2281', 1, now(), now(), 'someUser', 'Description of phase A', '7272d1fc-540e-4394-afe2-e133ca642e91',
        'Phasename A', 'P1M');

insert into phase (municipality_id, sort_order, created, updated, last_saved_by, body_text, id, name, time_to_complete)
values ('2281', 2, now(), now(), 'someUser', 'Description of phase B', '3e9780a7-96f3-4d07-80ee-a9634b786a38',
        'Phasename B', 'P1M');

insert into phase (municipality_id, sort_order, created, updated, last_saved_by, body_text, id, name, time_to_complete)
values ('2281', 1, now(), now(), 'someUser', 'Description of phase C', '539b074d-d654-49ec-9dce-220f8a5ba7bb',
        'Phasename C', 'P1M');

insert into phase (municipality_id, sort_order, created, updated, last_saved_by, body_text, id, name, time_to_complete)
values ('2281', 2, now(), now(), 'someUser', 'Description of phase D', 'd2c92810-3c46-4161-af54-7ae8b3c9d0b3',
        'Phasename D', 'P1M');

-- --------------------------------------------------------------------------------------
-- Checklist for organizationNumber 5335 (Sub organization 5335)
-- --------------------------------------------------------------------------------------
insert into organization (municipality_id, organization_number, created, updated, organization_name, id)
values ('2281', 5535, now(), now(), 'Sub organization 5335', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b');

insert into checklist (municipality_id, version, created, updated, last_saved_by, id, organization_id, life_cycle, name)
values ('2281', 1, now(), now(), 'someUser', 'e20598a4-6b32-459e-8c15-febbd4c5868e',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'ACTIVE', 'Checklist for Sub organization 5335');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, checklist_id,
                  phase_id,
                  text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (1, 'YES_OR_NO', now(), now(), 'someUser', 'Description of manager task', 'e43fdac2-331e-4808-8d83-753a59e329cd',
        'MANAGER_FOR_NEW_EMPLOYEE', 'e20598a4-6b32-459e-8c15-febbd4c5868e', '7272d1fc-540e-4394-afe2-e133ca642e91',
        'Text for manager task');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, heading_reference, id, role_type,
                  checklist_id,
                  phase_id,
                  text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (1, 'YES_OR_NO_WITH_TEXT', now(), now(), 'someUser', 'Description of first employee task',
        'http://www.address-to-click.web', 'd250a20c-a616-4147-bfe0-19a0d12f3df0', 'NEW_EMPLOYEE',
        'e20598a4-6b32-459e-8c15-febbd4c5868e',
        '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Text for first employee task');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, checklist_id,
                  phase_id,
                  text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (2, 'YES_OR_NO', now(), now(), 'someUser', 'Description of second employee task',
        '0c8b99e9-718b-4c92-9ba3-a49dc29d48b5', 'NEW_EMPLOYEE', 'e20598a4-6b32-459e-8c15-febbd4c5868e',
        '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Text for second employee task');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, checklist_id,
                  phase_id,
                  text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (3, 'YES_OR_NO', now(), now(), 'someUser', 'Description of manager task', '056423aa-01a4-4243-ace4-561a6e4cd25f',
        'MANAGER_FOR_NEW_EMPLOYEE', 'e20598a4-6b32-459e-8c15-febbd4c5868e', '3e9780a7-96f3-4d07-80ee-a9634b786a38',
        'Text for manager task');

insert into custom_sortorder (id, municipality_id, organization_number, component_type, component_id, position)
values ('01ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'PHASE', '7272d1fc-540e-4394-afe2-e133ca642e91', 4),
       ('02ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'PHASE', '3e9780a7-96f3-4d07-80ee-a9634b786a38', 3),
       ('03ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'PHASE', '539b074d-d654-49ec-9dce-220f8a5ba7bb', 2),
       ('04ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'PHASE', 'd2c92810-3c46-4161-af54-7ae8b3c9d0b3', 1),
       ('05ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'TASK', 'e43fdac2-331e-4808-8d83-753a59e329cd', 4),
       ('06ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'TASK', 'd250a20c-a616-4147-bfe0-19a0d12f3df0', 3),
       ('07ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'TASK', '0c8b99e9-718b-4c92-9ba3-a49dc29d48b5', 2),
       ('08ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'TASK', '056423aa-01a4-4243-ace4-561a6e4cd25f', 1),
       -- Tasks below is managed by organization 1 (root organization)
       ('09ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'TASK', 'c8c7abe0-5703-4ca1-86d3-74e5ad79b690', 4),
       ('10ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'TASK', 'a76e9920-261f-42fc-9077-14fb1cdb9871', 3),
       ('11ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'TASK', '0da1dfa2-5196-45c2-b605-162a323b9b5e', 2),
       ('12ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 5535, 'TASK', 'e930c70d-a961-4b71-89b4-935d47db982f', 1);

-- --------------------------------------------------------------------------------------
-- Checklist for organizationNumber 5336 (Sub organization 5336) that has no explicit custom sort 
-- --------------------------------------------------------------------------------------
insert into organization (municipality_id, organization_number, created, updated, organization_name, id)
values ('2281', 5536, now(), now(), 'Sub organization 5336', '11ecf84e-cb11-426f-acd9-081330f28f7c');

insert into checklist (municipality_id, version, created, updated, last_saved_by, id, organization_id, life_cycle, name)
values ('2281', 1, now(), now(), 'someUser', '68849473-59aa-4ae6-b4bd-7f046e857984',
        '11ecf84e-cb11-426f-acd9-081330f28f7c', 'ACTIVE', 'Checklist for Sub organization 5336');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, heading_reference, id, role_type,
                  checklist_id,
                  phase_id,
                  text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (9, 'YES_OR_NO', now(), now(), 'someUser', 'Description of task for dept 5536',
        'http://www.address-to-click-for-5536-task.web', '368c4c0b-ccb9-443b-9e38-9da52f0b4b95', 'NEW_EMPLOYEE',
        '68849473-59aa-4ae6-b4bd-7f046e857984',
        'd2c92810-3c46-4161-af54-7ae8b3c9d0b3', 'Text for dept 5536 task');

-- --------------------------------------------------------------------------------------
-- Checklist for organizationNumber 1 (Root organization)
-- --------------------------------------------------------------------------------------
insert into organization (municipality_id, organization_number, created, updated, organization_name, id)
values ('2281', 1, now(), now(), null, 'cfcb03b1-7344-4352-9b72-7aebb1f235e1');

insert into checklist (municipality_id, version, created, updated, last_saved_by, id, organization_id, life_cycle, name)
values ('2281', 1, now(), now(), 'someUser', '8c66e24b-3845-47ae-af74-c4611db8be7c',
        'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'ACTIVE', 'Checklist for Root organization');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, checklist_id,
                  phase_id,
                  text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (1, 'COMPLETED_OR_NOT_RELEVANT', now(), now(), 'someUser', 'Description of manager to new employee task',
        'c8c7abe0-5703-4ca1-86d3-74e5ad79b690', 'MANAGER_FOR_NEW_EMPLOYEE', '8c66e24b-3845-47ae-af74-c4611db8be7c',
        '539b074d-d654-49ec-9dce-220f8a5ba7bb', 'Text for task to be performed by new employees manager');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, checklist_id,
                  phase_id,
                  text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (1, 'COMPLETED_OR_NOT_RELEVANT_WITH_TEXT', now(), now(), 'someUser', 'Description of new employee task',
        'a76e9920-261f-42fc-9077-14fb1cdb9871', 'NEW_EMPLOYEE', '8c66e24b-3845-47ae-af74-c4611db8be7c',
        'd2c92810-3c46-4161-af54-7ae8b3c9d0b3', 'Text for task to be performed by new employee');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, checklist_id,
                  phase_id,
                  text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (1, 'YES_OR_NO', now(), now(), 'someUser', 'Description of new manager task',
        '0da1dfa2-5196-45c2-b605-162a323b9b5e', 'NEW_MANAGER', '8c66e24b-3845-47ae-af74-c4611db8be7c',
        '539b074d-d654-49ec-9dce-220f8a5ba7bb', 'Text for task to be performed by new manager');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, heading_reference, id, role_type,
                  checklist_id,
                  phase_id,
                  text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (2, 'YES_OR_NO', now(), now(), 'someUser', 'Description of managers manager task',
        'http://www.manager-manager-task-address.web', 'e930c70d-a961-4b71-89b4-935d47db982f',
        'MANAGER_FOR_NEW_MANAGER', '8c66e24b-3845-47ae-af74-c4611db8be7c',
        'd2c92810-3c46-4161-af54-7ae8b3c9d0b3', 'Text for task to be performed by new managers manager');

insert into custom_sortorder (id, municipality_id, organization_number, component_type, component_id, position)
values ('21ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 1, 'PHASE', '7272d1fc-540e-4394-afe2-e133ca642e91', 1),
       ('22ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 1, 'PHASE', '3e9780a7-96f3-4d07-80ee-a9634b786a38', 4),
       ('23ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 1, 'PHASE', '539b074d-d654-49ec-9dce-220f8a5ba7bb', 2),
       ('24ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 1, 'PHASE', 'd2c92810-3c46-4161-af54-7ae8b3c9d0b3', 3),
       ('25ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 1, 'TASK', 'c8c7abe0-5703-4ca1-86d3-74e5ad79b690', 2),
       ('26ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 1, 'TASK', 'a76e9920-261f-42fc-9077-14fb1cdb9871', 5),
       ('27ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 1, 'TASK', '0da1dfa2-5196-45c2-b605-162a323b9b5e', 3),
       ('28ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 1, 'TASK', 'e930c70d-a961-4b71-89b4-935d47db982f', 4);

-- ======================================================================================
-- Employee A and B, Manager A and C are used in testcases test01, test03
-- Employee B is used in test08
-- ======================================================================================
-- --------------------------------------------------------------------------------------
-- Checklist for employee A working for sub organization 5535
-- --------------------------------------------------------------------------------------

-- Manager
insert into manager (created, updated, email, first_name, id, last_name, username)
values (now(), now(), 'a.manager@5535.com', 'A Man', '02817ff3-632a-4228-9c31-25ad8124568c', 'Ager', 'aman0agr');

-- Employee
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'a.employee@5535.com', 'A Emp', '1810c9c4-7281-44de-9930-426d9f065f4d', 'Loyee',
        '02817ff3-632a-4228-9c31-25ad8124568c', 'Cleaner', 'aemp0loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        '1810c9c4-7281-44de-9930-426d9f065f4d', 'f853e2b1-a144-4305-b05e-ee8d6dc6d005');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('f853e2b1-a144-4305-b05e-ee8d6dc6d005', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('f853e2b1-a144-4305-b05e-ee8d6dc6d005', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Fulfilment
insert into fulfilment (updated, last_saved_by, id, employee_checklist_id, response_text, task_id, completed)
values ('2024-01-02 12:00:00.000', 'aemp0loyee', '34e076da-8694-4cb9-be1a-814212801686',
        'f853e2b1-a144-4305-b05e-ee8d6dc6d005', 'Response for employee task', 'd250a20c-a616-4147-bfe0-19a0d12f3df0',
        'TRUE');

-- --------------------------------------------------------------------------------------
-- Checklist for employee B working for sub organization 5535
-- --------------------------------------------------------------------------------------

-- Employee
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-06', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'b.employee@5535.com', 'B Emp', '8122705b-e0e6-4055-b301-eba21986e219', 'Loyee',
        '02817ff3-632a-4228-9c31-25ad8124568c', 'Loather', 'bemp0loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2024-07-06', '2024-10-06', '2024-01-06', false, '2024-01-06 12:00:00.000', '2024-01-06 12:00:00.000', NULL,
        '8122705b-e0e6-4055-b301-eba21986e219', 'f5960058-fad8-4825-85f3-b0fdb518adc5');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('f5960058-fad8-4825-85f3-b0fdb518adc5', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('f5960058-fad8-4825-85f3-b0fdb518adc5', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Custom task
insert into custom_task (sort_order, created, updated, last_saved_by, heading, id, employee_checklist_id, phase_id,
                         `text`, question_type, role_type)
values (0, '2024-01-03 12:00:00.000', '2024-01-03 12:00:00.000', 'someUser', 'Custom employee task',
        '1b3bfe66-0e6c-4e92-a410-7c620a5461f4', 'f5960058-fad8-4825-85f3-b0fdb518adc5',
        '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Descriptive text for custom task', 'YES_OR_NO', 'NEW_EMPLOYEE');

-- Custom fulfilment
insert into custom_fulfilment (updated, last_saved_by, custom_task_id, id, employee_checklist_id, response_text,
                               completed)
VALUES ('2024-01-03 12:00:00.000', 'aman0agr', '1b3bfe66-0e6c-4e92-a410-7c620a5461f4',
        'a5d19134-d21f-4965-bd25-44a123e94ee1', 'f5960058-fad8-4825-85f3-b0fdb518adc5', NULL, 'FALSE');

-- --------------------------------------------------------------------------------------
-- Checklist for manager C working for sub organization 55351
-- --------------------------------------------------------------------------------------

insert into organization (municipality_id, organization_number, created, updated, organization_name, id)
values ('2281', 55351, now(), now(), 'Sub organization 55351', '176eb3d9-ebbc-4951-9c8a-e4503f003f79');

-- Employee
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-06', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', '176eb3d9-ebbc-4951-9c8a-e4503f003f79',
        'c.manager@55351.com', 'C Newman', 'ae93a63e-d975-4cd9-8e28-4cc9ea8b4d96', 'Ager',
        '02817ff3-632a-4228-9c31-25ad8124568c', 'New manager', 'cman1agr', 'MANAGER');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2024-07-06', '2024-10-06', '2024-01-06', false, '2024-01-06 12:00:00.000', '2024-01-06 12:00:00.000', NULL,
        'ae93a63e-d975-4cd9-8e28-4cc9ea8b4d96', '2e2bb099-560c-450e-8f20-764498a37983');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('2e2bb099-560c-450e-8f20-764498a37983', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('2e2bb099-560c-450e-8f20-764498a37983', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Fulfilments
insert into fulfilment (updated, last_saved_by, id, employee_checklist_id, response_text, task_id, completed)
values ('2024-01-07 12:00:00.000', 'aman0agr', '6b17e790-f22c-46fe-8bff-f58af8cbab6a',
        '2e2bb099-560c-450e-8f20-764498a37983', 'Response on employee task performed by manager to new manager',
        'c8c7abe0-5703-4ca1-86d3-74e5ad79b690', 'TRUE'),
       ('2024-01-07 12:00:00.000', 'cman1agr', 'c44c6fac-2875-468c-9112-e0a5a2646cb6',
        '2e2bb099-560c-450e-8f20-764498a37983', 'Response on employee task performed by new manager',
        'a76e9920-261f-42fc-9077-14fb1cdb9871', 'TRUE'),
       ('2024-01-07 12:00:00.000', 'aman0agr', 'c44c6fac-2875-468c-1244-e0a5a2646cc6',
        '2e2bb099-560c-450e-8f20-764498a37983', 'Response on manager task performed by manager to new manager',
        'e930c70d-a961-4b71-89b4-935d47db982f', 'TRUE'),
       ('2024-01-07 12:00:00.000', 'cman1agr', 'c44c6fac-2875-468c-5678-e0a5a2646cd6',
        '2e2bb099-560c-450e-8f20-764498a37983', 'Response on manager task performed by new manager',
        '0da1dfa2-5196-45c2-b605-162a323b9b5e', 'TRUE');

-- ======================================================================================
-- Employee C, Manager C and D are used in testcase test02
-- ======================================================================================
-- --------------------------------------------------------------------------------------
-- Checklist for employee C working for sub organization 5535 having outdated
-- manager information
-- --------------------------------------------------------------------------------------

-- Manager
insert into manager (created, updated, email, first_name, id, last_name, username)
values (now(), now(), 'd.manager@55351.com', 'D Man', 'b1cf6779-f2a2-4236-89c6-0ba9beab70c7', 'Ager', 'dman2agr');

-- Employee
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), subdate(now(), INTERVAL 1 DAY), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'c.employee@55351.com', 'C Emp', '702323a9-f542-4081-a780-a1396068c8c9',
        'Loyee', 'b1cf6779-f2a2-4236-89c6-0ba9beab70c7', 'Growler', 'cemp3loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        '702323a9-f542-4081-a780-a1396068c8c9', 'c179d5e1-0c5d-4eba-9f27-aca5011263c5');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('c179d5e1-0c5d-4eba-9f27-aca5011263c5', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('c179d5e1-0c5d-4eba-9f27-aca5011263c5', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- ======================================================================================
-- Employee D and Manager E are used in testcase test04
-- ======================================================================================
-- --------------------------------------------------------------------------------------
-- Checklist for employee D working for sub organization 55352 having outdated
-- manager information
-- --------------------------------------------------------------------------------------

-- Manager
insert into manager (created, updated, email, first_name, id, last_name, username)
values (now(), now(), 'e.manager@55351.com', 'E Man', '749fa5bb-438d-47cf-ab1c-0664e9951210', 'Ager', 'eman3agr');

-- Employee
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), subdate(now(), INTERVAL 1 DAY), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1',
        'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'd.employee@55352.com', 'D Emp', 'fe34e2a9-5723-4734-91dc-42a664c5432f',
        'Loyee', '749fa5bb-438d-47cf-ab1c-0664e9951210', 'Muggler', 'demp4loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        'fe34e2a9-5723-4734-91dc-42a664c5432f', 'fda66ff0-d554-4501-a16d-1b0b6e1825c3');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('fda66ff0-d554-4501-a16d-1b0b6e1825c3', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('fda66ff0-d554-4501-a16d-1b0b6e1825c3', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- ======================================================================================
-- Manager G and M, Employee E, F and I are used in testcases test05 and test06
-- ======================================================================================
-- Manager (connected to Employee F and I)
insert into manager (created, updated, email, first_name, id, last_name, username)
values (now(), now(), 'g.manager@55351.com', 'G Man', 'f59918bc-a8f1-4f97-abe3-9f80f26e6bf2', 'Ager', 'gman5agr');

-- Manager (connected to Employee E)
insert into manager (created, updated, email, first_name, id, last_name, username)
values (now(), now(), 'm.manager@55351.com', 'M Man', '9d2adcf6-9234-4faf-a6c9-0c1c7518b534', 'Ager', 'mman5agr');

-- Employee E
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'e.employee@55351.com', 'E Emp', 'bfd69468-bd32-4b84-a3b0-c5e1742a5a34', 'Loyee',
        '9d2adcf6-9234-4faf-a6c9-0c1c7518b534', 'Struggler', 'eemp5loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        'bfd69468-bd32-4b84-a3b0-c5e1742a5a34', '8fcc1fc7-bcda-4db6-9375-ff99961ef011');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('8fcc1fc7-bcda-4db6-9375-ff99961ef011', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('8fcc1fc7-bcda-4db6-9375-ff99961ef011', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Employee F
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'f.employee@55351.com', 'F Emp', '87b0d9c2-c06e-409d-b77e-63f427e0dbc2', 'Loyee',
        'f59918bc-a8f1-4f97-abe3-9f80f26e6bf2', 'Juggler', 'femp6loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        '87b0d9c2-c06e-409d-b77e-63f427e0dbc2', 'e4474a9b-1a57-49b8-bec8-2e50db600fbb');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('e4474a9b-1a57-49b8-bec8-2e50db600fbb', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('e4474a9b-1a57-49b8-bec8-2e50db600fbb', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Delegation of employee checklist
insert into delegate (email, id, manager_id, employee_checklist_id, party_id, username, first_name, last_name)
values ('delegated.email@noreply.com', 'fcfff6b0-d66f-4f09-a77c-7b02979fbe07', 'f59918bc-a8f1-4f97-abe3-9f80f26e6bf2',
        'e4474a9b-1a57-49b8-bec8-2e50db600fbb', 'b33adee5-3f8f-4201-bfc5-f0e5ba8cd54f', 'dele0gate', 'John', 'Doe');

-- Employee I (is here to verify that manager is not deleted when checklist for Employee F is removed)
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'i.employee@55351.com', 'I Emp', '7bc7707c-2a82-44fb-bbf6-fe00f9ec11c6', 'Loyee',
        'f59918bc-a8f1-4f97-abe3-9f80f26e6bf2', 'Muggler', 'iemp6loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        '7bc7707c-2a82-44fb-bbf6-fe00f9ec11c6', '426c7fbf-e943-45c8-980c-f8cfd4268767');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('426c7fbf-e943-45c8-980c-f8cfd4268767', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('426c7fbf-e943-45c8-980c-f8cfd4268767', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- ======================================================================================
-- Manager H is used in test 07, 09, 10, 11, 12, 13, 16 and 19
-- Employee G is used in test 07, 09, 11, 12 and 13
-- Employee H is used in test 10
-- Employee J is used in test 16
-- Employee K is used in test 19
-- ======================================================================================
-- Manager
insert into manager (created, updated, email, first_name, id, last_name, username)
values (now(), now(), 'h.manager@55351.com', 'H Man', '9dcffa46-c500-4696-a862-04867df207d0', 'Ager', 'hman6agr');

-- Employee G
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'g.employee@55351.com', 'G Emp', 'f1abc7a4-ad20-4c74-a5d7-3d4833405a96', 'Loyee',
        '9dcffa46-c500-4696-a862-04867df207d0', 'Slugger', 'gemp6loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        'f1abc7a4-ad20-4c74-a5d7-3d4833405a96', '855e7d4e-af50-4fd3-b81d-a71299f38d1a');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('855e7d4e-af50-4fd3-b81d-a71299f38d1a', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('855e7d4e-af50-4fd3-b81d-a71299f38d1a', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Custom task for employee
insert into custom_task (sort_order, created, updated, last_saved_by, heading, id, employee_checklist_id, phase_id,
                         `text`, question_type, role_type)
values (0, '2024-01-03 12:00:00.000', '2024-01-03 12:00:00.000', 'someUser', 'First custom employee task',
        '677a9efd-55bf-468d-81e8-efc913b9f956', '855e7d4e-af50-4fd3-b81d-a71299f38d1a',
        '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Descriptive text for first custom task', 'YES_OR_NO', 'NEW_EMPLOYEE');

-- Custom task for manager
insert into custom_task (sort_order, created, updated, last_saved_by, heading, id, employee_checklist_id, phase_id,
                         `text`, question_type, role_type)
values (0, '2024-01-03 12:00:00.000', '2024-01-03 12:00:00.000', 'someUser', 'First custom manager task',
        '0ef2b968-2b65-41d5-9865-e3e0d0f37e00', '855e7d4e-af50-4fd3-b81d-a71299f38d1a',
        '7272d1fc-540e-4394-afe2-e133ca642e91', 'Descriptive text for custom task', 'YES_OR_NO',
        'MANAGER_FOR_NEW_EMPLOYEE');

-- Employee H
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'h.employee@55351.com', 'H Emp', 'd59ded65-bdf8-49e9-837d-3ca12be93970', 'Loyee',
        '9dcffa46-c500-4696-a862-04867df207d0', 'Snuggler', 'hemp7loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        'd59ded65-bdf8-49e9-837d-3ca12be93970', '51ca9112-001b-4f12-b866-8d59ef1c25c4');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('51ca9112-001b-4f12-b866-8d59ef1c25c4', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('51ca9112-001b-4f12-b866-8d59ef1c25c4', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Custom task
insert into custom_task (sort_order, created, updated, last_saved_by, heading, id, employee_checklist_id, phase_id,
                         `text`, question_type, role_type)
values (0, '2024-01-03 12:00:00.000', '2024-01-03 12:00:00.000', 'someUser', 'First custom employee task',
        '8b4eafc8-46ec-4fe3-9c2e-11625f144b10', '51ca9112-001b-4f12-b866-8d59ef1c25c4',
        '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Descriptive text for first custom task', 'YES_OR_NO', 'NEW_EMPLOYEE');

-- Employee J
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'j.employee@55351.com', 'J Emp', '619d40cd-025f-4c13-a8f2-e08715f70d56', 'Loyee',
        '9dcffa46-c500-4696-a862-04867df207d0', 'Snuggler', 'jemp7loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id, mentor_name, mentor_user_id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        '619d40cd-025f-4c13-a8f2-e08715f70d56', 'cca064da-ab15-4276-8fbb-e8ba07b28718', 'someMentorName',
        'someMentorUserId');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('cca064da-ab15-4276-8fbb-e8ba07b28718', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('cca064da-ab15-4276-8fbb-e8ba07b28718', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Employee K (used in test18 to validate inheritance of custom sort order)
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', '11ecf84e-cb11-426f-acd9-081330f28f7c',
        'k.employee@5536.com', 'K Emp', '9fcfa7e5-fcec-4b1b-b254-8ee29d3017cd', 'Loyee',
        '9dcffa46-c500-4696-a862-04867df207d0', 'Struggler', 'kemp8loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id, mentor_name, mentor_user_id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        '9fcfa7e5-fcec-4b1b-b254-8ee29d3017cd', '87f45fed-b64c-4e81-8a3a-282df548bc38', NULL, NULL);

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('87f45fed-b64c-4e81-8a3a-282df548bc38', '68849473-59aa-4ae6-b4bd-7f046e857984'),
       ('87f45fed-b64c-4e81-8a3a-282df548bc38', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- ======================================================================================
-- Manager J is used in test 20, 21, 22, 23
-- Employee L is used in test 20, 21, 22, 23
-- Employee M is used in test 20, 21, 22, 23
-- Employee N is used in test 20, 21, 22, 23
-- Employee O is used in test 20, 21, 22, 23
-- ======================================================================================
-- Manager J
insert into manager (created, updated, email, first_name, id, last_name, username)
values (now(), now(), 'j.manager@55351.com', 'J Man', '1dcffa46-c500-4696-a862-04867df207d2', 'Ager', 'jman6agr');

-- Employee L
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'l.employee@55351.com', 'Lars', 'f1abc7a4-ad20-4c74-a5d7-3d4833405a01', 'Pettersson',
        '1dcffa46-c500-4696-a862-04867df207d2', 'Slugger', 'lemp6loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2124-07-01', '2124-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        'f1abc7a4-ad20-4c74-a5d7-3d4833405a01', '01ca9112-001b-4f12-b866-8d59ef1c25c4');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('01ca9112-001b-4f12-b866-8d59ef1c25c4', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('01ca9112-001b-4f12-b866-8d59ef1c25c4', '8c66e24b-3845-47ae-af74-c4611db8be7c');

insert into delegate (id, party_id, username, first_name, last_name, email, manager_id, employee_checklist_id)
values ('1b6e83ec-35a7-469e-8137-d1bd044c92ad', 'b7c9f932-a7f3-4bc8-9cc6-f085077db34a', 'dele0gate',
        'Frank', 'Doe', 'johndoe@email.com', '1dcffa46-c500-4696-a862-04867df207d2',
        '01ca9112-001b-4f12-b866-8d59ef1c25c4');

-- Employee M
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'm.employee@55351.com', 'Lars', 'f1abc7a4-ad20-4c74-a5d7-3d4833405a02', 'Bertilsson',
        '1dcffa46-c500-4696-a862-04867df207d2', 'Slugger', 'memp6loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2124-07-01', '2124-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        'f1abc7a4-ad20-4c74-a5d7-3d4833405a02', '02ca9112-001b-4f12-b866-8d59ef1c25c4');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('02ca9112-001b-4f12-b866-8d59ef1c25c4', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('02ca9112-001b-4f12-b866-8d59ef1c25c4', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Delegation of employee checklist
insert into delegate (id, party_id, username, first_name, last_name, email, manager_id, employee_checklist_id)
values ('3b6e83ec-35a7-469e-8133-d1bd044c92ad', 'b3c9f954-a7f3-4bc8-9cc6-f085077db34a', 'dele0gate',
        'Johnny', 'Doe', 'johndoe@email.com', '1dcffa46-c500-4696-a862-04867df207d2',
        '02ca9112-001b-4f12-b866-8d59ef1c25c4');

-- Employee N
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'n.employee@55351.com', 'Anton', 'f1abc7a4-ad20-4c74-a5d7-3d4833405a03', 'Nyberg',
        '1dcffa46-c500-4696-a862-04867df207d2', 'Slugger', 'nemp6loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2124-07-01', '2124-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        'f1abc7a4-ad20-4c74-a5d7-3d4833405a03', '03ca9112-001b-4f12-b866-8d59ef1c25c4');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('03ca9112-001b-4f12-b866-8d59ef1c25c4', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('03ca9112-001b-4f12-b866-8d59ef1c25c4', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Delegation of employee checklist
insert into delegate (id, party_id, username, first_name, last_name, email, manager_id, employee_checklist_id)
values ('3b6e83ec-35a7-469e-8137-d1bd044c92ad', 'b3c9f934-a7f3-4bc8-9cc6-f085077db34a', 'dele0gate',
        'Hank', 'Doe', 'johndoe@email.com', '1dcffa46-c500-4696-a862-04867df207d2',
        '03ca9112-001b-4f12-b866-8d59ef1c25c4'),
       ('5b6e83ec-35a7-469e-8137-d1bd044c92ad', 'b3c9f934-a7f3-4bc8-9cc6-f085077db33a', 'dele1gate',
        'Patrik', 'Franco', 'patrikfranco@email.com', '1dcffa46-c500-4696-a862-04867df207d2',
        '03ca9112-001b-4f12-b866-8d59ef1c25c4');

-- Employee O
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name,
                      manager_id, title, username, employment_position)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b',
        'o.employee@55351.com', 'Fredrik', 'f1abc7a4-ad20-4c74-a5d7-3d4833405a04', 'Karlsson',
        '1dcffa46-c500-4696-a862-04867df207d2', 'Slugger', 'oemp6loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, correspondence_id,
                                employee_id, id)
values ('2124-07-01', '2124-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', NULL,
        'f1abc7a4-ad20-4c74-a5d7-3d4833405a04', '04ca9112-001b-4f12-b866-8d59ef1c25c4');

insert into referred_checklist (employee_checklist_id, checklist_id)
values ('04ca9112-001b-4f12-b866-8d59ef1c25c4', 'e20598a4-6b32-459e-8c15-febbd4c5868e'),
       ('04ca9112-001b-4f12-b866-8d59ef1c25c4', '8c66e24b-3845-47ae-af74-c4611db8be7c');

-- Delegation of employee checklist
insert into delegate (id, party_id, username, first_name, last_name, email, manager_id, employee_checklist_id)
values ('4b6e83ec-35a7-469e-8137-d1bd044c92ad', 'b3c9f934-a7f3-4bc8-9cc6-f085077db32a', 'dele0gate',
        'John', 'Doe', 'johndoe@email.com', '1dcffa46-c500-4696-a862-04867df207d2',
        '04ca9112-001b-4f12-b866-8d59ef1c25c4');

