set foreign_key_checks = 0;

truncate table employee_checklist;
truncate table correspondence;
truncate table checklist;
truncate table phase;
truncate table task;
truncate table custom_fulfilment;
truncate table custom_task;
truncate table employee;
truncate table manager;
truncate table delegate;
truncate table organization;
truncate table organization_communication_channel;
truncate table fulfilment;

set foreign_key_checks = 1;