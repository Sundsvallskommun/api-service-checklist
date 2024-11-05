-- ======================================================================================
-- Common data used in all tests
-- ======================================================================================
-- --------------------------------------------------------------------------------------
-- Checklist for roletype employee for organizationNumber 5335 (Sub organization 5335)
-- --------------------------------------------------------------------------------------
insert into organization (municipality_id, organization_number, created, updated, organization_name, id)
values ('2281', 5535, now(), now(), 'Sub organization 5335', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b');

insert into checklist (municipality_id, version, created, updated, last_saved_by, id, organization_id, life_cycle, name, role_type) -- setting role_type on checklist to employee means that checklist is aimed for new footpeople
values ('2281', 1, now(), now(), 'someUser', 'e20598a4-6b32-459e-8c15-febbd4c5868e', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'ACTIVE', 'Checklist for Sub organization 5335', 'EMPLOYEE');

insert into phase (sort_order, created, updated, last_saved_by, body_text, role_type, checklist_id, id, name, time_to_complete) -- role_type on phase defines if it is the new employee or his/hers boss that the target for the phase
values (1, now(), now(), 'someUser', 'Description of manager phase', 'MANAGER', 'e20598a4-6b32-459e-8c15-febbd4c5868e', '7272d1fc-540e-4394-afe2-e133ca642e91', 'Phasename', 'P1M');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, phase_id, text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (1, 'YES_OR_NO', now(), now(), 'someUser', 'Description of manager task', 'e43fdac2-331e-4808-8d83-753a59e329cd', 'MANAGER', '7272d1fc-540e-4394-afe2-e133ca642e91', 'Text for manager task');

insert into phase (sort_order, created, updated, last_saved_by, body_text, role_type, checklist_id, id, name, time_to_complete) -- role_type on phase defines if it is the new employee or his/hers boss that the target for the phase
values (2, now(), now(), 'someUser', 'Description of employee phase', 'EMPLOYEE', 'e20598a4-6b32-459e-8c15-febbd4c5868e', '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Phasename', 'P1M');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, phase_id, text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (1, 'YES_OR_NO_WITH_TEXT', now(), now(), 'someUser', 'Description of first employee task', 'd250a20c-a616-4147-bfe0-19a0d12f3df0', 'EMPLOYEE', '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Text for first employee task');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, phase_id, text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (2, 'YES_OR_NO', now(), now(), 'someUser', 'Description of second employee task', '0c8b99e9-718b-4c92-9ba3-a49dc29d48b5', 'EMPLOYEE', '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Text for second employee task');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, phase_id, text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (3, 'YES_OR_NO', now(), now(), 'someUser', 'Description of manager task', '056423aa-01a4-4243-ace4-561a6e4cd25f', 'MANAGER', '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Text for manager task');

-- --------------------------------------------------------------------------------------
-- Checklist for roletype employee for organizationNumber 1 (Root organization)
-- --------------------------------------------------------------------------------------
insert into organization (municipality_id, organization_number, created, updated, organization_name, id)
values ('2281', 1, now(), now(), null, 'cfcb03b1-7344-4352-9b72-7aebb1f235e1');

insert into checklist (municipality_id, version, created, updated, last_saved_by, id, organization_id, life_cycle, name, role_type) -- setting role_type on checklist to employee means that checklist is aimed for new footpeople
values ('2281', 1, now(), now(), 'someUser', '8c66e24b-3845-47ae-af74-c4611db8be7c', 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'ACTIVE', 'Checklist for Root organization', 'EMPLOYEE');

insert into phase (sort_order, created, updated, last_saved_by, body_text, role_type, checklist_id, id, name, time_to_complete) -- role_type on phase defines if it is the new employee or his/hers boss that the target for the phase
values (1, now(), now(), 'someUser', 'Description of manager phase', 'MANAGER', '8c66e24b-3845-47ae-af74-c4611db8be7c', '539b074d-d654-49ec-9dce-220f8a5ba7bb', 'Phasename', 'P1M');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, phase_id, text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (1, 'COMPLETED_OR_NOT_RELEVANT', now(), now(), 'someUser', 'Description of manager task', 'c8c7abe0-5703-4ca1-86d3-74e5ad79b690', 'MANAGER', '539b074d-d654-49ec-9dce-220f8a5ba7bb', 'Text for manager task');

insert into phase (sort_order, created, updated, last_saved_by, body_text, role_type, checklist_id, id, name, time_to_complete) -- role_type on phase defines if it is the new employee or his/hers boss that the target for the phase
values (2, now(), now(), 'someUser', 'Description of employee phase', 'EMPLOYEE', '8c66e24b-3845-47ae-af74-c4611db8be7c', 'd2c92810-3c46-4161-af54-7ae8b3c9d0b3', 'Phasename', 'P1M');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, phase_id, text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (1, 'COMPLETED_OR_NOT_RELEVANT_WITH_TEXT', now(), now(), 'someUser', 'Description of employee task', 'a76e9920-261f-42fc-9077-14fb1cdb9871', 'EMPLOYEE', 'd2c92810-3c46-4161-af54-7ae8b3c9d0b3', 'Text for employee task');

-- --------------------------------------------------------------------------------------
-- Checklist for roletype manager for organizationNumber 1 (Sundsvalls kommun)
-- --------------------------------------------------------------------------------------
insert into checklist (municipality_id, version, created, updated, last_saved_by, id, organization_id, life_cycle, name, role_type) -- setting role_type on checklist to manager means that checklist is aimed for new managers
values ('2281', 1, now(), now(), 'someUser', '0ae36695-c575-4ec4-bd97-a25093c2021f', 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'ACTIVE', 'Checklist for Sundsvalls kommun (chefer)', 'MANAGER');

insert into phase (sort_order, created, updated, last_saved_by, body_text, role_type, checklist_id, id, name, time_to_complete) -- role_type on phase defines if it is the new employee or his/hers boss that the target for the phase
values (1, now(), now(), 'someUser', 'Description of phase', 'EMPLOYEE', '0ae36695-c575-4ec4-bd97-a25093c2021f', '638ad05e-3f90-4cb1-b26f-1f364469b386', 'Phasename', 'P1M');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, phase_id, text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (1, 'YES_OR_NO', now(), now(), 'someUser', 'Description of new manager task', '0da1dfa2-5196-45c2-b605-162a323b9b5e', 'EMPLOYEE', '638ad05e-3f90-4cb1-b26f-1f364469b386', 'Text for task to be performed by new manager');

insert into task (sort_order, question_type, created, updated, last_saved_by, heading, id, role_type, phase_id, text) -- role_type on task defines if it is the new employee or his/hers boss that is targeted for the task
values (2, 'YES_OR_NO', now(), now(), 'someUser', 'Description of managers manager task', 'e930c70d-a961-4b71-89b4-935d47db982f', 'MANAGER', '638ad05e-3f90-4cb1-b26f-1f364469b386', 'Text for task to be performed by managers manager');

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
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, username, role_type)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'a.employee@5535.com', 'A Emp', '1810c9c4-7281-44de-9930-426d9f065f4d', 'Loyee', '02817ff3-632a-4228-9c31-25ad8124568c', 'Cleaner', 'aemp0loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, checklist_id, correspondence_id, employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL, '1810c9c4-7281-44de-9930-426d9f065f4d', 'f853e2b1-a144-4305-b05e-ee8d6dc6d005');

