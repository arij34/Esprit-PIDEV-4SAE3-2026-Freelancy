-- Run this SQL in MySQL to fix the tasks.status column size (required for INPROGRESS, INCOMPLETE, etc.)
-- Execute: mysql -u root challenge_db < src/main/resources/fix-task-status-column.sql
-- Or run in phpMyAdmin / MySQL Workbench:

USE challenge_db;
ALTER TABLE tasks MODIFY COLUMN status VARCHAR(20);
