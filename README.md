# Đồ án nhóm 15 — Hệ thống đặt vé xem phim

Ứng dụng web đặt vé rạp: xem phim, chọn suất và ghế, đặt đồ ăn, thanh toán (demo), quản trị phim/suất/phòng/voucher người dùng.

## Công nghệ

- Java **17**, Spring Boot **4.x** (Maven)
- MySQL, Spring Data JPA, Thymeleaf, Spring Security (form login + **Google OAuth2** tùy chọn)
- Gửi email (Gmail SMTP) tùy cấu hình

## Yêu cầu trước khi chạy

- **JDK 17**
- **Maven 3.8+** (hoặc dùng Maven wrapper nếu có)
- **MySQL** chạy local (mặc định port `3306`)

## Cấu hình

Sửa `src/main/resources/application.properties` (hoặc dùng biến môi trường tương ứng) cho đúng máy bạn:

| Mục | Mặc định trong repo | Ghi chú |
|-----|---------------------|---------|
| Database | `jdbc:mysql://localhost:3306/movieticket` | Schema `movieticket` sẽ được tạo nếu chưa có (`createDatabaseIfNotExist=true`) |
| User / password MySQL | `root` / *(trống)* | Bắt buộc điền `spring.datasource.password` nếu MySQL của bạn có mật khẩu |
| Cổng server | `8080` | Đổi bằng `server.port` nếu trùng cổng |
| Google OAuth2 | `client-id`, `client-secret`, … đang trống | Chỉ cần điền nếu muốn đăng nhập Google |
| Email | `spring.mail.username`, `spring.mail.password` | Để gửi mail xác nhận/vé; với Gmail thường dùng **App Password** |

`spring.jpa.hibernate.ddl-auto=update`: Hibernate tự cập nhật schema; **không** xóa dữ liệu khi restart.

## Cách chạy

1. Bật MySQL, tạo user có quyền truy cập (hoặc dùng `root` khớp với file cấu hình).
2. Trong thư mục gốc project:

```bash
mvn spring-boot:run
```

Hoặc build rồi chạy JAR:

```bash
mvn -q -DskipTests package
java -jar target/doannhom15-0.0.1-SNAPSHOT.jar
```

3. Mở trình duyệt: **http://localhost:8080**

### Tài khoản mẫu (lần chạy đầu, khi database chưa có phòng/phim)

Khi `DataInitializer` chạy trên DB trống, có thể có sẵn:

- **Admin:** `admin` / `admin123`
- **User:** `user` / `user123`

*(Nếu đã có dữ liệu phòng hoặc phim trong DB, initializer sẽ bỏ qua để không ghi đè dữ liệu — khi đó có thể cần tạo admin trong DB hoặc xóa dữ liệu test rồi chạy lại, tùy nhu cầu.)*

## Đường dẫn gợi ý sau khi đăng nhập

- Trang chủ: `/` hoặc `/home`
- Khu vực người dùng: `/user/...` (đặt vé, đơn, hồ sơ)
- Khu vực quản trị: `/admin/dashboard` (role ADMIN)

## API JSON (tham khảo)

- `GET /api/showtime/{id}/seats`
- `POST /api/bookings`
- `GET /api/promo-announcements/active`
