-- Drop the user table from challenge_db (created for testing)
-- Run this in phpMyAdmin, MySQL Workbench, or: mysql -u root challenge_db < drop-user-table.sql

USE challenge_db;

DROP TABLE IF EXISTS user;
