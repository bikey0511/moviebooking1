-- Chạy file này nếu gặp lỗi "Data truncated for column 'status'"
-- Chạy trong MySQL: source path/to/fix-booking-status.sql
-- Hoặc copy nội dung vào phpMyAdmin/MySQL Workbench

ALTER TABLE bookings MODIFY COLUMN status VARCHAR(20) NOT NULL;
