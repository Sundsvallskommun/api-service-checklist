alter table task
    modify column heading_reference varchar(1024);

alter table custom_task
    modify column heading_reference varchar(1024);
