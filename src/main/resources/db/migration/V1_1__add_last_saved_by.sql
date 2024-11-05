alter table checklist add last_saved_by varchar(255) not null after updated;
alter table custom_task add last_saved_by varchar(255) not null after updated;
alter table phase add last_saved_by varchar(255) not null after updated;
alter table task add last_saved_by varchar(255) not null after updated;
