-- Initialize databases for MobileDb
CREATE DATABASE IF NOT EXISTS Kisenyi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS Customer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Optional: create a dedicated application user instead of using root
-- CREATE USER 'mobileapp'@'%' IDENTIFIED BY 'change_me';
-- GRANT ALL PRIVILEGES ON Kisenyi.* TO 'mobileapp'@'%';
-- GRANT ALL PRIVILEGES ON Customer.* TO 'mobileapp'@'%';
-- FLUSH PRIVILEGES;
