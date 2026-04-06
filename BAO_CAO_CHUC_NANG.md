# BÁO CÁO CHỨC NĂNG ĐÃ PHÁT TRIỂN
## Hệ thống đặt vé xem phim - InApp Cinema

---

## 1. PHÂN QUYỀN & ĐĂNG NHẬP

### 1.1. Ba vai trò người dùng
- **USER** (Người dùng thường): Đặt vé, xem phim, nhận voucher, chơi quiz.
- **STAFF** (Nhân viên): Quét mã QR check-in, quản lý đơn hàng, xác nhận/hủy vé, quản lý phim/phòng/suất chiếu/bắp nước/voucher/khuyến mãi.
- **ADMIN** (Quản trị viên): Thống kê doanh thu, quản lý nhân viên, xác nhận vé có QR gửi email.

### 1.2. Đăng nhập đa nền tảng
- Đăng nhập bằng **tài khoản local** (username/password).
- Đăng nhập bằng **Google OAuth2** (SSO).
- Sau khi đăng nhập, hệ thống tự chuyển hướng:
  - ADMIN → `/admin/analytics`
  - STAFF → `/staff/dashboard`
  - USER → `/home`

### 1.3. Tự động chuyển hướng đăng nhập
- Người dùng bị khóa (disabled) sẽ được thông báo, không cho đăng nhập.
- Phiên đăng nhập hết hạn sẽ được phát hiện và yêu cầu đăng nhập lại.
- Nếu đăng nhập sai mật khẩu sẽ hiển thị thông báo lỗi rõ ràng.

### 1.4. Đăng ký tài khoản mới
- Người dùng đăng ký tài khoản mới → Tự động nhận **Voucher chào mừng** (giảm 20% giá vé, 1 lần sử dụng).
- Mỗi tài khoản chỉ nhận voucher chào mừng 1 lần duy nhất.

---

## 2. QUẢN LÝ PHIM & SUẤT CHIẾU

### 2.1. Trang chủ (Home)
- Hiển thị banner phim đang chiếu.
- Phim đang chiếu & sắp chiếu với poster, thể loại, thời lượng, đánh giá.
- Slide/phân trang theo ngày chiếu.
- Thông báo khuyến mãi nổi bật.

### 2.2. Trang danh sách phim
- Lọc phim: **Đang chiếu** / **Sắp chiếu**.
- Tìm kiếm phim theo tên.
- Sắp xếp theo ngày ra mắt, lượt đánh giá.
- Hiển thị poster, thể loại, thời lượng, đạo diễn, diễn viên, trailer.

### 2.3. Chi tiết phim
- Poster, backdrop, trailer nhúng YouTube.
- Lịch chiếu 7 ngày tới (tự động đồng bộ với dữ liệu suất chiếu).
- Ghế ngồi theo loại: **Ghế thường (STANDARD)**, **Ghế đôi (DOUBLE)** → ghế đôi ghép 2 ghế liền nhau tự động.
- Bình luận & đánh giá sao (1-5 sao) kèm nội dung.

### 2.4. Quản lý phim (Admin/Staff)
- Thêm mới phim: tiêu đề, mô tả, poster, backdrop, trailer, thể loại, thời lượng, đạo diễn, diễn viên, ngày ra mắt, trạng thái (Đang chiếu/Sắp chiếu).
- Chỉnh sửa phim.
- Xóa phim (chỉ phim chưa có suất chiếu đã bán vé).
- Upload poster/backdrop từ máy tính.

---

## 3. HỆ THỐNG ĐẶT VÉ

### 3.1. Chọn ghế
- Giao diện sơ đồ ghế theo phòng chiếu (A, B, C... hàng ngang).
- Ghế **STANDARD** hiển thị riêng, ghế **DOUBLE** hiển thị thành 1 ô ghép.
- Màu sắc phân biệt: ghế trống (xanh), ghế đã đặt (đỏ), ghế đang chọn (vàng).
- Click ghế → đặt/ghỡ đặt. Cần chọn đủ số ghế muốn đặt.
- Giá vé hiển thị theo loại ghế (phim VIP: ghế thường 65k, ghế đôi 130k).

### 3.2. Thêm bắp nước
- Trang bắp nước hiển thị combo, đồ ăn, nước uống kèm giá.
- Tăng/giảm số lượng từng món.
- Tính tổng tiền bắp nước tự động.

