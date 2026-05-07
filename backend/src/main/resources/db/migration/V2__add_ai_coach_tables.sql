create table ai_coach_sessions (
    id bigint auto_increment primary key,
    session_key varchar(64) not null unique,
    student_fk_id bigint not null,
    status varchar(32) not null,
    started_at timestamp not null default current_timestamp,
    last_interaction_at timestamp not null default current_timestamp,
    constraint fk_ai_coach_session_student foreign key (student_fk_id) references students (id)
);

create table ai_coach_events (
    id bigint auto_increment primary key,
    session_id bigint not null,
    sender varchar(32) not null,
    message_summary text not null,
    risk_flag boolean not null default false,
    risk_level varchar(16) not null,
    safety_action varchar(128) not null,
    created_at timestamp not null default current_timestamp,
    constraint fk_ai_coach_event_session foreign key (session_id) references ai_coach_sessions (id)
);

create index idx_ai_coach_sessions_student on ai_coach_sessions (student_fk_id, last_interaction_at);
create index idx_ai_coach_events_session on ai_coach_events (session_id, created_at);
