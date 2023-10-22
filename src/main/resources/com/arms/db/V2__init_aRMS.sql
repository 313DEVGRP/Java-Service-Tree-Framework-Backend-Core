ALTER TABLE T_ARMS_REQADD
    ADD COLUMN c_req_total_resource bigint null comment '총 작업 MM',
    ADD COLUMN c_req_plan_resource bigint null comment '총 계획 MM',
    ADD COLUMN c_req_total_time bigint null comment '총 기간 Day',
    ADD COLUMN c_req_plan_time bigint null comment '총 계획 Day',
    ADD COLUMN c_req_plan_progress bigint null comment '계획 진척도',
    ADD COLUMN c_req_performance_progress bigint null comment '실적 진척도',
    ADD COLUMN c_req_manager text null comment '담당자',
    ADD COLUMN c_req_output text null comment '산출물';

ALTER TABLE T_ARMS_REQADD_LOG
    ADD COLUMN c_req_total_resource bigint null comment '총 작업 MM',
    ADD COLUMN c_req_plan_resource bigint null comment '총 계획 MM',
    ADD COLUMN c_req_total_time bigint null comment '총 기간 Day',
    ADD COLUMN c_req_plan_time bigint null comment '총 계획 Day',
    ADD COLUMN c_req_plan_progress bigint null comment '계획 진척도',
    ADD COLUMN c_req_performance_progress bigint null comment '실적 진척도',
    ADD COLUMN c_req_manager text null comment '담당자',
    ADD COLUMN c_req_output text null comment '산출물';
