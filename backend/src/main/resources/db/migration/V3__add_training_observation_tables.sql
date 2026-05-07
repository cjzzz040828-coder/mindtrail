create table training_observation_sessions (
    id bigint auto_increment primary key,
    session_key varchar(64) not null unique,
    student_fk_id bigint not null,
    source_type varchar(32) not null,
    scene_code varchar(64) not null,
    indicator_summary varchar(255) not null,
    raw_video_saved boolean not null default false,
    status varchar(32) not null,
    started_at timestamp not null default current_timestamp,
    finished_at timestamp not null default current_timestamp,
    created_at timestamp not null default current_timestamp,
    constraint fk_training_observation_session_student foreign key (student_fk_id) references students (id)
);

create table training_observation_features (
    id bigint auto_increment primary key,
    session_id bigint not null,
    feature_code varchar(64) not null,
    feature_label varchar(128) not null,
    confidence_level varchar(16) not null,
    observation_summary varchar(255) not null,
    created_at timestamp not null default current_timestamp,
    constraint fk_training_observation_feature_session foreign key (session_id) references training_observation_sessions (id)
);

create index idx_training_observation_sessions_student_time on training_observation_sessions (student_fk_id, started_at);
create index idx_training_observation_features_session on training_observation_features (session_id, created_at);