-- Fulfilment
insert into fulfilment (updated, id, employee_checklist_id, response_text, task_id, completed)
values ('2024-01-02 12:00:00.000', '34e076da-8694-4cb9-be1a-814212801686', 'f853e2b1-a144-4305-b05e-ee8d6dc6d005', 'Response for employee task', 'd250a20c-a616-4147-bfe0-19a0d12f3df0', 'TRUE');

-- --------------------------------------------------------------------------------------
-- Checklist for employee B working for sub organization 5535
-- --------------------------------------------------------------------------------------

-- Employee
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, username, role_type)
values ('2024-01-06', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'b.employee@5535.com', 'B Emp', '8122705b-e0e6-4055-b301-eba21986e219', 'Loyee', '02817ff3-632a-4228-9c31-25ad8124568c', 'Loather', 'bemp0loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, checklist_id, correspondence_id, employee_id, id)
values ('2024-07-06', '2024-10-06', '2024-01-06', false, '2024-01-06 12:00:00.000', '2024-01-06 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL, '8122705b-e0e6-4055-b301-eba21986e219', 'f5960058-fad8-4825-85f3-b0fdb518adc5');

-- Custom task
insert into custom_task (sort_order, created, updated, last_saved_by, heading, id, employee_checklist_id, phase_id, `text`, question_type, role_type)
values (0, '2024-01-03 12:00:00.000', '2024-01-03 12:00:00.000', 'someUser', 'Custom employee task', '1b3bfe66-0e6c-4e92-a410-7c620a5461f4', 'f5960058-fad8-4825-85f3-b0fdb518adc5', '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Descriptive text for custom task', 'YES_OR_NO', 'EMPLOYEE');

-- Custom fulfilment
insert into custom_fulfilment (updated, custom_task_id, id, employee_checklist_id, response_text, completed)
VALUES('2024-01-03 12:00:00.000', '1b3bfe66-0e6c-4e92-a410-7c620a5461f4', 'a5d19134-d21f-4965-bd25-44a123e94ee1', 'f5960058-fad8-4825-85f3-b0fdb518adc5', NULL, 'FALSE');

-- --------------------------------------------------------------------------------------
-- Checklist for manager C working for sub organization 55351 hence getting
-- manager checklist for root organization
-- --------------------------------------------------------------------------------------

insert into organization (municipality_id, organization_number, created, updated, organization_name, id)
values ('2281', 55351, now(), now(), 'Sub organization 55351', '176eb3d9-ebbc-4951-9c8a-e4503f003f79');

-- Employee
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, username, role_type)
values ('2024-01-06', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', '176eb3d9-ebbc-4951-9c8a-e4503f003f79', 'c.manager@55351.com', 'C Newman', 'ae93a63e-d975-4cd9-8e28-4cc9ea8b4d96', 'Ager', '02817ff3-632a-4228-9c31-25ad8124568c', 'New manager', 'cman1agr', 'MANAGER');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, checklist_id, correspondence_id, employee_id, id)
values ('2024-07-06', '2024-10-06', '2024-01-06', false, '2024-01-06 12:00:00.000', '2024-01-06 12:00:00.000', '0ae36695-c575-4ec4-bd97-a25093c2021f', NULL, 'ae93a63e-d975-4cd9-8e28-4cc9ea8b4d96', '2e2bb099-560c-450e-8f20-764498a37983');

-- Fulfilments
insert into fulfilment (updated, id, employee_checklist_id, response_text, task_id, completed)
values ('2024-01-07 12:00:00.000', '6b17e790-f22c-46fe-8bff-f58af8cbab6a', '2e2bb099-560c-450e-8f20-764498a37983', 'Response for new manager task', '0da1dfa2-5196-45c2-b605-162a323b9b5e', 'TRUE'),
       ('2024-01-07 12:00:00.000', 'c44c6fac-2875-468c-9112-e0a5a2646cb6', '2e2bb099-560c-450e-8f20-764498a37983', 'Response for managers manager task', 'e930c70d-a961-4b71-89b4-935d47db982f', 'TRUE');

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
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, username, role_type)
values ('2024-01-01', now(), subdate(now(), INTERVAL 1 DAY), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'c.employee@55351.com', 'C Emp', '702323a9-f542-4081-a780-a1396068c8c9', 'Loyee', 'b1cf6779-f2a2-4236-89c6-0ba9beab70c7', 'Growler', 'cemp3loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, checklist_id, correspondence_id, employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL, '702323a9-f542-4081-a780-a1396068c8c9', 'c179d5e1-0c5d-4eba-9f27-aca5011263c5');

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
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, username, role_type)
values ('2024-01-01', now(), subdate(now(), INTERVAL 1 DAY), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'd.employee@55352.com', 'D Emp', 'fe34e2a9-5723-4734-91dc-42a664c5432f', 'Loyee', '749fa5bb-438d-47cf-ab1c-0664e9951210', 'Muggler', 'demp4loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, checklist_id, correspondence_id, employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL, 'fe34e2a9-5723-4734-91dc-42a664c5432f', 'fda66ff0-d554-4501-a16d-1b0b6e1825c3');

-- ======================================================================================
-- Manager G, Employee E and Employee F are used in testcases test05 and test06
-- ======================================================================================
-- Manager
insert into manager (created, updated, email, first_name, id, last_name, username)
values (now(), now(), 'g.manager@55351.com', 'G Man', 'f59918bc-a8f1-4f97-abe3-9f80f26e6bf2', 'Ager', 'gman5agr');

-- Employee E
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, username, role_type)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'e.employee@55351.com', 'E Emp', 'bfd69468-bd32-4b84-a3b0-c5e1742a5a34', 'Loyee', 'f59918bc-a8f1-4f97-abe3-9f80f26e6bf2', 'Struggler', 'eemp5loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, checklist_id, correspondence_id, employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL, 'bfd69468-bd32-4b84-a3b0-c5e1742a5a34', '8fcc1fc7-bcda-4db6-9375-ff99961ef011');

-- Employee F
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, username, role_type)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'f.employee@55351.com', 'F Emp', '87b0d9c2-c06e-409d-b77e-63f427e0dbc2', 'Loyee', 'f59918bc-a8f1-4f97-abe3-9f80f26e6bf2', 'Juggler', 'femp6loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, checklist_id, correspondence_id, employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL, '87b0d9c2-c06e-409d-b77e-63f427e0dbc2', 'e4474a9b-1a57-49b8-bec8-2e50db600fbb');

