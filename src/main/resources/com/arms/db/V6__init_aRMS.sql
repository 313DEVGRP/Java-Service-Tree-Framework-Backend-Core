--
-- Table structure for table `T_ARMS_ANNUAL_INCOME`
--

CREATE TABLE IF NOT EXISTS `aRMS`.`T_ARMS_ANNUAL_INCOME_LOG` (

                                                             `c_id`                      bigint(20) NOT NULL COMMENT '노드 아이디',
    `c_parentid`                bigint(20) NOT NULL COMMENT '부모 노드 아이디',
    `c_position`                bigint(20) NOT NULL COMMENT '노드 포지션',
    `c_left`                    bigint(20) NOT NULL COMMENT '노드 좌측 끝 포인트',
    `c_right`                   bigint(20) NOT NULL COMMENT '노드 우측 끝 포인트',
    `c_level`                   bigint(20) NOT NULL COMMENT '노드 DEPTH',
    `c_title`                   VARCHAR(255) COMMENT '노드 명',
    `c_type`                    VARCHAR(255) COMMENT '노드 타입',

    `c_method`                  text NULL COMMENT '노드 변경 행위',
    `c_state`                   text NULL COMMENT '노드 상태값 ( 이전인지. 이후인지)',
    `c_date`                    DATETIME NULL COMMENT '노드 변경 시',

    `c_name`                                VARCHAR(255) default NULL COMMENT '이름',
    `c_key`                                 VARCHAR(255) default NULL COMMENT '지라 고유 키',
    `c_annual_income`                       VARCHAR(255) default NULL COMMENT '연봉'

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='인력별 연봉 정보 트리거 로그';

DELIMITER $$
CREATE TRIGGER TG_INSERT_T_ARMS_ANNUAL_INCOME
    BEFORE  INSERT ON T_ARMS_ANNUAL_INCOME
    FOR EACH ROW
BEGIN
    insert into T_ARMS_ANNUAL_INCOME_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE, C_NAME, C_KEY, C_ANNUAL_INCOME)
    values (NEW.C_ID,NEW.C_PARENTID,NEW.C_POSITION,NEW.C_LEFT,NEW.C_RIGHT,NEW.C_LEVEL,NEW.C_TITLE,NEW.C_TYPE,'update','변경이전데이터',now(), NEW.C_NAME, NEW.C_KEY, NEW.C_ANNUAL_INCOME);
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER TG_UPDATE_T_ARMS_ANNUAL_INCOME
    BEFORE  UPDATE ON T_ARMS_ANNUAL_INCOME
    FOR EACH ROW
BEGIN
    insert into T_ARMS_ANNUAL_INCOME_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE, C_NAME, C_KEY, C_ANNUAL_INCOME)
    values (OLD.C_ID,OLD.C_PARENTID,OLD.C_POSITION,OLD.C_LEFT,OLD.C_RIGHT,OLD.C_LEVEL,OLD.C_TITLE,OLD.C_TYPE,'update','변경이전데이터',now(), OLD.C_NAME, OLD.C_KEY, OLD.C_ANNUAL_INCOME);
    insert into T_ARMS_ANNUAL_INCOME_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE, C_NAME, C_KEY, C_ANNUAL_INCOME)
    values (NEW.C_ID,NEW.C_PARENTID,NEW.C_POSITION,NEW.C_LEFT,NEW.C_RIGHT,NEW.C_LEVEL,NEW.C_TITLE,NEW.C_TYPE,'update','변경이후데이터',now(), NEW.C_NAME, NEW.C_KEY, NEW.C_ANNUAL_INCOME);
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER TG_DELETE_T_ARMS_ANNUAL_INCOME
    BEFORE  DELETE ON T_ARMS_ANNUAL_INCOME
    FOR EACH ROW
BEGIN
    insert into T_ARMS_ANNUAL_INCOME_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE, C_NAME, C_KEY, C_ANNUAL_INCOME)
    values (OLD.C_ID,OLD.C_PARENTID,OLD.C_POSITION,OLD.C_LEFT,OLD.C_RIGHT,OLD.C_LEVEL,OLD.C_TITLE,OLD.C_TYPE,'delete','삭제된데이터',now(), OLD.C_NAME, OLD.C_KEY, OLD.C_ANNUAL_INCOME);
END $$
DELIMITER ;