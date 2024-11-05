INSERT INTO organization (municipality_id, id, organization_name, organization_number, created, updated)
VALUES ('2281', '19dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 1', '1', '2019-01-01 00:00:00', '2019-01-01 00:00:00');

INSERT INTO checklist (municipality_id, id, organization_id, name, display_name, version, role_type, life_cycle, created, updated, last_saved_by)
VALUES ('2281', '1fb37edc-eb16-4ac3-a436-02971f020b28', '19dddb61-9a7b-423f-a873-94049e17cbee', 'Checklist 1', 'Checklist 1', 1, 'EMPLOYEE', 'ACTIVE', '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser');

INSERT INTO phase (id, name, body_text, time_to_complete, role_type, permission, checklist_id, sort_order, created, updated, last_saved_by)
VALUES ('18f2b2cc-1fc8-42ee-a752-fae751c1a858', 'Phase 1', 'This is phase 1', 'P1D', 'EMPLOYEE', 'ADMIN', '1fb37edc-eb16-4ac3-a436-02971f020b28', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser'),
       ('28f2b2cc-1fc8-42ee-a752-fae751c1a858', 'Phase 2', 'This is phase 2', 'P5D', 'EMPLOYEE', 'ADMIN', '1fb37edc-eb16-4ac3-a436-02971f020b28', 2, '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser');

INSERT INTO task (id, heading, TEXT, sort_order, role_type, question_type, permission, phase_id, created, updated, last_saved_by)
VALUES ('414803ef-0074-4f24-b5e5-54c48f7c6ea9', 'Task 1', 'Task 1', 1, 'EMPLOYEE', 'YES_OR_NO', 'ADMIN', '18f2b2cc-1fc8-42ee-a752-fae751c1a858', '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser'),
       ('514803ef-0074-4f24-b5e5-54c48f7c6ea9', 'Task 2', 'Task 2', 2, 'EMPLOYEE', 'YES_OR_NO', 'ADMIN', '18f2b2cc-1fc8-42ee-a752-fae751c1a858', '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser'),
       ('714803ef-0074-4f24-b5e5-54c48f7c6ea9', 'Task 1', 'Task 1', 1, 'EMPLOYEE', 'YES_OR_NO', 'ADMIN', '28f2b2cc-1fc8-42ee-a752-fae751c1a858', '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser'),
       ('814803ef-0074-4f24-b5e5-54c48f7c6ea9', 'Task 2', 'Task 2', 2, 'EMPLOYEE', 'YES_OR_NO', 'ADMIN', '28f2b2cc-1fc8-42ee-a752-fae751c1a858', '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser'),
       ('914803ef-0074-4f24-b5e5-54c48f7c6ea9', 'Task 1', 'Task 4', 4, 'EMPLOYEE', 'YES_OR_NO', 'ADMIN', '28f2b2cc-1fc8-42ee-a752-fae751c1a858', '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser'),
       ('114803ef-0074-4f24-b5e5-54c48f7c6ea9', 'Task 2', 'Task 3', 3, 'EMPLOYEE', 'YES_OR_NO', 'ADMIN', '28f2b2cc-1fc8-42ee-a752-fae751c1a858', '2024-01-01 00:00:00', '2024-01-01 00:00:00', 'someUser');
