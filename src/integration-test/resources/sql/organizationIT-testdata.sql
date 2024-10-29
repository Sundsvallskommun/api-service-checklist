INSERT INTO organization(municipality_id, id, organization_name, organization_number, created, updated)
VALUES ('2281', '15764278-50c8-4a19-af00-077bfc314fd2', 'Test Organization', '1', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
       ('2281', '25764278-50c8-4a19-af00-077bfc314fd2', 'Test Organization 2', '2', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
       ('2281', '35764278-50c8-4a19-af00-077bfc314fd2', 'Test Organization 3', '3', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
       ('2281', '45764278-50c8-4a19-af00-077bfc314fd2', 'Will be deleted in test', '4', '2019-01-01 00:00:00', '2019-01-01 00:00:00');

INSERT INTO checklist(municipality_id, id, organization_id, name, version, role_type, life_cycle, created, updated)
VALUES ('2281', '4bc85739-cf6d-4223-b9dc-09218f044db7', '45764278-50c8-4a19-af00-077bfc314fd2', 'CHECKLIST_SHOULD_BE_DELETED', 1, 'EMPLOYEE', 'RETIRED', '2019-01-01 00:00:00', '2019-01-01 00:00:00');
       
INSERT INTO organization_communication_channel(organization_id, communication_channel)
VALUES ('15764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('25764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL'),
       ('35764278-50c8-4a19-af00-077bfc314fd2', 'EMAIL');