### 3.3. Thanh toán
- Tính tổng tiền: Tiền vé + Tiền bắp nước - Giảm giá.
- **Đếm ngược thời gian thanh toán** (6 phút). Hết giờ → tự động hủy đơn.
- Nhập **mã voucher** giảm giá:
  - **Voucher vé/phim** (loại TICKET): giảm % hoặc số tiền cho tiền vé.
  - **Voucher bắp nước** (loại CONCESSION): giảm % hoặc số tiền cho tiền bắp nước.
  - Kiểm tra số lần sử dụng voucher tối đa / mỗi tài khoản.
- Mã QR thanh toán ngân hàng (Vietcombank) hiển thị kèm số tài khoản, tên chủ TK, nội dung chuyển khoản (mã đơn).
- User bấm **"Tôi đã chuyển khoản"** để xác nhận đã thanh toán.

### 3.4. Trạng thái đơn hàng
- **PENDING**: Chờ thanh toán (có đếm ngược).
- **PAID**: Đã thanh toán, chờ rạp xác nhận.
- **CONFIRMED**: Đã xác nhận (admin/staff xác nhận + gửi email vé + QR).
- **CANCELLED**: Đã hủy (hết hạn hoặc bị hủy).

### 3.5. Email thông báo
- **Email xác nhận thanh toán** gửi cho khách ngay sau khi bấm "Đã chuyển khoản" (chưa có QR).
- **Email vé xác nhận** gửi khi admin/staff xác nhận đơn, kèm:
  - Mã vé (VD: CINEMA-1775457245039).
  - Mã QR để quét vào rạp.
  - Thông tin phim, suất chiếu, ghế, tổng tiền.
- **Email hết hạn** gửi khi đơn PENDING bị hủy do hết giờ thanh toán.
- Email luôn gửi đúng địa chỉ email của **chủ đơn hàng** (lấy từ CSDL, không dùng email người đang đăng nhập).

---

## 4. QUẢN LÝ PHÒNG CHIẾU & GHẾ

### 4.1. Cấu hình phòng chiếu
- Thêm phòng: tên, loại phòng (Thường / VIP).
- Phòng VIP có giá vé cao hơn.
- Sửa/xóa phòng (không xóa được nếu có suất chiếu đang bán vé).

### 4.2. Tạo ghế tự động
- Khi tạo phòng mới → hệ thống **tự tạo ghế** theo cấu hình:
  - **Phòng thường** (5 hàng ghế thường × 20 ghế = 100 ghế + 5 ghế đôi ở cuối).
  - **Phòng VIP** (3 hàng × 12 ghế thường + 4 ghế đôi VIP).
- Ghế đôi tự động ghép ghế liền nhau (Double Seat).
- Ghế đôi chỉ được bán theo cặp (1 lần mua 2 ghế liền).

---

## 5. QUẢN LÝ SUẤT CHIẾU

### 5.1. Thêm suất chiếu
- Chọn phim, chọn phòng, chọn ngày (trong vòng 7 ngày tới), chọn giờ bắt đầu, nhập giá vé.
- Tự kiểm tra **xung đột giờ** (không cho tạo 2 suất chiếu chồng lên nhau cùng phòng).

### 5.2. Danh sách suất chiếu
- Xem lịch suất chiếu theo ngày.
- Lọc theo phim hoặc phòng chiếu.
- Sửa giờ/giá, xóa suất (không xóa được nếu có ghế đã bán).

---

## 6. QUẢN LÝ ĐƠN HÀNG (STAFF/ADMIN)

### 6.1. Danh sách đơn hàng
- Xem tất cả đơn hàng, lọc theo trạng thái (PENDING / PAID / CONFIRMED / CANCELLED).
- Phân trang.
- Hiển thị tổng tiền, thời gian đặt, trạng thái.

### 6.2. Chi tiết đơn hàng
- Xem đầy đủ thông tin: phim, suất chiếu, ghế, bắp nước, voucher đã dùng, tổng tiền.
- **Xác nhận đơn** (chuyển CONFIRMED) → hệ thống gửi email vé + QR cho khách.
- **Hủy đơn** (CANCELLED).
- **Hoàn tác check-in** (khi khách phản ánh lỗi quét).

### 6.3. Thêm đơn hàng mới (Staff)
- Nhân viên có thể tạo đơn hàng tại quầy (chọn phim, suất chiếu, ghế, bắp nước, xác nhận thanh toán trực tiếp → CONFIRMED luôn).

---

## 7. QUẢN LÝ VOUCHER & KHUYẾN MÃI

