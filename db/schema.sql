create schema if not exists sh;

create table sh.mappings
(
	pk serial not null,
	key VARCHAR(16) not null,
	value VARCHAR(255) not null
);

comment on table sh.mappings is 'Link Mappings';

create unique index mappings_key_uindex
	on sh.mappings (key);

create unique index mappings_pk_uindex
	on sh.mappings (pk);

create unique index mappings_value_uindex
	on sh.mappings (value);

alter table sh.mappings
	add constraint mappings_pk
		primary key (pk);

