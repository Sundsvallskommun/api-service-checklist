create table initiation_info
(
    id          varchar(255) not null,
    log_id      varchar(255),
    information varchar(255),
    status      varchar(255),
    created     datetime(6),
    primary key (id)
) engine = InnoDB;
