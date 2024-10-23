INSERT INTO organization (municipality_id, id, organization_name, organization_number, created, updated)
VALUES ('2281', '19dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 1', '1', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
        ('2281', '29dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 2', '2', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
        ('2281', '39dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 3', '3', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
        ('2281', '49dddb61-9a7b-423f-a873-94049e17cbee', 'Test Organization 4', '4', '2019-01-01 00:00:00', '2019-01-01 00:00:00');

INSERT INTO checklist(municipality_id, id, organization_id, name, version, display_name, role_type, life_cycle, created, updated)
VALUES ('2281', '15764278-50c8-4a19-af00-077bfc314fd2', '19dddb61-9a7b-423f-a873-94049e17cbee', 'CHECKLIST_ELNAT', 1, 'Checklista för Elnät', 'EMPLOYEE', 'CREATED', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
       ('2281', '25764278-50c8-4a19-af00-077bfc314fd2', '29dddb61-9a7b-423f-a873-94049e17cbee', 'CHECKLIST_VOO', 1, 'Checklista för Vård och omsorg', 'EMPLOYEE', 'ACTIVE', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
       ('2281', '35764278-50c8-4a19-af00-077bfc314fd2', '39dddb61-9a7b-423f-a873-94049e17cbee', 'BOSS_LIST', 1, 'Cheflista', 'MANAGER', 'RETIRED', '2019-01-01 00:00:00', '2019-01-01 00:00:00'),
       ('2281', '45764278-50c8-4a19-af00-077bfc314fd2', '49dddb61-9a7b-423f-a873-94049e17cbee', 'INACTIVE_LIST', 1, 'Inaktiv lista', 'MANAGER', 'CREATED', '2019-01-01 00:00:00', '2019-01-01 00:00:00');

INSERT INTO phase(id, name, body_text, time_to_complete, role_type, sort_order, created, updated, checklist_id)
VALUES ('1455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Första veckan', 'Dessa uppgifter ska göras första veckan', 'P1W', 'EMPLOYEE', 1, '2019-01-01 00:00:00', '2019-01-01 00:00:00', '15764278-50c8-4a19-af00-077bfc314fd2'), 
       ('2455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Första veckan', 'Dessa uppgifter ska göras andra veckan', 'P2W', 'EMPLOYEE', 3, '2019-01-01 00:00:00', '2019-01-01 00:00:00', '15764278-50c8-4a19-af00-077bfc314fd2'),
       ('3455a5d4-1db8-4a25-a49f-92fdd0c60a14', 'Första veckan', 'Dessa uppgifter ska göras första månaden', 'P1M', 'EMPLOYEE', 2, '2019-01-01 00:00:00', '2019-01-01 00:00:00', '15764278-50c8-4a19-af00-077bfc314fd2');

INSERT INTO task(id, heading, text, role_type, question_type, created, sort_order, updated, phase_id)
VALUES ('aba82aca-f841-4257-baec-d745e3ab78bf', 'Fika', 'Bjud på hembakat fika', 'EMPLOYEE', 'YES_OR_NO', '2019-01-01 00:00:00', 2, '2019-01-01 00:00:00', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('bba82aca-f841-4257-baec-d745e3ab78bf', 'Allergi', 'Har du några allergier?', 'EMPLOYEE', 'YES_OR_NO_WITH_TEXT', '2019-01-01 00:00:00', 3, '2019-01-01 00:00:00', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14'),
       ('cba82aca-f841-4257-baec-d745e3ab78bf', 'Lunch', 'Boka upp social lunch med chefen', 'EMPLOYEE', 'COMPLETED_OR_NOT_RELEVANT_WITH_TEXT', '2019-01-01 00:00:00', 1, '2019-01-01 00:00:00', '1455a5d4-1db8-4a25-a49f-92fdd0c60a14');

