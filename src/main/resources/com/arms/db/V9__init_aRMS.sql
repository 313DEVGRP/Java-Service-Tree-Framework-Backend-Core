--
-- Table structure for table `GLOBAL_CONTENTS_TREE_MAP`
--

CREATE TABLE IF NOT EXISTS `aRMS`.`GLOBAL_CONTENTS_TREE_MAP` (
        `map_key` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '키',
        `pdservice_link`                        bigint(20) DEFAULT NULL COMMENT '제품(서비스)',
        `pdservicedetail_link` bigint(20) DEFAULT NULL COMMENT '제품(서비스)의 게시글',
        `filerepository_link` bigint(20) DEFAULT NULL COMMENT '파일링크',

        PRIMARY KEY (`map_key`),

        KEY `pdservice_link` (`pdservice_link`),
        KEY `pdservicedetail_link` (`pdservicedetail_link`),
        KEY `filerepository_link` (`filerepository_link`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='컨텐츠 트리 맵';

--
-- Table structure for table `T_ARMS_PDSERVICE_DETAIL`
--

CREATE TABLE IF NOT EXISTS `aRMS`.`T_ARMS_PDSERVICE_DETAIL` (

     `c_id`                      bigint(20) AUTO_INCREMENT primary key COMMENT '노드 아이디',
    `c_parentid`                bigint(20) NOT NULL COMMENT '부모 노드 아이디',
    `c_position`                bigint(20) NOT NULL COMMENT '노드 포지션',
    `c_left`                    bigint(20) NOT NULL COMMENT '노드 좌측 끝 포인트',
    `c_right`                   bigint(20) NOT NULL COMMENT '노드 우측 끝 포인트',
    `c_level`                   bigint(20) NOT NULL COMMENT '노드 DEPTH',
    `c_title`                   VARCHAR(255) COMMENT '노드 명',
    `c_type`                    VARCHAR(255) COMMENT '노드 타입',

    `c_contents`    longtext        COMMENT '내용',

    `c_created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `c_updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `c_deleted_at` TIMESTAMP NULL DEFAULT NULL

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='제품(서비스) 게시글';


Insert into `aRMS`.`T_ARMS_PDSERVICE_DETAIL` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (1, 0, 0, 1, 4, 0, 'T_ARMS_PDSERVICE_DETAIL', 'root');
Insert into `aRMS`.`T_ARMS_PDSERVICE_DETAIL` (C_ID, C_PARENTID, C_POSITION, C_LEFT, C_RIGHT, C_LEVEL, C_TITLE, C_TYPE)
Values (2, 1, 0, 2, 3, 1, '제품(서비스) 게시글', 'drive');
