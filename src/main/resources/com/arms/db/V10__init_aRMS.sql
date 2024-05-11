ALTER TABLE `aRMS`.`T_ARMS_PDSERVICE_DETAIL` DROP COLUMN `c_deleted_at`;

ALTER TABLE `aRMS`.`T_ARMS_PDSERVICE_DETAIL`
    ADD COLUMN `c_drawio_contents` LONGTEXT NULL COMMENT 'drawio xml' AFTER `c_contents`,
    ADD COLUMN `c_drawdb_contents` LONGTEXT NULL COMMENT 'drawdb xml' AFTER `c_drawio_contents`;

