SET foreign_key_checks = 0;

TRUNCATE TABLE referred_checklist;
TRUNCATE TABLE employee_checklist;
TRUNCATE TABLE correspondence;
TRUNCATE TABLE checklist;
TRUNCATE TABLE phase;
TRUNCATE TABLE task;
TRUNCATE TABLE custom_fulfilment;
TRUNCATE TABLE custom_task;
TRUNCATE TABLE employee;
TRUNCATE TABLE manager;
TRUNCATE TABLE delegate;
TRUNCATE TABLE organization;
TRUNCATE TABLE organization_communication_channel;
TRUNCATE TABLE fulfilment;

SET foreign_key_checks = 1;