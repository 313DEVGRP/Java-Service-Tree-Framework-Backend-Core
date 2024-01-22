ALTER TABLE T_ARMS_REQSTATUS
    ADD COLUMN c_req_pdservice_versionset_link text null comment '이슈 생성된 버전 목록';

ALTER TABLE T_ARMS_REQSTATUS_LOG
    ADD COLUMN c_req_pdservice_versionset_link text null comment '이슈 생성된 버전 목록';