INSERT INTO correspondence(id, message_id, recipient, attempts, correspondence_status, communication_channel, sent)
VALUES ('17989a99-dbee-4a1c-b3a7-c5225e1fd64f', '16e1db89-fa23-4c3c-9d7d-9eed89b99d37', 'Recipient@email.com', 1, 'SENT', 'EMAIL', '2024-01-01 00:00:00'),
       ('27989a99-dbee-4a1c-b3a7-c5225e1fd64f', '26e1db89-fa23-4c3c-9d7d-9eed89b99d37', 'Recipient@email.com', 1, 'SENT', 'EMAIL', '2024-01-01 00:00:00'),
       ('37989a99-dbee-4a1c-b3a7-c5225e1fd64f', '36e1db89-fa23-4c3c-9d7d-9eed89b99d37', 'Recipient@email.com', 1, 'SENT', 'EMAIL', '2024-01-01 00:00:00');

INSERT INTO organization(municipality_id, id, organization_name, organization_number, created, updated)
VALUES ('2281', '187933cd-45f2-4aad-8048-d2514bf49037', 'Sundsvall API Stuga', '1234', '2024-01-01 00:00:00', '2024-01-02 00:00:00'),
       ('2281', '12236b39-d094-40d0-b25d-9f3722a229e5', 'Sundsvall API Stuga A', '123', '2024-01-01 00:00:00', '2024-01-02 00:00:00'),
       ('2281', '22236b39-d094-40d0-b25d-9f3722a229e5', 'Sundsvall API Stuga B', '321', '2024-01-01 00:00:00', '2024-01-02 00:00:00');

INSERT INTO manager(id, first_name, last_name, username, email, created, updated)
VALUES ('1273fadb-0455-455e-a5e6-0eebda329867', 'Kalle', 'Anka', 'kalleanka', 'kalleanka@email.com', '2024-01-01 00:00:00', '2024-01-02 00:00:00');

INSERT INTO employee(id, first_name, last_name, email, username, title, employment_position, start_date, created, updated, organization_id, department_id, manager_id)
VALUES ('1f183588-48ef-4287-9725-4963997c817d', 'John', 'Doe', 'johndoe@email.com', 'johndoe', 'title', 'EMPLOYEE', '2021-03-01 00:00:00', '2020-01-01 00:00:00', '2024-01-01 00:00:00', '187933cd-45f2-4aad-8048-d2514bf49037', '12236b39-d094-40d0-b25d-9f3722a229e5', '1273fadb-0455-455e-a5e6-0eebda329867'),
       ('2f183588-48ef-4287-9725-4963997c817d', 'Johnny', 'Poe', 'johnnypoe@email.com', 'johnnypoe', 'title', 'EMPLOYEE', '2021-03-01 00:00:00', '2020-01-01 00:00:00', '2024-01-01 00:00:00', '187933cd-45f2-4aad-8048-d2514bf49037', '12236b39-d094-40d0-b25d-9f3722a229e5', '1273fadb-0455-455e-a5e6-0eebda329867'),
       ('3f183588-48ef-4287-9725-4963997c817d', 'Johanna', 'Soe', 'johannasoe@email.com', 'johannasoe', 'title', 'MANAGER', '2021-03-01 00:00:00', '2020-01-01 00:00:00', '2024-01-01 00:00:00', '187933cd-45f2-4aad-8048-d2514bf49037', '22236b39-d094-40d0-b25d-9f3722a229e5', '1273fadb-0455-455e-a5e6-0eebda329867');

INSERT INTO checklist(municipality_id, id, name, display_name, version, life_cycle, created, updated, last_saved_by)
VALUES ('2281', '113ec51b-f122-4b1a-a040-ed38c7a6656e', 'checklist1', 'Checklist', 1, 'ACTIVE', '2024-01-01 00:00:00', '2024-01-02 00:00:00', 'someUser'),
       ('2281', '213ec51b-f122-4b1a-a040-ed38c7a6656e', 'checklist2', 'Checklist', 1, 'ACTIVE', '2024-01-01 00:00:00', '2024-01-02 00:00:00', 'someUser'),
       ('2281', '313ec51b-f122-4b1a-a040-ed38c7a6656e', 'checklist3', 'Checklist', 1, 'ACTIVE', '2024-01-01 00:00:00', '2024-01-02 00:00:00', 'someUser');

INSERT INTO employee_checklist(id, created, updated, start_date, end_date, expiration_date, locked, employee_id, correspondence_id)
VALUES ('1fb37edc-eb16-4ac3-a436-02971f020b28', '2019-01-01 00:00:00', '2024-01-01 00:00:00', '2024-02-01 00:00:00', '2025-01-01 00:00:00', '2024-10-01 00:00:00', false, '1f183588-48ef-4287-9725-4963997c817d', '17989a99-dbee-4a1c-b3a7-c5225e1fd64f'),
       ('2fb37edc-eb16-4ac3-a436-02971f020b28', '2019-01-01 00:00:00', '2024-01-01 00:00:00', '2024-02-01 00:00:00', '2025-01-01 00:00:00', '2024-10-01 00:00:00', false, '2f183588-48ef-4287-9725-4963997c817d', '27989a99-dbee-4a1c-b3a7-c5225e1fd64f'),
       ('3fb37edc-eb16-4ac3-a436-02971f020b28', '2019-01-01 00:00:00', '2024-01-01 00:00:00', '2024-02-01 00:00:00', '2025-01-01 00:00:00', '2024-10-01 00:00:00', false, '3f183588-48ef-4287-9725-4963997c817d', '37989a99-dbee-4a1c-b3a7-c5225e1fd64f');

INSERT INTO referred_checklist (employee_checklist_id, checklist_id)
VALUES ('1fb37edc-eb16-4ac3-a436-02971f020b28', '113ec51b-f122-4b1a-a040-ed38c7a6656e'),
       ('2fb37edc-eb16-4ac3-a436-02971f020b28', '213ec51b-f122-4b1a-a040-ed38c7a6656e'),
       ('3fb37edc-eb16-4ac3-a436-02971f020b28', '313ec51b-f122-4b1a-a040-ed38c7a6656e');
