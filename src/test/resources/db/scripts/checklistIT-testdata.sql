INSERT INTO organization (municipality_id, id, organization_name, organization_number, created, updated)
VALUES ('2281', '19dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 1', '1', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
        ('2281', '29dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 2', '2', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
        ('2281', '39dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 3', '3', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
        ('2281', '49dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 4', '4', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
        ('2281', '59dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 5', '5', '2019-01-01 00:00:00', '2019-01-01 00:00:00');

INSERT INTO checklist(municipality_id, id, organization_id, name, version, display_name, life_cycle, created, updated, last_saved_by)
VALUES ('2281', '15764278-50c8-4a19-af00-077bfc314fd2', '19dddb61-9a7b-423f-a873-94049e17cbee', 'CHECKLIST_ELNAT', 1, 'Checklista för Elnät', 'CREATED', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '25764278-50c8-4a19-af00-077bfc314fd2', '29dddb61-9a7b-423f-a873-94049e17cbee', 'CHECKLIST_VOO', 1, 'Checklista för Vård och omsorg', 'ACTIVE', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '35764278-50c8-4a19-af00-077bfc314fd2', '39dddb61-9a7b-423f-a873-94049e17cbee', 'RETIRED_LIST', 1, 'Pensionerad checklista', 'RETIRED', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '45764278-50c8-4a19-af00-077bfc314fd2', '49dddb61-9a7b-423f-a873-94049e17cbee', 'LIST_FOR_ACTIVATION', 1, 'Inaktiv lista', 'CREATED', '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser');

INSERT INTO phase(municipality_id, id, name, body_text, time_to_complete, sort_order, created, updated, last_saved_by)
VALUES ('2281', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Första veckan', 'Dessa uppgifter ska göras första veckan', 'P1W', 1, '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Andra veckan', 'Dessa uppgifter ska göras andra veckan', 'P2W', 3, '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser'),
       ('2281', '3455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Tredje veckan', 'Dessa uppgifter ska göras tredje veckan', 'P3W', 2, '2019-01-01 00:00:00', '2019-01-01 00:00:00', 'someUser');

INSERT INTO task(id, heading, text, role_type, question_type, created, sort_order, updated, last_saved_by, phase_id, checklist_id)
VALUES ('aba82aca-f841-4257-baec-d745e3ab78bf', 'Fika', 'Bjud på hembakat fika', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 2, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '15764278-50c8-4a19-af00-077bfc314fd2'),
       ('bba82aca-f841-4257-baec-d745e3ab78bf', 'Allergi', 'Har du några allergier?', 'NEW_EMPLOYEE', 'YES_OR_NO_WITH_TEXT', '2019-01-01 00:00:00', 3, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '15764278-50c8-4a19-af00-077bfc314fd2'),
       ('cba82aca-f841-4257-baec-d745e3ab78bf', 'Lunch', 'Boka upp social lunch med chefen', 'NEW_EMPLOYEE', 'COMPLETED_OR_NOT_RELEVANT_WITH_TEXT', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '15764278-50c8-4a19-af00-077bfc314fd2'),
       ('dba82aca-f841-4257-baec-d745e3ab78bf', 'Fika', 'Bjud på hembakat fika', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 2, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '35764278-50c8-4a19-af00-077bfc314fd2'),
       ('eba82aca-f841-4257-baec-d745e3ab78bf', 'Allergi', 'Har du några allergier?', 'NEW_EMPLOYEE', 'YES_OR_NO_WITH_TEXT', '2019-01-01 00:00:00', 3, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '35764278-50c8-4a19-af00-077bfc314fd2'),
       ('fba82aca-f841-4257-baec-d745e3ab78bf', 'Lunch', 'Boka upp social lunch med chefen', 'NEW_EMPLOYEE', 'COMPLETED_OR_NOT_RELEVANT_WITH_TEXT', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', 'someUser', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', '35764278-50c8-4a19-af00-077bfc314fd2');


INSERT INTO custom_sortorder (id, municipality_id, organization_number, component_type, component_id, position)
VALUES -- organizational unit 3
('11ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 3, 'PHASE', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 3), -- Första veckan
('12ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 3, 'PHASE', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 1), -- Andra veckan
('13ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 3, 'PHASE', '3455a5d4-1db8-4a25-a49f-92fdd0c60a14', 2), -- Tredje veckan
('14ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 3, 'TASK', 'dba82aca-f841-4257-baec-d745e3ab78bf', 3), -- Fika
('15ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 3, 'TASK', 'eba82aca-f841-4257-baec-d745e3ab78bf', 1), -- Allergi
('16ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 3, 'TASK', 'fba82aca-f841-4257-baec-d745e3ab78bf', 2); -- Lunch