### 7.1. Voucher giảm giá
- Tạo voucher với:
  - **Mã voucher** tùy chỉnh.
  - **Loại**: Giảm theo % hoặc theo số tiền cố định.
  - **Loại áp dụng**: TICKET (giảm tiền vé) / CONCESSION (giảm tiền bắp nước).
  - Giá trị giảm tối đa (nếu dùng %).
  - Số lần sử dụng tối đa / mỗi tài khoản.
  - Ngày bắt đầu, ngày hết hạn.
  - Trạng thái bật/tắt.

### 7.2. Thông báo khuyến mãi (Discount Announcement)
- Tạo thông báo hiển thị trên web (VD: "Rạp chiếu phim Nhật Bản - Giảm 20% vé...").
- Tự động liên kết với voucher (khuyến mãi chỉ áp dụng khi thông báo còn hiệu lực).
- Hiển thị dạng thông báo popup/than trên trang chủ.

### 7.3. Voucher của người dùng
- Mỗi người dùng có trang xem **Voucher của tôi**.
- Hiển thị danh sách voucher đang có: mã, giá trị, ngày hết hạn, đã dùng hay chưa.
- Voucher chào mừng tạo tự động khi đăng ký.

---

## 8. QUẢN LÝ BẮP NƯỚC

### 8.1. Thêm món bắp nước
- Tên, mô tả, giá, hình ảnh, danh mục (Combo / Đồ ăn / Nước uống).
- Trạng thái bật/tắt.

### 8.2. Danh sách bắp nước
- Hiển thị danh sách món kèm giá, phân loại theo danh mục.
- Sửa/xóa món.
- Người dùng chọn bắp nước khi đặt vé.

---

## 9. HỆ THỐNG QUÉT MÃ QR CHECK-IN

### 9.1. Trang quét mã vé (Staff)
- Nhân viên bật **camera trên điện thoại/tablet** để quét mã QR trên vé khách.
- Nhập mã vé bằng tay nếu QR không đọc được.
- Có thể dùng trên **máy tính** (nhập tay) hoặc **điện thoại** (quét camera).

### 9.2. Xử lý kết quả quét
- **Thành công**: Hiện vòng tròn xanh lớn 2 giây + animation checkmark → tự ẩn để quét khách tiếp theo.
- **Thất bại** — vé đã quét rồi: Hiển thị lỗi đỏ kèm **thời gian đã quét lần đầu** (VD: "Vé đã được sử dụng lúc 06/04/2026 14:30:45").
- **Thất bại** — vé chưa thanh toán: Thông báo vé chưa hợp lệ.
- **Thất bại** — không tìm thấy: Thông báo mã vé không tồn tại.

### 9.3. Hoàn tác check-in
- Nhân viên có thể hoàn tác check-in từ trang chi tiết đơn hàng (trong trường hợp khách chưa vào rạp mà gặp lỗi quét).

### 9.4. Số liệu check-in
- Dashboard nhân viên hiển thị: Số vé đã check-in hôm nay.

---

## 10. QUIZ ĐỐ CÂU HỎI

### 10.1. Trang chơi quiz
- Người dùng đăng nhập → bấm "Đố câu hỏi" để chơi.
- Hệ thống chọn **5 câu hỏi ngẫu nhiên** từ ngân hàng câu hỏi.
- Mỗi câu hỏi có 4 đáp án, chọn đúng → cộng điểm.
- Hiển thị điểm số, thời gian, câu đúng/sai.

### 10.2. Nhận voucher khi chơi
- **Đạt 4/5 → 5/5 điểm**: Nhận voucher ngẫu nhiên từ hệ thống (giảm 10% - 15% giá vé).
- **Đạt 3/5 điểm**: Nhận voucher giảm 5%.
- Mỗi tài khoản chỉ chơi quiz **1 lần/ngày**.

### 10.3. Quản lý câu hỏi (Admin/Staff)
- Thêm/sửa/xóa câu hỏi.
- Câu hỏi có: nội dung, 4 đáp án, đáp án đúng, danh mục (Phim / Rạp chiếu / Bắp nước / Khác).
- Ngân hàng câu hỏi khởi tạo sẵn **41 câu** khi lần đầu chạy hệ thống.

---

## 11. TRANG CÁ NHÂN & TÀI KHOẢN

### 11.1. Thông tin cá nhân
- Người dùng xem và chỉnh sửa: **Email**, **Số điện thoại**.
- Tên đăng nhập không thể thay đổi.

### 11.2. Trang thông tin cá nhân riêng cho Staff
- Nhân viên đăng nhập tại khu vực nhân viên → xem thông tin tài khoản nhân viên (không phải admin).
- Cập nhật email, số điện thoại của tài khoản nhân viên.

