DELIMITER //
CREATE PROCEDURE AddDrawIOColumnsToReqAddTables()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE tableName VARCHAR(255);
    DECLARE column_exists BOOLEAN;
    DECLARE cur CURSOR FOR
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name LIKE 'T_ARMS_REQADD%';
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
BEGIN

ROLLBACK;

RESIGNAL;
END;

START TRANSACTION;
OPEN cur;
read_loop: LOOP
        FETCH cur INTO tableName;
        IF done THEN
            LEAVE read_loop;
END IF;
        SET @column_exists = (
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_name = tableName
              AND column_name = 'c_drawio_contents'
        );
        IF @column_exists = 0 THEN
            SET @alter_sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN c_drawio_contents LONGTEXT NULL COMMENT ''drawio xml'' AFTER c_req_contents');
PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END IF;
        SET @column_exists = (
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_name = tableName
              AND column_name = 'c_drawio_image_raw'
        );
        IF @column_exists = 0 THEN
            SET @alter_sql = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN c_drawio_image_raw LONGTEXT NULL COMMENT ''drawio image raw data'' AFTER c_drawio_contents');
PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END IF;
END LOOP;
CLOSE cur;
COMMIT;
END//
DELIMITER ;

CALL AddDrawIOColumnsToReqAddTables();