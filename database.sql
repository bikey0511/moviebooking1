-- ================================================
-- CINEMA MOVIE BOOKING SYSTEM DATABASE SCHEMA
-- Generated from Spring Boot JPA Entities
-- ================================================

-- Drop existing database and create new one
CREATE DATABASE movieticket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE movieticket;

-- ================================================
-- TABLE: users
-- Description: Quản lý người dùng (Admin, Staff, User)
-- ================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20),
    google_id VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: movies
-- Description: Danh sách phim
-- ================================================
CREATE TABLE movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description LONGTEXT,
    duration INT,
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    release_date DATE,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME,
    INDEX idx_status (status),
    INDEX idx_release_date (release_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: cinema_rooms
-- Description: Các phòng chiếu (Normal hoặc VIP)
-- ================================================
CREATE TABLE cinema_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    room_type VARCHAR(20),
    total_seats INT,
    created_at DATETIME,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: seats
-- Description: Các ghế trong phòng chiếu
-- ================================================
CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_number VARCHAR(50) NOT NULL,
    room_id BIGINT NOT NULL,
    seat_type VARCHAR(50),
    row_name VARCHAR(10),
    created_at DATETIME,
    FOREIGN KEY (room_id) REFERENCES cinema_rooms(id) ON DELETE CASCADE,
    INDEX idx_room_id (room_id),
    UNIQUE KEY unique_seat_room (seat_number, room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: showtimes
-- Description: Lịch chiếu phim
-- ================================================
CREATE TABLE showtimes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at DATETIME,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES cinema_rooms(id) ON DELETE CASCADE,
    INDEX idx_movie_id (movie_id),
    INDEX idx_room_id (room_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: bookings
-- Description: Đơn đặt vé xem phim
-- ================================================
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    booking_time DATETIME NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    concession_total DECIMAL(10, 2) DEFAULT 0,
    voucher_code VARCHAR(50),
    concession_voucher_code VARCHAR(50),
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    created_at DATETIME,
    payment_expiry DATETIME,
    ticket_code VARCHAR(100),
    checked_in_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (showtime_id) REFERENCES showtimes(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_showtime_id (showtime_id),
    INDEX idx_status (status),
    INDEX idx_ticket_code (ticket_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: booking_seats
-- Description: Ghế được đặt trong mỗi lần đặt vé
-- ================================================
CREATE TABLE booking_seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    created_at DATETIME,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE,
    INDEX idx_booking_id (booking_id),
    INDEX idx_seat_id (seat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: payments
-- Description: Thanh toán cho đặt vé
-- ================================================
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    payment_code VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME,
    expired_at DATETIME,
    paid_at DATETIME,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_expired_at (expired_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: vouchers
-- Description: Mã giảm giá (vé hoặc bắp nước)
-- ================================================
CREATE TABLE vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    owner_user_id BIGINT,
    discount_type VARCHAR(20) NOT NULL,
    value_type VARCHAR(10) NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    valid_from DATETIME,
    valid_to DATETIME,
    max_uses INT,
    used_count INT DEFAULT 0,
    active BOOLEAN DEFAULT true,
    discount_announcement_id BIGINT,
    created_at DATETIME,
    FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_code (code),
    INDEX idx_active (active),
    INDEX idx_valid_to (valid_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: voucher_usages
-- Description: Lịch sử sử dụng voucher của user
-- ================================================
CREATE TABLE voucher_usages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    voucher_id BIGINT NOT NULL,
    usage_count INT DEFAULT 1,
    used_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_voucher (user_id, voucher_id),
    INDEX idx_user_id (user_id),
    INDEX idx_voucher_id (voucher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: concession_items
-- Description: Đồ ăn nhẹ (bắp, nước, combo)
-- ================================================
CREATE TABLE concession_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    active BOOLEAN DEFAULT true,
    INDEX idx_type (type),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: booking_concessions
-- Description: Đồ ăn được đặt trong mỗi lần đặt vé
-- ================================================
CREATE TABLE booking_concessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    concession_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (concession_id) REFERENCES concession_items(id) ON DELETE CASCADE,
    INDEX idx_booking_id (booking_id),
    INDEX idx_concession_id (concession_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: movie_reviews
-- Description: Đánh giá và bình luận về phim
-- ================================================
CREATE TABLE movie_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment LONGTEXT,
    created_at DATETIME,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_movie_id (movie_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: discount_announcements
-- Description: Thông báo khuyến mãi
-- ================================================
CREATE TABLE discount_announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    image_url VARCHAR(500),
    published_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    INDEX idx_published_at (published_at),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: quiz_questions
-- Description: Câu hỏi quiz
-- ================================================
CREATE TABLE quiz_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question VARCHAR(500) NOT NULL,
    option_a VARCHAR(200) NOT NULL,
    option_b VARCHAR(200) NOT NULL,
    option_c VARCHAR(200) NOT NULL,
    option_d VARCHAR(200) NOT NULL,
    correct_answer VARCHAR(1) NOT NULL,
    category VARCHAR(200),
    active BOOLEAN DEFAULT true,
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- TABLE: quiz_participations
-- Description: Lượt chơi quiz của user
-- ================================================
CREATE TABLE quiz_participations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    voucher_code VARCHAR(50),
    participated_at DATETIME NOT NULL,
    won BOOLEAN DEFAULT false,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_participated_at (participated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- Sample Data Insertion (Optional)
-- ================================================

-- Insert sample users
INSERT INTO users (username, password, email, phone, role, enabled, created_at) VALUES
('admin', '$2a$10$EblZQbUObNtTnLHXoxyR2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe', 'admin@cinema.com', '0901234567', 'ADMIN', true, NOW()),
('staff1', '$2a$10$EblZQbUObNtTnLHXoxyR2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe', 'staff@cinema.com', '0912345678', 'STAFF', true, NOW()),
('user1', '$2a$10$EblZQbUObNtTnLHXoxyR2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe', 'user1@example.com', '0987654321', 'USER', true, NOW()),
('user2', '$2a$10$EblZQbUObNtTnLHXoxyR2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMUe', 'user2@example.com', '0987654322', 'USER', true, NOW());

-- Insert sample movies
INSERT INTO movies (title, description, duration, poster_url, status, release_date, created_at) VALUES
('Avengers: Endgame', 'Phim siêu anh hùng hay nhất', 181, '/images/avengers.jpg', 'NOW_SHOWING', '2024-01-01', NOW()),
('Avatar 2', 'Phim viễn tưởng tuyệt vời', 192, '/images/avatar.jpg', 'NOW_SHOWING', '2024-01-15', NOW()),
('Oppenheimer', 'Drama lịch sử', 180, '/images/oppenheimer.jpg', 'COMING_SOON', '2024-03-01', NOW());

-- Insert sample cinema rooms
INSERT INTO cinema_rooms (name, room_type, total_seats, created_at) VALUES
('Phòng 1 (Normal)', 'NORMAL', 120, NOW()),
('Phòng 2 (Normal)', 'NORMAL', 120, NOW()),
('Phòng VIP', 'VIP', 40, NOW());

-- Insert sample vouchers
INSERT INTO vouchers (code, discount_type, value_type, value, valid_from, valid_to, active, created_at) VALUES
('WELCOME2024', 'TICKET', 'PERCENT', 10, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), true, NOW()),
('COMBO50', 'CONCESSION', 'FIXED', 50000, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), true, NOW()),
('QUIZ2024', 'TICKET', 'FIXED', 100000, NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY), true, NOW());

-- Insert sample concession items
INSERT INTO concession_items (name, description, price, type, active) VALUES
('Bắp caramel', 'Bắp mặn ngon', 50000, 'BAP', true),
('Nước ngọt', 'Nước Coca 355ml', 25000, 'NUOC', true),
('Nước cam', 'Nước cam tươi', 30000, 'NUOC', true),
('Combo Bắp + Nước', 'Bắp 1 hộp + Nước 1 cốc', 70000, 'COMBO', true);

-- Insert sample quiz questions
INSERT INTO quiz_questions (question, option_a, option_b, option_c, option_d, correct_answer, category, active) VALUES
('Bộ phim nào là phim Hollywood hay nhất?', 'Avatar', 'Inception', 'Titanic', 'Jurassic Park', 'B', 'Cinema', true),
('Năm nào phim Avengers: Endgame ra mắt?', '2018', '2019', '2020', '2021', 'B', 'Cinema', true),
('Đạo diễn nào nổi tiếng với các bộ phim khoa học viễn tưởng?', 'Steven Spielberg', 'James Cameron', 'Quentin Tarantino', 'Christopher Nolan', 'D', 'Cinema', true);

-- ================================================
-- Create Indexes for Performance
-- ================================================
ALTER TABLE bookings ADD INDEX idx_created_at (created_at);
ALTER TABLE bookings ADD INDEX idx_booking_time (booking_time);
ALTER TABLE bookings ADD INDEX idx_payment_expiry (payment_expiry);
ALTER TABLE showtimes ADD INDEX idx_created_at (created_at);
ALTER TABLE users ADD INDEX idx_role (role);

-- ================================================
-- Database setup complete
-- ================================================
COMMIT;
