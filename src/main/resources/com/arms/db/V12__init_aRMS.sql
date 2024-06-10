ALTER TABLE `aRMS`.`T_ARMS_JIRAISSUESTATUS`
    ADD COLUMN c_req_state_mapping_link         bigint null comment 'ALM 상태 - 요구사항 상태 매핑';

ALTER TABLE `aRMS`.`T_ARMS_JIRASERVER`
    ADD COLUMN c_server_contents_text_formatting_type         text null comment 'ALM 서버 본문 형식 유형';