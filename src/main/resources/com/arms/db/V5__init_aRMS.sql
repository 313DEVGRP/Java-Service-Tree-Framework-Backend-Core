--
-- Table structure for table `T_ARMS_ANNUAL_INCOME`
--
CREATE TABLE IF NOT EXISTS `aRMS`.`T_ARMS_ANNUAL_INCOME` (

    `c_id`                                  bigint(20) AUTO_INCREMENT primary key COMMENT '노드 아이디',
    `c_parentid`                            bigint(20) NOT NULL COMMENT '부모 노드 아이디',
    `c_position`                            bigint(20) NOT NULL COMMENT '노드 포지션',
    `c_left`                                bigint(20) NOT NULL COMMENT '노드 좌측 끝 포인트',
    `c_right`                               bigint(20) NOT NULL COMMENT '노드 우측 끝 포인트',
    `c_level`                               bigint(20) NOT NULL COMMENT '노드 DEPTH',
    `c_title`                               VARCHAR(255) COMMENT '노드 명',
    `c_type`                                VARCHAR(255) COMMENT '노드 타입',

    `c_name`                                VARCHAR(255) default NULL COMMENT '이름',
    `c_key`                                 VARCHAR(255) default NULL COMMENT '지라 고유 키',
    `c_annual_income`                       VARCHAR(255) default NULL COMMENT '연봉'

) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='인력별 연봉 정보';

Insert into `aRMS`.`T_ARMS_ANNUAL_INCOME` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (1, 0, 0, 1, 4, 0, 'T_ARMS_ANNUAL_INCOME', 'root');
Insert into `aRMS`.`T_ARMS_ANNUAL_INCOME` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (2, 1, 0, 2, 3, 1, '인력별 연봉 정보', 'drive');