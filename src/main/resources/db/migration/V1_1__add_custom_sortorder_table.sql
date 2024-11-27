    create table custom_sortorder (
        organization_number integer not null,
        position integer not null,
        component_id varchar(255) not null,
        id varchar(255) not null,
        municipality_id varchar(255) not null,
        component_type enum ('PHASE','TASK') not null,
        primary key (id)
    ) engine=InnoDB;

    create index idx_custom_sortorder_municipality_id_organization_number 
       on custom_sortorder (municipality_id, organization_number);

    alter table if exists custom_sortorder 
       add constraint uk_municipality_id_organization_number_component_id unique (municipality_id, organization_number, component_id);
