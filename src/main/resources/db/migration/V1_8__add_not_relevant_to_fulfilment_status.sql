alter table fulfilment
    modify column completed enum ('EMPTY','FALSE','TRUE','NOT_RELEVANT');

alter table custom_fulfilment
    modify column completed enum ('EMPTY','FALSE','TRUE','NOT_RELEVANT');

alter table task
    add column optional bit not null default 0;
