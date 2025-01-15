INSERT INTO organization (municipality_id, id, organization_name, organization_number, created, updated)
VALUES ('2281', '19dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 1', '1', '2019-01-01 00:00:00', '2019-01-01 00:00:00');

INSERT INTO checklist (municipality_id, id, organization_id, name, display_name, version, life_cycle, created, updated, last_saved_by)
VALUES ('2281', '1fb37edc-eb16-4ac3-a436-02971f020b28', '19dddb61-9a7b-423f-a873-94049e17cbee', 'Checklist 1', 'Checklist 1', 1, 'ACTIVE', '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser');

INSERT INTO phase (municipality_id, id, name, body_text, time_to_complete, permission, sort_order, created, updated, last_saved_by)
VALUES ('2281', '28f2b2cc-1fc8-42ee-a752-fae751c1a858', 'Phase 1', 'This is phase 1', 'P1D', 'ADMIN', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser');

INSERT INTO task (id, heading, heading_reference, TEXT, sort_order, role_type, question_type, permission, phase_id, checklist_id, created, updated, last_saved_by)
VALUES ('414803ef-0074-4f24-b5e5-54c48f7c6ea9', 'Task 1', null, 'Task 1', 1, 'NEW_EMPLOYEE', 'YES_OR_NO', 'ADMIN', '28f2b2cc-1fc8-42ee-a752-fae751c1a858', '1fb37edc-eb16-4ac3-a436-02971f020b28', '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser'),
       ('514803ef-0074-4f24-b5e5-54c48f7c6ea9', 'Task 2', 'http://www.task-2.web', 'Task 2', 2, 'NEW_EMPLOYEE', 'YES_OR_NO', 'ADMIN', '28f2b2cc-1fc8-42ee-a752-fae751c1a858', '1fb37edc-eb16-4ac3-a436-02971f020b28', '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser');

INSERT INTO custom_sortorder (id, municipality_id, organization_number, component_type, component_id, position)
VALUES
('14ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 1, 'TASK', '414803ef-0074-4f24-b5e5-54c48f7c6ea9', 2), -- Task 1
('15ca2228-c49e-4e36-91c6-8e3bcb733c14', '2281', 1, 'TASK', '514803ef-0074-4f24-b5e5-54c48f7c6ea9', 1); -- Task 2

