INSERT INTO organization(municipality_id, id, organization_name, organization_number, created, updated)
VALUES ('2281', '15764278-50c8-4a19-af00-077bfc314fd2', 'Root organization A (level 1)', '1', '2019-01-01 00:00:00', '2019-01-01 00:00:00'), -- has an active checklist
       ('2281', '25764278-50c8-4a19-af00-077bfc314fd2', 'Middle organization 1 (level 2)', '11', '2019-01-01 00:00:00', '2019-01-01 00:00:00'), -- should have own sort order, plus an active checklist
       ('2281', '35764278-50c8-4a19-af00-077bfc314fd2', 'Middle organization 2 (level 2)', '12', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
       ('2281', '45764278-50c8-4a19-af00-077bfc314fd2', 'Leaf organization 1 (level 3)', '111', '2019-01-01 00:00:00', '2019-01-01 00:00:00'), -- should have own sort order, plus a deprecated and an active checklist
       ('2281', '55764278-50c8-4a19-af00-077bfc314fd2', 'Leaf organization 2 (level 3)', '112', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
       ('2262', '65764278-50c8-4a19-af00-077bfc314fd2', 'Root organization B (level 1)', '2', '2019-01-01 00:00:00', '2019-01-01 00:00:00'); -- is here to verify removal of retired checklist when org is deleted

INSERT INTO checklist(municipality_id, id, organization_id, name, version, life_cycle, created, updated, last_saved_by)
VALUES ('2281', '1bc85739-cf6d-4223-b9dc-09218f044db7', '45764278-50c8-4a19-af00-077bfc314fd2', 'Deprecated checklist for organization 1 on level 3', 1, 'DEPRECATED', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '2bc85739-cf6d-4223-b9dc-09218f044db7', '45764278-50c8-4a19-af00-077bfc314fd2', 'Active checklist for organization 1 on level 3', 1, 'ACTIVE', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '3bc85739-cf6d-4223-b9dc-09218f044db7', '25764278-50c8-4a19-af00-077bfc314fd2', 'Active checklist for organization 1 on level 2', 1, 'ACTIVE', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '4bc85739-cf6d-4223-b9dc-09218f044db7', '15764278-50c8-4a19-af00-077bfc314fd2', 'Active checklist for root organization A', 1, 'ACTIVE', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2262', '5bc85739-cf6d-4223-b9dc-09218f044db7', '65764278-50c8-4a19-af00-077bfc314fd2', 'Retired checklist for root organization B', 1, 'RETIRED', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser');
       
INSERT INTO organization_communication_channel(organization_id, communication_channel)
VALUES ('15764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('25764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('35764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('45764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL');

INSERT INTO phase(municipality_id, id, name, body_text, time_to_complete, sort_order, created, updated, last_saved_by)
VALUES ('2281', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Fas 1', 'Uppgifter under fas 1', 'P1W', 1, '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Fas 2', 'Uppgifter under fas 2', 'P2W', 2, '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '3455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Fas 3', 'Uppgifter under fas 3', 'P1M', 3, '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser');

INSERT INTO task(id, heading, text, role_type, question_type, created, sort_order, updated, last_saved_by, phase_id, checklist_id)
VALUES 
       -- Deprecated list for organization 1 on level 3
       ('1ba82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 1', 'Gör A', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '1bc85739-cf6d-4223-b9dc-09218f044db7'),
       ('2ba82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 2', 'Gör B', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 2, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '1bc85739-cf6d-4223-b9dc-09218f044db7'),
       
       -- Active list for organization 1 on level 3
       ('3ba82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 1', 'Gör A', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '2bc85739-cf6d-4223-b9dc-09218f044db7'), 
       ('4ba82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 2', 'Gör B', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '2bc85739-cf6d-4223-b9dc-09218f044db7'),
       ('5ba82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 3', 'Gör C', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 3, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '2bc85739-cf6d-4223-b9dc-09218f044db7'),
       ('6ba82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 4', 'Gör D', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', '2bc85739-cf6d-4223-b9dc-09218f044db7'),
       
       -- Active list for organization 1 on level 2
       ('7ba82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 5', 'Gör E', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 2, '2019-01-01 00:00:00', 'someUser', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', '3bc85739-cf6d-4223-b9dc-09218f044db7'),
       ('8ba82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 6', 'Gör F', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', '3bc85739-cf6d-4223-b9dc-09218f044db7'),
       
       -- Active list for root organization on level 1
       ('9ba82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 7', 'Gör G', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '4bc85739-cf6d-4223-b9dc-09218f044db7'),
       ('10a82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 8', 'Gör H', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', '4bc85739-cf6d-4223-b9dc-09218f044db7'),
       ('11a82aca-f841-4257-baec-d745e3ab78bf', 'Uppg 9', 'Gör I', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '3455a5d4-1db8-4a25-a49f-92fdd0c60a14', '4bc85739-cf6d-4223-b9dc-09218f044db7');

INSERT INTO custom_sortorder (id, municipality_id, organization_number, component_type, component_id, position)
VALUES
       -- Sortorder for organization 1 on level 3
       ('001a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'PHASE', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 3),
       ('002a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'PHASE', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 2),
       ('003a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'PHASE', '3455a5d4-1db8-4a25-a49f-92fdd0c60a14', 1),
       ('004a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'TASK', '3ba82aca-f841-4257-baec-d745e3ab78bf', 9),
       ('005a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'TASK', '4ba82aca-f841-4257-baec-d745e3ab78bf', 8),
       ('006a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'TASK', '5ba82aca-f841-4257-baec-d745e3ab78bf', 7),
       ('007a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'TASK', '6ba82aca-f841-4257-baec-d745e3ab78bf', 6),
       ('008a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'TASK', '7ba82aca-f841-4257-baec-d745e3ab78bf', 5),
       ('009a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'TASK', '8ba82aca-f841-4257-baec-d745e3ab78bf', 4),
       ('010a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'TASK', '9ba82aca-f841-4257-baec-d745e3ab78bf', 3),
       ('011a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'TASK', '10a82aca-f841-4257-baec-d745e3ab78bf', 2),
       ('012a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  111, 'TASK', '11a82aca-f841-4257-baec-d745e3ab78bf', 1),
       
       -- Sortorder for organization 1 on level 2
       ('013a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'PHASE', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 1),
       ('014a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'PHASE', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 3),
       ('015a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'PHASE', '3455a5d4-1db8-4a25-a49f-92fdd0c60a14', 2),
       ('016a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'TASK', '3ba82aca-f841-4257-baec-d745e3ab78bf', 1),
       ('017a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'TASK', '4ba82aca-f841-4257-baec-d745e3ab78bf', 3),
       ('018a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'TASK', '5ba82aca-f841-4257-baec-d745e3ab78bf', 2),
       ('019a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'TASK', '6ba82aca-f841-4257-baec-d745e3ab78bf', 6),
       ('020a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'TASK', '7ba82aca-f841-4257-baec-d745e3ab78bf', 4),
       ('021a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'TASK', '8ba82aca-f841-4257-baec-d745e3ab78bf', 5),
       ('022a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'TASK', '9ba82aca-f841-4257-baec-d745e3ab78bf', 9),
       ('023a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'TASK', '10a82aca-f841-4257-baec-d745e3ab78bf', 8),
       ('024a2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  11, 'TASK', '11a82aca-f841-4257-baec-d745e3ab78bf', 7);
       