-- Delegation of employee checklist
insert into delegate (email, id, manager_id, employee_checklist_id, party_id, username, first_name, last_name)
values ('delegated.email@noreply.com', 'fcfff6b0-d66f-4f09-a77c-7b02979fbe07', 'f59918bc-a8f1-4f97-abe3-9f80f26e6bf2', 'e4474a9b-1a57-49b8-bec8-2e50db600fbb', 'b33adee5-3f8f-4201-bfc5-f0e5ba8cd54f', 'dele0gate', 'John', 'Doe');

-- ======================================================================================
-- Manager H is used in testcases test07, test09, test10, test11, test12 and test13
-- Employee G is used in testcases test07, test09, test11, test12 and test13
-- Employee H is used in testcase test10
-- ======================================================================================
-- Manager
insert into manager (created, updated, email, first_name, id, last_name, username)
values (now(), now(), 'h.manager@55351.com', 'H Man', '9dcffa46-c500-4696-a862-04867df207d0', 'Ager', 'hman6agr');

-- Employee G
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, username, role_type)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'g.employee@55351.com', 'G Emp', 'f1abc7a4-ad20-4c74-a5d7-3d4833405a96', 'Loyee', '9dcffa46-c500-4696-a862-04867df207d0', 'Slugger', 'gemp6loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, checklist_id, correspondence_id, employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL, 'f1abc7a4-ad20-4c74-a5d7-3d4833405a96', '855e7d4e-af50-4fd3-b81d-a71299f38d1a');

