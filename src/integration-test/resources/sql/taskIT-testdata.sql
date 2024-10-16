INSERT INTO organization (id, organization_name, organization_number, created, updated)
VALUES ('19dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 1', '1', '2019-01-01 00:00:00', '2019-01-01 00:00:00');

INSERT INTO checklist(id, organization_id, name, display_name, version, role_type, life_cycle, created, updated)
VALUES ('1fb37edc-eb16-4ac3-a436-02971f020b28', '19dddb61-9a7b-423f-a873-94049e17cbee', 'Checklist 1', 'Checklist 1', 1, 'EMPLOYEE',
        'ACTIVE', '2024-01-01 00:00:00', '2024-01-01 00:00:00');

INSERT INTO phase(id, name, body_text, time_to_complete, role_type, permission, checklist_id,
                  sort_order, created, updated)
VALUES ('28f2b2cc-1fc8-42ee-a752-fae751c1a858', 'Phase 1', 'This is phase 1', 'P1D', 'EMPLOYEE',
        'ADMIN', '1fb37edc-eb16-4ac3-a436-02971f020b28', 1, '2024-01-01 00:00:00',
        '2024-01-01 00:00:00');

INSERT INTO task(id, heading, text, sort_order, role_type, question_type, permission, phase_id,
                 created, updated)
VALUES ('414803ef-0074-4f24-b5e5-54c48f7c6ea9', 'Task 1', 'Task 1', 1, 'EMPLOYEE', 'YES_OR_NO',
        'ADMIN', '28f2b2cc-1fc8-42ee-a752-fae751c1a858', '2024-01-01 00:00:00',
        '2024-01-01 00:00:00'),
       ('514803ef-0074-4f24-b5e5-54c48f7c6ea9', 'Task 2', 'Task 2', 2, 'EMPLOYEE', 'YES_OR_NO',
        'ADMIN', '28f2b2cc-1fc8-42ee-a752-fae751c1a858', '2024-01-01 00:00:00',
        '2024-01-01 00:00:00');

