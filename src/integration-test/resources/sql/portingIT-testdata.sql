INSERT INTO organization(municipality_id, id, organization_name, organization_number, created, updated)
-- Organization for test01 and test02
VALUES ('2281', '15764278-50c8-4a19-af00-077bfc314fd2', 'Organization_1', '1', now(), now()),
-- Organization for test03
       ('2281', '25764278-50c8-4a19-af00-077bfc314fd2', 'Organization_2', '2', now(), now()),
-- Organization for test04
       ('2281', '35764278-50c8-4a19-af00-077bfc314fd2', 'Organization_3', '3', now(), now()),
-- Organization for test05
       ('2281', '45764278-50c8-4a19-af00-077bfc314fd2', 'Organization_4', '4', now(), now()),
-- Organization for test06
       ('2281', '55764278-50c8-4a19-af00-077bfc314fd2', 'Organization_5', '5', now(), now()),
-- Organization for test07
       ('2281', '65764278-50c8-4a19-af00-077bfc314fd2', 'Organization_6', '6', now(), now()),
-- Organization for test08
       ('2281', '75764278-50c8-4a19-af00-077bfc314fd2', 'Organization_7', '7', now(), now());

INSERT INTO organization_communication_channel(organization_id, communication_channel)
VALUES ('15764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('25764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('35764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('45764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('55764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('65764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('75764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL');
       
INSERT INTO checklist(municipality_id, id, organization_id, name, display_name, version, role_type, life_cycle, created, updated)
-- Checklists for test01 and test02
VALUES ('2281', '1bc85739-cf6d-4223-b9dc-09218f044db7', '15764278-50c8-4a19-af00-077bfc314fd2', 'TEST01_02_EMPLOYEE_CHECKLIST', 'Checklist for employee',    1, 'EMPLOYEE', 'DEPRECATED', now(), now()),
       ('2281', '2bc85739-cf6d-4223-b9dc-09218f044db7', '15764278-50c8-4a19-af00-077bfc314fd2', 'TEST01_02_EMPLOYEE_CHECKLIST', 'Checklist for employee',    2, 'EMPLOYEE', 'ACTIVE',     now(), now()),
       ('2281', '1a16a4a5-3dce-48a6-b6f1-31abca9af606', '15764278-50c8-4a19-af00-077bfc314fd2', 'TEST01_02_MANAGER_CHECKLIST',  'Checklist for manager',     1, 'MANAGER',  'ACTIVE',     now(), now()),
-- Checklist for test03
       ('2281', '83929b6a-c29e-4d95-9212-b02fb3f9b9cf', '25764278-50c8-4a19-af00-077bfc314fd2', 'TEST03_EMPLOYEE_CHECKLIST', 'Active checklist for employee', 1, 'EMPLOYEE', 'ACTIVE',  now(), now()),
-- Checklists for test04
       ('2281', '141896ee-6c32-4b53-8c81-3bae51a1ed05', '35764278-50c8-4a19-af00-077bfc314fd2', 'TEST04_EMPLOYEE_CHECKLIST', 'Active checklist for employee', 1, 'EMPLOYEE', 'ACTIVE',  now(), now()),
       ('2281', '241896ee-6c32-4b53-8c81-3bae51a1ed05', '35764278-50c8-4a19-af00-077bfc314fd2', 'TEST04_EMPLOYEE_CHECKLIST', 'Draft checklist for employee',  2, 'EMPLOYEE', 'CREATED', now(), now()),
-- Checklist for test06
       ('2281', '3880efec-e66e-42a5-a3ac-ea2d08da0e5c', '55764278-50c8-4a19-af00-077bfc314fd2', 'TEST06_EMPLOYEE_CHECKLIST', 'Active checklist for employee', 1, 'EMPLOYEE', 'ACTIVE',  now(), now()),
-- Checklists for test07
       ('2281', '1914e06d-5ca5-4ea7-9052-23228be56cca', '65764278-50c8-4a19-af00-077bfc314fd2', 'TEST07_EMPLOYEE_CHECKLIST', 'Active checklist for employee', 1, 'EMPLOYEE', 'ACTIVE',  now(), now()),
       ('2281', '2914e06d-5ca5-4ea7-9052-23228be56cca', '65764278-50c8-4a19-af00-077bfc314fd2', 'TEST07_EMPLOYEE_CHECKLIST', 'Draft checklist for employee', 2, 'EMPLOYEE', 'CREATED', now(), now());

INSERT INTO phase(id, name, body_text, time_to_complete, role_type, sort_order, created, updated, checklist_id)
-- Phases for checklists in test01 and test02
VALUES ('1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Phase name', 'Phase description of deprecated phase for employee', 'P1W', 'EMPLOYEE', 1, now(), now(), '1bc85739-cf6d-4223-b9dc-09218f044db7'),
       ('2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Phase name', 'Phase description of active phase for employee',     'P1W', 'EMPLOYEE', 1, now(), now(), '2bc85739-cf6d-4223-b9dc-09218f044db7'),
       ('459befa3-11eb-4287-bb07-ed7367780fa7', 'Phase name', 'Phase description of active phase for new manager',  'P1W', 'EMPLOYEE', 1, now(), now(), '1a16a4a5-3dce-48a6-b6f1-31abca9af606'),
-- Phases for checklist in test03
       ('11bf5015-2dda-4dd6-9129-797fa2868e56', 'Phase name', 'Phase description', 'P1W', 'EMPLOYEE', 1, now(), now(), '83929b6a-c29e-4d95-9212-b02fb3f9b9cf'),
-- Phases for checklists in test04
       ('1e20a240-e1a6-458a-bcb0-572bfe345806', 'Phase name', 'Phase description', 'P1W', 'EMPLOYEE', 1, now(), now(), '141896ee-6c32-4b53-8c81-3bae51a1ed05'),
       ('2e20a240-e1a6-458a-bcb0-572bfe345806', 'Phase name', 'Phase description', 'P1W', 'EMPLOYEE', 1, now(), now(), '241896ee-6c32-4b53-8c81-3bae51a1ed05'),
-- Phases for checklist in test06
       ('784d3ce0-ade8-4241-8f50-1c93573b2303', 'Phase name', 'Phase description', 'P1W', 'EMPLOYEE', 1, now(), now(), '3880efec-e66e-42a5-a3ac-ea2d08da0e5c'),
-- Phases for checklists in test07
       ('1fc59eb7-95b7-4323-899f-310762b1cdd7', 'Phase name', 'Phase description', 'P1W', 'EMPLOYEE', 1, now(), now(), '1914e06d-5ca5-4ea7-9052-23228be56cca'),
       ('2fc59eb7-95b7-4323-899f-310762b1cdd7', 'Phase name', 'Phase description', 'P1W', 'EMPLOYEE', 1, now(), now(), '2914e06d-5ca5-4ea7-9052-23228be56cca');
       
INSERT INTO task(id, heading, text, role_type, question_type, sort_order, created, updated, phase_id)
-- Tasks for phases in test01 and test02
VALUES ('1af3e3c4-4b67-4f74-ae6e-dfde9a7cf934', 'Task name', 'Task description of deprecated task for employee', 'EMPLOYEE', 'YES_OR_NO', 1, now(), now(), '1455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('2af3e3c4-4b67-4f74-ae6e-dfde9a7cf934', 'Task name', 'Task description of active task for employee',     'EMPLOYEE', 'YES_OR_NO', 1, now(), now(), '2455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('ca8a7b75-bf90-45a5-a6dc-df66f5ce38d7', 'Task name', 'Task description of active task for new manager',  'EMPLOYEE', 'YES_OR_NO', 1, now(), now(), '459befa3-11eb-4287-bb07-ed7367780fa7'),
-- Task for phase in test03
       ('cc7076b6-ffee-467a-870e-d1ba1ff1b892', 'Task name', 'Task description', 'EMPLOYEE', 'YES_OR_NO', 1, now(), now(), '11bf5015-2dda-4dd6-9129-797fa2868e56'),
-- Tasks for phases in test04
       ('1d1d4f38-9687-47b8-93e4-9fe133c08e9a', 'Task name', 'Task description', 'EMPLOYEE', 'YES_OR_NO', 1, now(), now(), '1e20a240-e1a6-458a-bcb0-572bfe345806'),
       ('2d1d4f38-9687-47b8-93e4-9fe133c08e9a', 'Task name', 'Task description', 'EMPLOYEE', 'YES_OR_NO', 1, now(), now(), '2e20a240-e1a6-458a-bcb0-572bfe345806'),
-- Task for phase in test06
       ('a7ce8ecd-5ff9-4b33-9d66-0870f3cd2ddd', 'Task name', 'Task description', 'EMPLOYEE', 'YES_OR_NO', 1, now(), now(), '784d3ce0-ade8-4241-8f50-1c93573b2303'),
-- Tasks for phases in test07
       ('1f78daa1-e83a-4523-8939-9a1a2ccdeb3c', 'Task name', 'Task description', 'EMPLOYEE', 'YES_OR_NO', 1, now(), now(), '1fc59eb7-95b7-4323-899f-310762b1cdd7'),
       ('2f78daa1-e83a-4523-8939-9a1a2ccdeb3c', 'Task name', 'Task description', 'EMPLOYEE', 'YES_OR_NO', 1, now(), now(), '2fc59eb7-95b7-4323-899f-310762b1cdd7');
