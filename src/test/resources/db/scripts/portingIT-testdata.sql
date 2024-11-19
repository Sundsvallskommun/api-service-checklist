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
       
INSERT INTO checklist(municipality_id, id, organization_id, name, display_name, version, life_cycle, created, updated, last_saved_by)
-- Checklists for test01 and test02
VALUES ('2281', '1bc85739-cf6d-4223-b9dc-09218f044db7', '15764278-50c8-4a19-af00-077bfc314fd2', 'TEST01_02_CHECKLIST', 'Deprecated checklist',     1, 'DEPRECATED', now(), now(), 'someUser'),
       ('2281', '2bc85739-cf6d-4223-b9dc-09218f044db7', '15764278-50c8-4a19-af00-077bfc314fd2', 'TEST01_02_CHECKLIST', 'Active checklist',         2, 'ACTIVE',     now(), now(), 'someUser'),
-- Checklist for test03
       ('2281', '83929b6a-c29e-4d95-9212-b02fb3f9b9cf', '25764278-50c8-4a19-af00-077bfc314fd2', 'TEST03_CHECKLIST', 'Active checklist',            1, 'ACTIVE',  now(), now(), 'someUser'),
-- Checklists for test04
       ('2281', '141896ee-6c32-4b53-8c81-3bae51a1ed05', '35764278-50c8-4a19-af00-077bfc314fd2', 'TEST04_CHECKLIST', 'Active checklist',            1, 'ACTIVE',  now(), now(), 'someUser'),
       ('2281', '241896ee-6c32-4b53-8c81-3bae51a1ed05', '35764278-50c8-4a19-af00-077bfc314fd2', 'TEST04_CHECKLIST', 'Draft checklist',             2, 'CREATED', now(), now(), 'someUser'),
-- Checklist for test06
       ('2281', '3880efec-e66e-42a5-a3ac-ea2d08da0e5c', '55764278-50c8-4a19-af00-077bfc314fd2', 'TEST06_CHECKLIST', 'Active checklist',            1, 'ACTIVE',  now(), now(), 'someUser'),
-- Checklists for test07
       ('2281', '1914e06d-5ca5-4ea7-9052-23228be56cca', '65764278-50c8-4a19-af00-077bfc314fd2', 'TEST07_CHECKLIST', 'Active checklist',            1, 'ACTIVE',  now(), now(), 'someUser'),
       ('2281', '2914e06d-5ca5-4ea7-9052-23228be56cca', '65764278-50c8-4a19-af00-077bfc314fd2', 'TEST07_CHECKLIST', 'Draft checklist',             2, 'CREATED', now(), now(), 'someUser');

INSERT INTO phase(municipality_id, id, name, body_text, time_to_complete, sort_order, created, updated, last_saved_by)
-- Phases for checklists in test01 and test02
VALUES ('2281', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Phase name', 'Phase description of deprecated phase', 'P1W', 1, now(), now(), 'someUser'),
       ('2281', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Phase name', 'Phase description of active phase',     'P1W', 1, now(), now(), 'someUser'),
-- Phases for checklist in test03
       ('2281', '11bf5015-2dda-4dd6-9129-797fa2868e56', 'Phase name', 'Phase description', 'P1W', 1, now(), now(), 'someUser'),
-- Phases for checklists in test04
       ('2281', '1e20a240-e1a6-458a-bcb0-572bfe345806', 'Phase name', 'Phase description', 'P1W', 1, now(), now(), 'someUser'),
       ('2281', '2e20a240-e1a6-458a-bcb0-572bfe345806', 'Phase name', 'Phase description', 'P1W', 1, now(), now(), 'someUser'),
-- Phases for checklist in test06
       ('2281', '784d3ce0-ade8-4241-8f50-1c93573b2303', 'Phase name', 'Phase description', 'P1W', 1, now(), now(), 'someUser'),
-- Phases for checklists in test07
       ('2281', '1fc59eb7-95b7-4323-899f-310762b1cdd7', 'Phase name', 'Phase description', 'P1W', 1, now(), now(), 'someUser'),
       ('2281', '2fc59eb7-95b7-4323-899f-310762b1cdd7', 'Phase name', 'Phase description', 'P1W', 1, now(), now(), 'someUser');
       
INSERT INTO task(id, heading, text, role_type, question_type, sort_order, created, updated, last_saved_by, checklist_id, phase_id)
-- Tasks for phases in test01 and test02
VALUES ('1af3e3c4-4b67-4f74-ae6e-dfde9a7cf934', 'Task name', 'Task description of deprecated task for employee', 'NEW_EMPLOYEE', 'YES_OR_NO', 1, now(), now(), 'someUser', '1bc85739-cf6d-4223-b9dc-09218f044db7', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('2af3e3c4-4b67-4f74-ae6e-dfde9a7cf934', 'Task name', 'Task description of active task for employee',     'NEW_EMPLOYEE', 'YES_OR_NO', 1, now(), now(), 'someUser', '2bc85739-cf6d-4223-b9dc-09218f044db7', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('ca8a7b75-bf90-45a5-a6dc-df66f5ce38d7', 'Task name', 'Task description of active task for new manager',  'NEW_MANAGER',  'YES_OR_NO', 1, now(), now(), 'someUser', '2bc85739-cf6d-4223-b9dc-09218f044db7', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
-- Task for phase in test03
       ('cc7076b6-ffee-467a-870e-d1ba1ff1b892', 'Task name', 'Task description', 'NEW_EMPLOYEE', 'YES_OR_NO', 1, now(), now(), 'someUser', '83929b6a-c29e-4d95-9212-b02fb3f9b9cf', '11bf5015-2dda-4dd6-9129-797fa2868e56'),
-- Tasks for phases in test04
       ('1d1d4f38-9687-47b8-93e4-9fe133c08e9a', 'Task name', 'Task description', 'NEW_EMPLOYEE', 'YES_OR_NO', 1, now(), now(), 'someUser', '141896ee-6c32-4b53-8c81-3bae51a1ed05', '1e20a240-e1a6-458a-bcb0-572bfe345806'),
       ('2d1d4f38-9687-47b8-93e4-9fe133c08e9a', 'Task name', 'Task description', 'NEW_EMPLOYEE', 'YES_OR_NO', 1, now(), now(), 'someUser', '241896ee-6c32-4b53-8c81-3bae51a1ed05', '2e20a240-e1a6-458a-bcb0-572bfe345806'),
-- Task for phase in test06
       ('a7ce8ecd-5ff9-4b33-9d66-0870f3cd2ddd', 'Task name', 'Task description', 'NEW_EMPLOYEE', 'YES_OR_NO', 1, now(), now(), 'someUser', '3880efec-e66e-42a5-a3ac-ea2d08da0e5c', '784d3ce0-ade8-4241-8f50-1c93573b2303'),
-- Tasks for phases in test07
       ('1f78daa1-e83a-4523-8939-9a1a2ccdeb3c', 'Task name', 'Task description', 'NEW_EMPLOYEE', 'YES_OR_NO', 1, now(), now(), 'someUser', '1914e06d-5ca5-4ea7-9052-23228be56cca', '1fc59eb7-95b7-4323-899f-310762b1cdd7'),
       ('2f78daa1-e83a-4523-8939-9a1a2ccdeb3c', 'Task name', 'Task description', 'NEW_EMPLOYEE', 'YES_OR_NO', 1, now(), now(), 'someUser', '2914e06d-5ca5-4ea7-9052-23228be56cca', '2fc59eb7-95b7-4323-899f-310762b1cdd7');