-- Custom task in employee phase
insert into custom_task (sort_order, created, updated, last_saved_by, heading, id, employee_checklist_id, phase_id, `text`, question_type, role_type)
values (0, '2024-01-03 12:00:00.000', '2024-01-03 12:00:00.000', 'someUser', 'First custom employee task', '677a9efd-55bf-468d-81e8-efc913b9f956', '855e7d4e-af50-4fd3-b81d-a71299f38d1a', '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Descriptive text for first custom task', 'YES_OR_NO', 'EMPLOYEE');

-- Custom task in manager phase
insert into custom_task (sort_order, created, updated, last_saved_by, heading, id, employee_checklist_id, phase_id, `text`, question_type, role_type)
values (0, '2024-01-03 12:00:00.000', '2024-01-03 12:00:00.000', 'someUser', 'First custom manager task', '0ef2b968-2b65-41d5-9865-e3e0d0f37e00', '855e7d4e-af50-4fd3-b81d-a71299f38d1a', '7272d1fc-540e-4394-afe2-e133ca642e91', 'Descriptive text for custom task', 'YES_OR_NO', 'MANAGER');

-- Employee H
insert into employee (start_date, created, updated, organization_id, department_id, email, first_name, id, last_name, manager_id, title, username, role_type)
values ('2024-01-01', now(), now(), 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'bd49f474-303c-4a4e-aa54-5d4f58d9188b', 'h.employee@55351.com', 'H Emp', 'd59ded65-bdf8-49e9-837d-3ca12be93970', 'Loyee', '9dcffa46-c500-4696-a862-04867df207d0', 'Snuggler', 'hemp7loyee', 'EMPLOYEE');

-- Employee checklist
insert into employee_checklist (end_date, expiration_date, start_date, locked, created, updated, checklist_id, correspondence_id, employee_id, id)
values ('2024-07-01', '2024-10-01', '2024-01-01', false, '2024-01-01 12:00:00.000', '2024-01-01 12:00:00.000', 'e20598a4-6b32-459e-8c15-febbd4c5868e', NULL, 'd59ded65-bdf8-49e9-837d-3ca12be93970', '51ca9112-001b-4f12-b866-8d59ef1c25c4');

-- Custom task
insert into custom_task (sort_order, created, updated, last_saved_by, heading, id, employee_checklist_id, phase_id, `text`, question_type, role_type)
values (0, '2024-01-03 12:00:00.000', '2024-01-03 12:00:00.000', 'someUser', 'First custom employee task', '8b4eafc8-46ec-4fe3-9c2e-11625f144b10', '51ca9112-001b-4f12-b866-8d59ef1c25c4', '3e9780a7-96f3-4d07-80ee-a9634b786a38', 'Descriptive text for first custom task', 'YES_OR_NO', 'EMPLOYEE');
