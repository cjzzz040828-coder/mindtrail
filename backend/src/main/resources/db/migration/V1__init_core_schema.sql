create table schools (
    id bigint auto_increment primary key,
    school_code varchar(64) not null unique,
    school_name varchar(128) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table school_classes (
    id bigint auto_increment primary key,
    school_id bigint not null,
    class_name varchar(128) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint uk_school_classes_school_name unique (school_id, class_name),
    constraint fk_school_classes_school foreign key (school_id) references schools (id)
);

create table students (
    id bigint auto_increment primary key,
    school_id bigint not null,
    class_id bigint not null,
    student_id varchar(64) not null,
    student_name varchar(128) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint uk_students_school_student unique (school_id, student_id),
    constraint fk_students_school foreign key (school_id) references schools (id),
    constraint fk_students_class foreign key (class_id) references school_classes (id)
);

create table consent_records (
    id bigint auto_increment primary key,
    student_fk_id bigint not null,
    version varchar(32) not null,
    guardian_consent boolean not null default false,
    student_assent boolean not null default false,
    camera_training_consent boolean not null default false,
    avatar_consent boolean not null default false,
    submitted_at timestamp not null default current_timestamp,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint uk_consent_student_version unique (student_fk_id, version),
    constraint fk_consent_student foreign key (student_fk_id) references students (id)
);

create table screening_submissions (
    id bigint auto_increment primary key,
    student_fk_id bigint not null,
    sleep_score integer not null,
    stress_score integer not null,
    answers_json text not null,
    note_summary text,
    risk_level varchar(16) not null,
    trend varchar(32) not null,
    submitted_at timestamp not null default current_timestamp,
    constraint fk_screening_student foreign key (student_fk_id) references students (id)
);

create table training_plans (
    id bigint auto_increment primary key,
    student_fk_id bigint not null,
    plan_date date not null,
    encouragement varchar(255) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint uk_training_plan_student_date unique (student_fk_id, plan_date),
    constraint fk_training_plan_student foreign key (student_fk_id) references students (id)
);

create table training_tasks (
    id bigint auto_increment primary key,
    plan_id bigint not null,
    task_key varchar(64) not null,
    title varchar(128) not null,
    duration_label varchar(64) not null,
    status varchar(32) not null,
    sort_order integer not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint uk_training_task_plan_key unique (plan_id, task_key),
    constraint fk_training_task_plan foreign key (plan_id) references training_plans (id)
);

create table alert_cases (
    id bigint auto_increment primary key,
    alert_key varchar(64) not null unique,
    student_fk_id bigint,
    student_name varchar(128) not null,
    class_name varchar(128) not null,
    risk_level varchar(16) not null,
    summary varchar(255) not null,
    status varchar(32) not null,
    reason text not null,
    suggested_actions text not null,
    privacy_notice varchar(255) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    constraint fk_alert_student foreign key (student_fk_id) references students (id)
);

create table audit_logs (
    id bigint auto_increment primary key,
    actor_type varchar(32) not null,
    actor_id varchar(128) not null,
    action varchar(128) not null,
    target_type varchar(64) not null,
    target_id varchar(128) not null,
    metadata_json text,
    created_at timestamp not null default current_timestamp
);

create index idx_consent_student on consent_records (student_fk_id);
create index idx_screening_student_time on screening_submissions (student_fk_id, submitted_at);
create index idx_training_student_date on training_plans (student_fk_id, plan_date);
create index idx_alert_status_risk on alert_cases (status, risk_level);

insert into alert_cases (
    alert_key,
    student_name,
    class_name,
    risk_level,
    summary,
    status,
    reason,
    suggested_actions,
    privacy_notice
) values
(
    'alert-demo-1',
    '林同学',
    '七年级2班',
    'HIGH',
    '自评文本出现危机表达',
    '待处理',
    '近两天压力显著升高，并出现明显自伤倾向表达。',
    '联系家长|联系学校心理老师|安排线下复核',
    '仅展示必要摘要信息，完整隐私内容默认不可见。'
),
(
    'alert-demo-2',
    '王同学',
    '七年级1班',
    'MEDIUM',
    '连续 5 天未完成训练',
    '处理中',
    '训练完成度持续下降，建议老师进行温和询问。',
    '发送关怀提醒|观察后续趋势|必要时联系心理老师',
    '仅展示必要摘要信息，完整隐私内容默认不可见。'
),
(
    'alert-demo-3',
    '周同学',
    '七年级3班',
    'LOW',
    '睡眠波动',
    '继续观察',
    '近期睡眠分数有波动，但暂未达到高风险阈值。',
    '继续观察|鼓励完成放松训练',
    '仅展示必要摘要信息，完整隐私内容默认不可见。'
);
