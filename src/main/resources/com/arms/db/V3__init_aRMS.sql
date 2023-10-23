ALTER TABLE T_ARMS_REQPRIORITY
    ADD COLUMN c_desc text null comment '설명';
ALTER TABLE T_ARMS_REQPRIORITY_LOG
    ADD COLUMN c_desc text null comment '설명';

UPDATE T_ARMS_REQPRIORITY
SET C_RIGHT=14
WHERE C_ID=1;

UPDATE T_ARMS_REQPRIORITY
SET C_RIGHT=13
WHERE C_ID=2;

Insert into `aRMS`.`T_ARMS_REQPRIORITY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (3, 2, 0, 3, 4, 2, '매우 낮음', 'default');
Insert into `aRMS`.`T_ARMS_REQPRIORITY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (4, 2, 1, 5, 6, 2, '낮음', 'default');
Insert into `aRMS`.`T_ARMS_REQPRIORITY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (5, 2, 2, 7, 8, 2, '중간', 'default');
Insert into `aRMS`.`T_ARMS_REQPRIORITY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (6, 2, 3, 9, 10, 2, '높음', 'default');
Insert into `aRMS`.`T_ARMS_REQPRIORITY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (7, 2, 4, 11, 12, 2, '매우 높음', 'default');


--
-- Table structure for table `T_ARMS_REQSTATE`
--

CREATE TABLE IF NOT EXISTS `aRMS`.`T_ARMS_REQSTATE_LOG` (

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

    `c_etc`                     varchar(255)    COMMENT '비고',
    `c_desc`                    text            COMMENT '설명',
    `c_contents`                longtext        COMMENT '내용'

) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='T_ARMS_REQSTATE_LOG';

CREATE TABLE IF NOT EXISTS `aRMS`.`T_ARMS_REQSTATE` (

    `c_id`                      bigint(20) AUTO_INCREMENT primary key COMMENT '노드 아이디',
    `c_parentid`                bigint(20) NOT NULL COMMENT '부모 노드 아이디',
    `c_position`                bigint(20) NOT NULL COMMENT '노드 포지션',
    `c_left`                    bigint(20) NOT NULL COMMENT '노드 좌측 끝 포인트',
    `c_right`                   bigint(20) NOT NULL COMMENT '노드 우측 끝 포인트',
    `c_level`                   bigint(20) NOT NULL COMMENT '노드 DEPTH',
    `c_title`                   VARCHAR(255) COMMENT '노드 명',
    `c_type`                    VARCHAR(255) COMMENT '노드 타입',

    `c_etc`                     varchar(255)    COMMENT '비고',
    `c_desc`                    text            COMMENT '설명',
    `c_contents`                longtext        COMMENT '내용'

) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='T_ARMS_REQSTATE';

Insert into `aRMS`.`T_ARMS_REQSTATE` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (1, 0, 0, 1, 18, 0, 'T_ARMS_REQSTATE', 'root');
Insert into `aRMS`.`T_ARMS_REQSTATE` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (2, 1, 0, 2, 17, 1, '제품(서비스) 요구사항 상태 ( ARMS 전용 )', 'drive');
Insert into `aRMS`.`T_ARMS_REQSTATE` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (3, 2, 0, 3, 4, 2, '제안', 'default');
Insert into `aRMS`.`T_ARMS_REQSTATE` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (4, 2, 1, 5, 6, 2, '승인', 'default');
Insert into `aRMS`.`T_ARMS_REQSTATE` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (5, 2, 2, 7, 8, 2, 'A-RMS 전파', 'default');
Insert into `aRMS`.`T_ARMS_REQSTATE` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (6, 2, 3, 9, 10, 2, '구현', 'default');
Insert into `aRMS`.`T_ARMS_REQSTATE` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (7, 2, 4, 11, 12, 2, '검증', 'default');
Insert into `aRMS`.`T_ARMS_REQSTATE` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (8, 2, 5, 13, 14, 2, '삭제', 'default');
Insert into `aRMS`.`T_ARMS_REQSTATE` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (9, 2, 6, 15, 16, 2, '반려', 'default');

DELIMITER $$
CREATE TRIGGER TG_INSERT_T_ARMS_REQSTATE
    BEFORE  INSERT ON T_ARMS_REQSTATE
    FOR EACH ROW
BEGIN
    insert into T_ARMS_REQSTATE_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE)
    values (NEW.C_ID,NEW.C_PARENTID,NEW.C_POSITION,NEW.C_LEFT,NEW.C_RIGHT,NEW.C_LEVEL,NEW.C_TITLE,NEW.C_TYPE,'update','변경이전데이터',now());
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER TG_UPDATE_T_ARMS_REQSTATE
    BEFORE  UPDATE ON T_ARMS_REQSTATE
    FOR EACH ROW
BEGIN
    insert into T_ARMS_REQSTATE_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE)
    values (OLD.C_ID,OLD.C_PARENTID,OLD.C_POSITION,OLD.C_LEFT,OLD.C_RIGHT,OLD.C_LEVEL,OLD.C_TITLE,OLD.C_TYPE,'update','변경이전데이터',now());
    insert into T_ARMS_REQSTATE_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE)
    values (NEW.C_ID,NEW.C_PARENTID,NEW.C_POSITION,NEW.C_LEFT,NEW.C_RIGHT,NEW.C_LEVEL,NEW.C_TITLE,NEW.C_TYPE,'update','변경이후데이터',now());
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER TG_DELETE_T_ARMS_REQSTATE
    BEFORE  DELETE ON T_ARMS_REQSTATE
    FOR EACH ROW
BEGIN
    insert into T_ARMS_REQSTATE_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE)
    values (OLD.C_ID,OLD.C_PARENTID,OLD.C_POSITION,OLD.C_LEFT,OLD.C_RIGHT,OLD.C_LEVEL,OLD.C_TITLE,OLD.C_TYPE,'delete','삭제된데이터',now());
END $$
DELIMITER ;

--
-- Table structure for table `T_ARMS_REQDIFFICULTY`
--

CREATE TABLE IF NOT EXISTS `aRMS`.`T_ARMS_REQDIFFICULTY_LOG` (

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

    `c_etc`                     varchar(255)    COMMENT '비고',
    `c_desc`                    text            COMMENT '설명',
    `c_contents`                longtext        COMMENT '내용'

) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='T_ARMS_REQDIFFICULTY_LOG';

CREATE TABLE IF NOT EXISTS `aRMS`.`T_ARMS_REQDIFFICULTY` (

    `c_id`                      bigint(20) AUTO_INCREMENT primary key COMMENT '노드 아이디',
    `c_parentid`                bigint(20) NOT NULL COMMENT '부모 노드 아이디',
    `c_position`                bigint(20) NOT NULL COMMENT '노드 포지션',
    `c_left`                    bigint(20) NOT NULL COMMENT '노드 좌측 끝 포인트',
    `c_right`                   bigint(20) NOT NULL COMMENT '노드 우측 끝 포인트',
    `c_level`                   bigint(20) NOT NULL COMMENT '노드 DEPTH',
    `c_title`                   VARCHAR(255) COMMENT '노드 명',
    `c_type`                    VARCHAR(255) COMMENT '노드 타입',

    `c_etc`                     varchar(255)    COMMENT '비고',
    `c_desc`                    text            COMMENT '설명',
    `c_contents`                longtext        COMMENT '내용'

) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='T_ARMS_REQDIFFICULTY';

Insert into `aRMS`.`T_ARMS_REQDIFFICULTY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (1, 0, 0, 1, 14, 0, 'T_ARMS_REQDIFFICULTY', 'root');
Insert into `aRMS`.`T_ARMS_REQDIFFICULTY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (2, 1, 0, 2, 13, 1, '제품(서비스) 요구사항 난이도', 'drive');
Insert into `aRMS`.`T_ARMS_REQDIFFICULTY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (3, 2, 0, 3, 4, 2, '매우 어려움', 'default');
Insert into `aRMS`.`T_ARMS_REQDIFFICULTY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (4, 2, 1, 5, 6, 2, '어려움', 'default');
Insert into `aRMS`.`T_ARMS_REQDIFFICULTY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (5, 2, 2, 7, 8, 2, '보통', 'default');
Insert into `aRMS`.`T_ARMS_REQDIFFICULTY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (6, 2, 3, 9, 10, 2, '쉬움', 'default');
Insert into `aRMS`.`T_ARMS_REQDIFFICULTY` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (7, 2, 4, 11, 12, 2, '매우 쉬움', 'default');

DELIMITER $$
CREATE TRIGGER TG_INSERT_T_ARMS_REQDIFFICULTY
    BEFORE  INSERT ON T_ARMS_REQDIFFICULTY
    FOR EACH ROW
BEGIN
    insert into T_ARMS_REQDIFFICULTY_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE)
    values (NEW.C_ID,NEW.C_PARENTID,NEW.C_POSITION,NEW.C_LEFT,NEW.C_RIGHT,NEW.C_LEVEL,NEW.C_TITLE,NEW.C_TYPE,'update','변경이전데이터',now());
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER TG_UPDATE_T_ARMS_REQDIFFICULTY
    BEFORE  UPDATE ON T_ARMS_REQDIFFICULTY
    FOR EACH ROW
BEGIN
    insert into T_ARMS_REQDIFFICULTY_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE)
    values (OLD.C_ID,OLD.C_PARENTID,OLD.C_POSITION,OLD.C_LEFT,OLD.C_RIGHT,OLD.C_LEVEL,OLD.C_TITLE,OLD.C_TYPE,'update','변경이전데이터',now());
    insert into T_ARMS_REQDIFFICULTY_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE)
    values (NEW.C_ID,NEW.C_PARENTID,NEW.C_POSITION,NEW.C_LEFT,NEW.C_RIGHT,NEW.C_LEVEL,NEW.C_TITLE,NEW.C_TYPE,'update','변경이후데이터',now());
END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER TG_DELETE_T_ARMS_REQDIFFICULTY
    BEFORE  DELETE ON T_ARMS_REQDIFFICULTY
    FOR EACH ROW
BEGIN
    insert into T_ARMS_REQDIFFICULTY_LOG (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE, C_METHOD, C_STATE, C_DATE)
    values (OLD.C_ID,OLD.C_PARENTID,OLD.C_POSITION,OLD.C_LEFT,OLD.C_RIGHT,OLD.C_LEVEL,OLD.C_TITLE,OLD.C_TYPE,'delete','삭제된데이터',now());
END $$
DELIMITER ;
