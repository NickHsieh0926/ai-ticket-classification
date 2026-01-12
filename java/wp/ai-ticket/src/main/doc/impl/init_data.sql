-- 預插一筆測試資料 (帳號: admin, 密碼: 123456)
-- 注意：這裡的密碼是經過 BCrypt 加密後的字串
INSERT INTO users (username, password, role) 
VALUES ('admin', '$2a$10$viOlnU9WQg0QLfGcL2COY.hUQ8fQr5KU1VtIfNrm.lbOURZ7KkXkS', 'ADMIN');