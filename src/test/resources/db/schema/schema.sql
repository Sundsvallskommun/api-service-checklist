
    create table checklist (
        version integer,
        created datetime(6),
        updated datetime(6),
        display_name varchar(255),
        id varchar(255) not null,
        last_saved_by varchar(255) not null,
        municipality_id varchar(255),
        name varchar(255),
        organization_id varchar(255),
        life_cycle enum ('ACTIVE','CREATED','DEPRECATED','RETIRED'),
        primary key (id)
    ) engine=InnoDB;

    create table correspondence (
        attempts integer,
        sent datetime(6),
        id varchar(255) not null,
        message_id varchar(255),
        recipient varchar(255) not null,
        communication_channel enum ('EMAIL','NO_COMMUNICATION'),
        correspondence_status enum ('ERROR','NOT_SENT','SENT','WILL_NOT_SEND'),
        primary key (id)
    ) engine=InnoDB;

    create table custom_fulfilment (
        updated datetime(6),
        custom_task_id varchar(255),
        employee_checklist_id varchar(255),
        id varchar(255) not null,
        last_saved_by varchar(255) not null,
        response_text varchar(255),
        completed enum ('EMPTY','FALSE','TRUE'),
        primary key (id)
    ) engine=InnoDB;

    create table custom_sortorder (
        organization_number integer not null,
        position integer not null,
        component_id varchar(255) not null,
        id varchar(255) not null,
        municipality_id varchar(255) not null,
        component_type enum ('PHASE','TASK') not null,
        primary key (id)
    ) engine=InnoDB;

    create table custom_task (
        sort_order integer,
        created datetime(6),
        updated datetime(6),
        employee_checklist_id varchar(255),
        heading varchar(255),
        heading_reference varchar(1024),
        id varchar(255) not null,
        last_saved_by varchar(255) not null,
        phase_id varchar(255),
        text varchar(2048),
        question_type enum ('COMPLETED_OR_NOT_RELEVANT','COMPLETED_OR_NOT_RELEVANT_WITH_TEXT','YES_OR_NO','YES_OR_NO_WITH_TEXT'),
        role_type enum ('MANAGER_FOR_NEW_EMPLOYEE','MANAGER_FOR_NEW_MANAGER','NEW_EMPLOYEE','NEW_MANAGER'),
        primary key (id)
    ) engine=InnoDB;

    create table delegate (
        email varchar(255) not null,
        employee_checklist_id varchar(255),
        first_name varchar(255) not null,
        id varchar(255) not null,
        last_name varchar(255) not null,
        manager_id varchar(255),
        party_id varchar(255) not null,
        username varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table employee (
        start_date date,
        created datetime(6),
        updated datetime(6),
        department_id varchar(255),
        email varchar(255),
        first_name varchar(255),
        id varchar(255) not null,
        last_name varchar(255),
        manager_id varchar(255),
        organization_id varchar(255),
        title varchar(255),
        username varchar(255),
        employment_position enum ('EMPLOYEE','MANAGER'),
        primary key (id)
    ) engine=InnoDB;

    create table employee_checklist (
        completed bit,
        end_date date,
        expiration_date date,
        locked bit,
        start_date date,
        created datetime(6),
        updated datetime(6),
        correspondence_id varchar(255),
        employee_id varchar(255),
        id varchar(255) not null,
        mentor_name varchar(255),
        mentor_user_id varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table fulfilment (
        updated datetime(6),
        employee_checklist_id varchar(255),
        id varchar(255) not null,
        last_saved_by varchar(255) not null,
        response_text varchar(255),
        task_id varchar(255),
        completed enum ('EMPTY','FALSE','TRUE'),
        primary key (id)
    ) engine=InnoDB;

    create table initiation_info (
        created datetime(6),
        id varchar(255) not null,
        information varchar(255),
        log_id varchar(255),
        municipality_id varchar(255),
        status varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table manager (
        created datetime(6),
        updated datetime(6),
        email varchar(255),
        first_name varchar(255),
        id varchar(255) not null,
        last_name varchar(255),
        username varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table organization (
        organization_number integer,
        created datetime(6),
        updated datetime(6),
        id varchar(255) not null,
        municipality_id varchar(255),
        organization_name varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table organization_communication_channel (
        organization_id varchar(255) not null,
        communication_channel enum ('EMAIL','NO_COMMUNICATION') not null,
        primary key (organization_id, communication_channel)
    ) engine=InnoDB;

    create table phase (
        sort_order integer,
        created datetime(6),
        updated datetime(6),
        body_text varchar(2048),
        id varchar(255) not null,
        last_saved_by varchar(255) not null,
        municipality_id varchar(255),
        name varchar(255),
        time_to_complete varchar(255),
        permission enum ('ADMIN','SUPERADMIN'),
        primary key (id)
    ) engine=InnoDB;

    create table referred_checklist (
        checklist_id varchar(255) not null,
        employee_checklist_id varchar(255) not null
    ) engine=InnoDB;

    create table task (
        sort_order integer,
        created datetime(6),
        updated datetime(6),
        checklist_id varchar(255),
        heading varchar(255),
        heading_reference varchar(1024),
        id varchar(255) not null,
        last_saved_by varchar(255) not null,
        phase_id varchar(255),
        text varchar(2048),
        permission enum ('ADMIN','SUPERADMIN'),
        question_type enum ('COMPLETED_OR_NOT_RELEVANT','COMPLETED_OR_NOT_RELEVANT_WITH_TEXT','YES_OR_NO','YES_OR_NO_WITH_TEXT'),
        role_type enum ('MANAGER_FOR_NEW_EMPLOYEE','MANAGER_FOR_NEW_MANAGER','NEW_EMPLOYEE','NEW_MANAGER'),
        primary key (id)
    ) engine=InnoDB;

    alter table if exists checklist 
       add constraint uk_checklist_name_municipality_id_version unique (name, municipality_id, version);

    create index idx_custom_sortorder_municipality_id_organization_number 
       on custom_sortorder (municipality_id, organization_number);

    alter table if exists custom_sortorder 
       add constraint uk_municipality_id_organization_number_component_id unique (municipality_id, organization_number, component_id);

    create index idx_delegate_username 
       on delegate (username);

    create index idx_delegate_first_name 
       on delegate (first_name);

    create index idx_delegate_last_name 
       on delegate (last_name);

    create index idx_delegate_email 
       on delegate (email);

    create index idx_employee_username 
       on employee (username);

    create index employee_checklist_expiration_date_locked_idx 
       on employee_checklist (expiration_date, locked);

    alter table if exists employee_checklist 
       add constraint uk_correspondence_id unique (correspondence_id);

    alter table if exists employee_checklist 
       add constraint uk_employee_id unique (employee_id);

    create index idx_manager_username 
       on manager (username);

    create index organization_number_municipality_id_idx 
       on organization (organization_number, municipality_id);

    alter table if exists organization 
       add constraint uk_organization_organization_number_municipality_id unique (organization_number, municipality_id);

    create index phase_municipality_id_idx 
       on phase (municipality_id);

    alter table if exists referred_checklist 
       add constraint uk_employee_checklist_id_checklist_id unique (employee_checklist_id, checklist_id);

    alter table if exists checklist 
       add constraint fk_organization_checklist 
       foreign key (organization_id) 
       references organization (id);

    alter table if exists custom_fulfilment 
       add constraint fk_custom_task_fulfilment_task 
       foreign key (custom_task_id) 
       references custom_task (id);

    alter table if exists custom_fulfilment 
       add constraint fk_custom_fulfilment_employee_checklist 
       foreign key (employee_checklist_id) 
       references employee_checklist (id);

    alter table if exists custom_task 
       add constraint fk_custom_task_employee_checklist 
       foreign key (employee_checklist_id) 
       references employee_checklist (id);

    alter table if exists custom_task 
       add constraint fk_custom_task_phase 
       foreign key (phase_id) 
       references phase (id);

    alter table if exists delegate 
       add constraint fk_delegate_manager 
       foreign key (manager_id) 
       references manager (id);

    alter table if exists delegate 
       add constraint fk_delegate_employee_checklist 
       foreign key (employee_checklist_id) 
       references employee_checklist (id);

    alter table if exists employee 
       add constraint fk_employee_company 
       foreign key (organization_id) 
       references organization (id);

    alter table if exists employee 
       add constraint fk_employee_department 
       foreign key (department_id) 
       references organization (id);

    alter table if exists employee 
       add constraint fk_employee_manager 
       foreign key (manager_id) 
       references manager (id);

    alter table if exists employee_checklist 
       add constraint fk_employee_checklist_correspondence 
       foreign key (correspondence_id) 
       references correspondence (id);

    alter table if exists employee_checklist 
       add constraint fk_employee_checklist_employee 
       foreign key (employee_id) 
       references employee (id);

    alter table if exists fulfilment 
       add constraint fk_fulfilment_employee_checklist 
       foreign key (employee_checklist_id) 
       references employee_checklist (id);

    alter table if exists fulfilment 
       add constraint fk_fulfilment_task 
       foreign key (task_id) 
       references task (id);

    alter table if exists organization_communication_channel 
       add constraint fk_organization_communication_channel_organization 
       foreign key (organization_id) 
       references organization (id);

    alter table if exists referred_checklist 
       add constraint fk_referred_checklist_checklist 
       foreign key (checklist_id) 
       references checklist (id);

    alter table if exists referred_checklist 
       add constraint fk_referred_checklist_employee_checklist 
       foreign key (employee_checklist_id) 
       references employee_checklist (id);

    alter table if exists task 
       add constraint fk_task_phase 
       foreign key (phase_id) 
       references phase (id);

    alter table if exists task 
       add constraint fk_checklist_task 
       foreign key (checklist_id) 
       references checklist (id);
