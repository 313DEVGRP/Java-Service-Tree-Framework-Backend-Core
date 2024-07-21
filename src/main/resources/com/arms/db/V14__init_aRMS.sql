ALTER TABLE `aRMS`.`T_ARMS_JIRAISSUESTATUS`
    ADD COLUMN c_issue_type_mapping_id         text null comment '클라우드 지라 이슈 상태 - 이슈 유형SET 매핑';

ALTER TABLE `aRMS`.`T_ARMS_REQSTATE`
    ADD COLUMN c_state_category_mapping_id         bigint null comment '암스 상태 카테고리 매핑 아이디';

--
-- Table structure for table `T_ARMS_REQSTATE_CATEGORY`
--

CREATE TABLE IF NOT EXISTS `aRMS`.`T_ARMS_REQSTATE_CATEGORY_LOG` (

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

    `c_category_icon`           text            COMMENT '카테고리 아이콘',
    `c_closed`                  varchar(255)    COMMENT '완료 상태 여부',
    `c_etc`                     varchar(255)    COMMENT '비고',
    `c_desc`                    text            COMMENT '설명',
    `c_contents`                longtext        COMMENT '내용'

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='T_ARMS_REQSTATE_CATEGORY_LOG';

CREATE TABLE IF NOT EXISTS `aRMS`.`T_ARMS_REQSTATE_CATEGORY` (

    `c_id`                      bigint(20) AUTO_INCREMENT primary key COMMENT '노드 아이디',
    `c_parentid`                bigint(20) NOT NULL COMMENT '부모 노드 아이디',
    `c_position`                bigint(20) NOT NULL COMMENT '노드 포지션',
    `c_left`                    bigint(20) NOT NULL COMMENT '노드 좌측 끝 포인트',
    `c_right`                   bigint(20) NOT NULL COMMENT '노드 우측 끝 포인트',
    `c_level`                   bigint(20) NOT NULL COMMENT '노드 DEPTH',
    `c_title`                   VARCHAR(255) COMMENT '노드 명',
    `c_type`                    VARCHAR(255) COMMENT '노드 타입',

    `c_category_icon`           text            COMMENT '카테고리 아이콘',
    `c_closed`                  varchar(255)    COMMENT '완료 상태 여부',
    `c_etc`                     varchar(255)    COMMENT '비고',
    `c_desc`                    text            COMMENT '설명',
    `c_contents`                longtext        COMMENT '내용'

    ) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='T_ARMS_REQSTATE_CATEGORY';

Insert into `aRMS`.`T_ARMS_REQSTATE_CATEGORY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (1, 0, 0, 1, 18, 0, 'T_ARMS_REQSTATE_CATEGORY', 'root');
Insert into `aRMS`.`T_ARMS_REQSTATE_CATEGORY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (2, 1, 0, 2, 17, 1, '요구사항 상태 카테고리 ( ARMS 전용 )', 'drive');
Insert into `aRMS`.`T_ARMS_REQSTATE_CATEGORY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_CATEGORY_ICON, C_CLOSED)
Values (3, 2, 0, 3, 4, 2, '열림', 'default', '<i class="fa fa-folder-o text-danger"></i>', 'false');
Insert into `aRMS`.`T_ARMS_REQSTATE_CATEGORY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_CATEGORY_ICON, C_CLOSED)
Values (4, 2, 1, 5, 6, 2, '진행중', 'default', '<i class="fa fa-fire" style="color: #E49400;"></i>', 'false');
Insert into `aRMS`.`T_ARMS_REQSTATE_CATEGORY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_CATEGORY_ICON, C_CLOSED)
Values (5, 2, 2, 7, 8, 2, '해결됨', 'default', '<i class="fa fa-fire-extinguisher text-success"></i>', 'true');
Insert into `aRMS`.`T_ARMS_REQSTATE_CATEGORY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_CATEGORY_ICON, C_CLOSED)
Values (6, 2, 3, 9, 10, 2, '닫힘', 'default', '<i class="fa fa-folder text-primary"></i>', 'true');

DELIMITER $$
CREATE TRIGGER TG_INSERT_T_ARMS_REQSTATE_CATEGORY
    BEFORE  INSERT ON T_ARMS_REQSTATE_CATEGORY
    FOR EACH ROW
BEGIN
    insert into T_ARMS_REQSTATE_CATEGORY_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_CATEGORY_ICON, C_CLOSED, C_METHOD, C_STATE, C_DATE)
    values (NEW.C_ID,NEW.C_PARENTID,NEW.C_POSITION,NEW.C_LEFT,NEW.C_RIGHT,NEW.C_LEVEL,NEW.C_TITLE,NEW.C_TYPE,NEW.C_CATEGORY_ICON,NEW.C_CLOSED,'update','변경이전데이터',now());
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER TG_UPDATE_T_ARMS_REQSTATE_CATEGORY
    BEFORE  UPDATE ON T_ARMS_REQSTATE_CATEGORY
    FOR EACH ROW
BEGIN
    insert into T_ARMS_REQSTATE_CATEGORY_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_CATEGORY_ICON, C_CLOSED, C_METHOD, C_STATE, C_DATE)
    values (OLD.C_ID,OLD.C_PARENTID,OLD.C_POSITION,OLD.C_LEFT,OLD.C_RIGHT,OLD.C_LEVEL,OLD.C_TITLE,OLD.C_TYPE,OLD.C_CATEGORY_ICON,OLD.C_CLOSED,'update','변경이전데이터',now());
    insert into T_ARMS_REQSTATE_CATEGORY_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_CATEGORY_ICON, C_CLOSED, C_METHOD, C_STATE, C_DATE)
    values (NEW.C_ID,NEW.C_PARENTID,NEW.C_POSITION,NEW.C_LEFT,NEW.C_RIGHT,NEW.C_LEVEL,NEW.C_TITLE,NEW.C_TYPE,NEW.C_CATEGORY_ICON,NEW.C_CLOSED,'update','변경이후데이터',now());
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER TG_DELETE_T_ARMS_REQSTATE_CATEGORY
    BEFORE  DELETE ON T_ARMS_REQSTATE_CATEGORY
    FOR EACH ROW
BEGIN
    insert into T_ARMS_REQSTATE_CATEGORY_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_CATEGORY_ICON, C_CLOSED, C_METHOD, C_STATE, C_DATE)
    values (OLD.C_ID,OLD.C_PARENTID,OLD.C_POSITION,OLD.C_LEFT,OLD.C_RIGHT,OLD.C_LEVEL,OLD.C_TITLE,OLD.C_TYPE,OLD.C_CATEGORY_ICON,OLD.C_CLOSED,'delete','삭제된데이터',now());
END $$
DELIMITER ;