INSERT INTO organization(municipality_id, organization_number, created, updated, organization_name, id)
VALUES ('2281',  13, now(), now(), 'Sundsvalls kommun', 'cfcb03b1-7344-4352-9b72-7aebb1f235e1'),
       ('2281', 578, now(), now(), 'Sub-avdelning 578', '047c78a2-aadc-40e5-8913-8623b1fecc35');

INSERT INTO checklist(municipality_id, id, organization_id, name, VERSION, life_cycle, created, last_saved_by)
VALUES ('2281', '15764278-50c8-4a19-af00-077bfc314fd2', 'cfcb03b1-7344-4352-9b72-7aebb1f235e1', 'Övergripande checklista som ärvs av alla enheter under Svall', 1, 'ACTIVE', '2019-01-01 00:00:00', 'someUser');

INSERT INTO phase(municipality_id, id, name, body_text, time_to_complete, sort_order, created, last_saved_by)
VALUES ('2281', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Fas-1', 'Beskrivning fas-1', 'P1W', 1, '2019-01-01 00:00:00', 'someUser'),
       ('2281', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Fas-2', 'Beskrivning fas-2', 'P2W', 1, '2019-01-01 00:00:00', 'someUser');

INSERT INTO task(id, heading, text, role_type, question_type, created, sort_order, last_saved_by, checklist_id, phase_id)
VALUES ('aba82aca-f841-4257-baec-d745e3ab78bf', 'Uppgift-1', 'Beskrivning av 1', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, 'someUser', '15764278-50c8-4a19-af00-077bfc314fd2', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('bba82aca-f841-4257-baec-d745e3ab78bf', 'Uppgift-2', 'Beskrivning av 2', 'NEW_EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 1, 'someUser', '15764278-50c8-4a19-af00-077bfc314fd2', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14');

INSERT INTO custom_sortorder (id, municipality_id, organization_number, component_type, component_id, position)
VALUES -- organizational unit 13
('01ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  13, 'PHASE', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 1),
('02ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  13, 'PHASE', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 2),
('03ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  13, 'TASK', 'aba82aca-f841-4257-baec-d745e3ab78bf', 1),
('04ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281',  13, 'TASK', 'bba82aca-f841-4257-baec-d745e3ab78bf', 2),
-- organizational unit 578
('05ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'PHASE', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 1),
('06ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'PHASE', '2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 2),
('07ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'TASK', 'aba82aca-f841-4257-baec-d745e3ab78bf', 1),
('08ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 578, 'TASK', 'bba82aca-f841-4257-baec-d745e3ab78bf', 2);