### 11.3. Trang thông tin cá nhân riêng cho Admin
- Quản trị viên quản lý nhân viên: bật/tắt tài khoản nhân viên.

---

## 12. TRANG THỐNG KÊ (ADMIN)

### 12.1. Dashboard thống kê
- Tổng số đơn hàng hôm nay.
- Doanh thu hôm nay.
- Số vé đã check-in hôm nay.
- Số đơn chờ xác nhận (PENDING).
- Tổng số phim, tổng số đơn hàng, tổng số người dùng, tổng doanh thu.

### 12.2. Biểu đồ doanh thu
- Biểu đồ cột/doanh thu theo **tháng** trong năm (chọn năm).
- Biểu đồ tổng doanh thu theo **trạng thái** đơn hàng (PAID / CONFIRMED / CANCELLED).

---

## 13. CÁC TÍNH NĂNG BỔ SUNG

### 13.1. Bảng màu giao diện
- Mã màu chủ đạo: Đỏ Cinema (#e50914) + Vàng Citrine (#ffc107) + Đen Eerie Black (#1a1a2e).
- Giao diện tối (dark theme) xuyên suốt trang người dùng.
- Sidebar giao diện sáng cho khu vực Admin/Staff.

### 13.2. Thông báo popup voucher
- Khi đăng ký / nhận voucher → hiện **toast popup** thông báo mã voucher mới.
- Icon chuông thông báo trên header hiển thị số thông báo khuyến mãi.

### 13.3. Múi giờ & ngôn ngữ
- Toàn bộ hệ thống sử dụng **tiếng Việt**.
- Ngày giờ hiển thị theo định dạng Việt Nam (dd/MM/yyyy HH:mm).
- Múi giờ server: UTC, hiển thị +07:00 (Việt Nam).

### 13.4. Responsive
- Giao diện người dùng **responsive** trên mobile/tablet/desktop.
- Khu vực Admin/Staff tối ưu trên máy tính và tablet.

### 13.5. Bảo mật
- Password mã hóa BCrypt.
- Role-based access control (ROLE_USER / ROLE_STAFF / ROLE_ADMIN).
- CSRF disabled (đổi lấy các biện pháp bảo mật khác).

---

## 14. CÔNG NGHỆ SỬ DỤNG

| Layer | Công nghệ |
|-------|-----------|
| Backend | Java 17+, Spring Boot 3.x, Spring Security |
| ORM | Hibernate (JPA) |
| Database | MySQL 8.x |
| Frontend | Thymeleaf, HTML5, CSS3, JavaScript |
| UI Components | Bootstrap 5, Tabler Icons (CDN), Ion Icons |
| QR Code | html5-qrcode library (camera scanner) |
| Email | Spring Mail + Gmail SMTP |
| OAuth2 | Google OAuth2 (SSO) |
| Build Tool | Maven |

---

## 15. TÀI KHOẢN MẶC ĐỊNH

| Username | Password | Vai trò |
|----------|---------|---------|
| admin | admin123 | ADMIN |
| staff | staff123 | STAFF |
| user | user123 | USER |

---

## 16. CẤU TRÚC MÃ NGUỒN CHÍNH

```
src/main/java/com/example/doannhom15/
├── config/
│   ├── SecurityConfig.java          # Cấu hình bảo mật, phân quyền
│   ├── CustomAuthenticationSuccessHandler.java  # Chuyển hướng theo vai trò
│   ├── CustomOAuth2UserService.java  # Xử lý đăng nhập Google
│   ├── DataInitializer.java          # Khởi tạo dữ liệu mẫu
│   └── UserRoleColumnMigrator.java    # Sửa cột role trong DB
├── controller/
│   ├── AuthController.java           # Đăng nhập / đăng ký
│   ├── BookingController.java        # Quy trình đặt vé người dùng
│   ├── AdminBookingController.java   # Quản lý đơn hàng (Staff/Admin)
│   ├── AdminMovieController.java      # Quản lý phim
│   ├── AdminRoomController.java       # Quản lý phòng chiếu
│   ├── AdminShowtimeController.java   # Quản lý suất chiếu
│   ├── AdminConcessionController.java # Quản lý bắp nước
│   ├── AdminVoucherController.java    # Quản lý voucher
│   ├── AdminDiscountAnnouncementController.java # Thông báo khuyến mãi
│   ├── StaffCheckInController.java     # Quét mã QR check-in
│   ├── StaffDashboardController.java  # Dashboard nhân viên
│   ├── StaffProfileController.java    # Thông tin cá nhân nhân viên
│   ├── QuizController.java            # Quiz đố câu hỏi
│   └── UserVoucherApiController.java   # API voucher cho frontend
├── model/
│   ├── User.java                      # Tài khoản (USER/STAFF/ADMIN)
│   ├── Movie.java                     # Phim
│   ├── CinemaRoom.java                # Phòng chiếu
│   ├── Seat.java                      # Ghế ngồi
│   ├── Showtime.java                  # Suất chiếu
│   ├── Booking.java                   # Đơn đặt vé
│   ├── BookingSeat.java               # Ghế trong đơn
│   ├── BookingConcession.java         # Bắp nước trong đơn
│   ├── ConcessionItem.java            # Món bắp nước
│   ├── Payment.java                   # Thông tin thanh toán
│   ├── Voucher.java                   # Voucher giảm giá
│   ├── VoucherUsage.java              # Lịch sử dùng voucher
│   ├── DiscountAnnouncement.java       # Thông báo khuyến mãi
│   ├── MovieReview.java               # Đánh giá phim
│   ├── QuizQuestion.java              # Câu hỏi quiz
│   └── QuizParticipation.java         # Lượt chơi quiz
├── repository/
│   ├── UserRepository.java
│   ├── MovieRepository.java
│   ├── BookingRepository.java
│   ├── SeatRepository.java
│   ├── ShowtimeRepository.java
│   ├── VoucherRepository.java
│   └── ...
├── service/
│   ├── BookingService.java           # Logic đặt vé, check-in, xác nhận
│   ├── PaymentService.java            # Logic thanh toán
│   ├── EmailService.java              # Gửi email xác nhận/vé/hết hạn
│   ├── VoucherService.java            # Logic voucher, kiểm tra hạn sử dụng
│   ├── QuizService.java               # Logic quiz, tạo voucher khi thắng
│   ├── MovieReviewService.java        # Logic đánh giá phim
│   └── ...
└── api/
    ├── SeatApiController.java        # API chọn ghế (REST)
    └── UserVoucherApiController.java  # API voucher (REST)
```

---

## 17. DATABASE SCHEMA CHÍNH

### Bảng `users`
- id, username, password, email, phone, google_id, **role** (VARCHAR - hỗ trợ USER/STAFF/ADMIN), enabled, created_at.

### Bảng `movies`
- id, title, description, poster_url, backdrop_url, trailer_url, genre, duration, director, cast, release_date, status (NOW_SHOWING / COMING_SOON).

### Bảng `cinema_rooms`
- id, name, room_type (NORMAL / VIP).

### Bảng `seats`
- id, room_id, row_name, seat_number, seat_type (STANDARD / DOUBLE).

### Bảng `showtimes`
- id, movie_id, room_id, start_time, price.

### Bảng `bookings`
- id, user_id, showtime_id, booking_time, total_price, status (PENDING/PAID/CONFIRMED/CANCELLED), ticket_code, checked_in_at, payment_expiry, voucher_code, concession_voucher_code, discount_amount, concession_total.

### Bảng `payments`
- id, booking_id, payment_code, amount, status, expired_at.

### Bảng `vouchers`
- id, code, discount_type (TICKET/CONCESSION), discount_value, max_uses, max_uses_per_user, start_date, expiry_date, active.

### Bảng `quiz_questions`
- id, question, option_a, option_b, option_c, option_d, correct_answer, category.

---

## 18. LUỒNG NGHIỆP VỤ TỔNG HỢP

### Luồng đặt vé đầy đủ
```
1. User đăng nhập
2. Chọn phim → Chọn suất chiếu → Chọn ghế → Xác nhận đặt
3. Thêm bắp nước (tùy chọn) → Tiếp tục
4. Nhập mã voucher (tùy chọn) → Xem tổng tiền
5. Chuyển khoản ngân hàng theo mã QR
6. Bấm "Tôi đã chuyển khoản" → Đơn chuyển PAID
7. Staff xác nhận đơn → Đơn chuyển CONFIRMED + Gửi email vé + QR
8. User đến rạp → Staff quét mã QR → Check-in thành công
```

### Luồng quiz
```
1. User đăng nhập → Bấm "Đố câu hỏi"
2. Hệ thống chọn 5 câu ngẫu nhiên
3. User trả lời 5 câu
4. Xem điểm:
   - 4-5 đúng → Nhận voucher giảm 10-15%
   - 3 đúng → Nhận voucher giảm 5%
   - <3 đúng → Không nhận được voucher
```

---

*Báo cáo được tổng hợp ngày 06/04/2026*
