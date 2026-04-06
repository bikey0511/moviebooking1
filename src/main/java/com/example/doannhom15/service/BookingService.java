package com.example.doannhom15.service;

import com.example.doannhom15.model.*;
import com.example.doannhom15.repository.BookingConcessionRepository;
import com.example.doannhom15.repository.BookingRepository;
import com.example.doannhom15.repository.BookingSeatRepository;
import com.example.doannhom15.repository.SeatRepository;
import com.example.doannhom15.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingConcessionRepository bookingConcessionRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ConcessionService concessionService;
    private final VoucherService voucherService;
    private final PaymentService paymentService;
    
    public Page<Booking> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookingTime").descending());
        return bookingRepository.findAll(pageable);
    }
    
    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByBookingTimeDesc(user);
    }

    /** Hủy các vé PENDING đã hết hạn thanh toán (theo Payment.expiredAt). */
    @Transactional
    public void cancelExpiredPendingBookings(User user) {
        List<Booking> list = bookingRepository.findByUserOrderByBookingTimeDesc(user);
        LocalDateTime now = LocalDateTime.now();
        for (Booking b : list) {
            if (b.getStatus() != Booking.BookingStatus.PENDING) continue;
            Payment payment = paymentService.getPaymentByBookingId(b.getId());
            if (payment == null || payment.getExpiredAt() == null) continue;
            if (payment.getExpiredAt().isBefore(now)) {
                b.setStatus(Booking.BookingStatus.CANCELLED);
                bookingRepository.save(b);
                try {
                    paymentService.markAsExpired(b.getId());
                } catch (Exception ignored) { }
            }
        }
    }
    
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    /** Load booking đủ quan hệ để gửi email (chạy trong transaction mới, dùng trong @Async). */
    @Transactional(readOnly = true)
    public Booking getBookingByIdForEmail(Long id) {
        return bookingRepository.findByIdWithDetailsForEmail(id).orElse(null);
    }
    
    public Page<Booking> getBookingsByStatus(Booking.BookingStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookingTime").descending());
        return bookingRepository.findByStatus(status, pageable);
    }
    
    @Transactional
    public Booking createBooking(User user, Long showtimeId, List<Long> seatIds) {
    
        // 1. Kiểm tra user
        if (user == null) {
            throw new RuntimeException("User not found");
        }
    
        // 2. Lấy suất chiếu
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));
    
        // 3. Lấy danh sách ghế
        List<Seat> seats = seatRepository.findAllById(seatIds);
    
        if (seats.isEmpty()) {
            throw new RuntimeException("No seats selected");
        }
    
        // 4. Kiểm tra ghế tồn tại đủ không
        if (seats.size() != seatIds.size()) {
            throw new RuntimeException("Some seats not found");
        }
    
        // 5. Kiểm tra ghế đã bị đặt chưa
        List<Long> bookedSeatIds = bookingSeatRepository.findBookedSeatIdsByShowtimeId(showtimeId);
    
        for (Long seatId : seatIds) {
            if (bookedSeatIds.contains(seatId)) {
                throw new RuntimeException("Seat already booked: " + seatId);
            }
        }
    
        // 6. Tính tiền
        BigDecimal totalPrice = showtime.getPrice()
                .multiply(BigDecimal.valueOf(seats.size()));
    
        // 7. Tạo mã vé
        String ticketCode = "CINEMA-" + System.currentTimeMillis();
    
        // 8. Tạo booking
        Booking booking = Booking.builder()
                .user(user)
                .showtime(showtime)
                .totalPrice(totalPrice)
                .status(Booking.BookingStatus.PENDING)
                .bookingTime(LocalDateTime.now())
                .ticketCode(ticketCode)
                .paymentExpiry(LocalDateTime.now().plusMinutes(6))
                .build();
    
        Booking savedBooking = bookingRepository.save(booking);
    
        // 9. Lưu ghế
        List<BookingSeat> bookingSeats = new ArrayList<>();
    
        for (Seat seat : seats) {
            BookingSeat bookingSeat = BookingSeat.builder()
                    .booking(savedBooking)
                    .seat(seat)
                    .build();
    
            bookingSeats.add(bookingSeat);
        }
    
        bookingSeatRepository.saveAll(bookingSeats);
    
        return savedBooking;
    }
    
    @Transactional
    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(Booking.BookingStatus.PAID);
        return bookingRepository.save(booking);
    }
    
    // Admin xác nhận thanh toán thành công
    @Transactional
    public Booking confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }
    
    public List<Long> getBookedSeatIds(Long showtimeId) {
        return bookingSeatRepository.findBookedSeatIdsByShowtimeId(showtimeId);
    }
    
    public BigDecimal getTotalRevenue() {
        return bookingRepository.getTotalRevenue();
    }
    
    public BigDecimal getRevenueBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.getRevenueBetweenDates(startDate, endDate);
    }
    
    public long count() {
        return bookingRepository.count();
    }
    
    public long countByStatus(Booking.BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }
    
    public List<Object[]> getMonthlyRevenue(int year) {
        return bookingRepository.getMonthlyRevenue(year);
    }

    /**
     * Cập nhật đơn hàng: bắp nước và (tùy chọn) hai mã giảm giá từ form thanh toán.
     * @param vouchersFromClient false khi gửi từ trang combo — giữ nguyên mã đã lưu, chỉ tính lại giảm giá.
     */
    @Transactional
    public void updateOrder(Long bookingId, String concessionItems, String ticketVoucherCode, String concessionVoucherCode,
                            boolean vouchersFromClient) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Không thể sửa đơn hàng đã thanh toán");
        }

        BigDecimal concessionTotal;

        if (concessionItems != null && !concessionItems.isBlank()) {
            bookingConcessionRepository.deleteByBookingId(bookingId);
            concessionTotal = BigDecimal.ZERO;
            List<BookingConcession> list = new ArrayList<>();
            for (String part : concessionItems.split(",")) {
                String[] kv = part.trim().split(":");
                if (kv.length != 2) continue;
                try {
                    Long cid = Long.parseLong(kv[0].trim());
                    int qty = Integer.parseInt(kv[1].trim());
                    if (qty <= 0) continue;
                    ConcessionItem item = concessionService.getById(cid);
                    if (item == null || !item.isActive()) continue;
                    BigDecimal unitPrice = item.getPrice();
                    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
                    concessionTotal = concessionTotal.add(lineTotal);
                    BookingConcession bc = BookingConcession.builder()
                            .booking(booking)
                            .concessionItem(item)
                            .quantity(qty)
                            .unitPrice(unitPrice)
                            .build();
                    list.add(bc);
                } catch (NumberFormatException ignored) { }
            }
            booking.setConcessionTotal(concessionTotal);
            bookingConcessionRepository.saveAll(list);
        } else {
            concessionTotal = booking.getConcessionTotal() != null ? booking.getConcessionTotal() : BigDecimal.ZERO;
        }

        BigDecimal ticketTotal = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;

        if (vouchersFromClient) {
            String tCode = ticketVoucherCode != null ? ticketVoucherCode.trim() : "";
            String cCode = concessionVoucherCode != null ? concessionVoucherCode.trim() : "";
            if (!tCode.isEmpty() && tCode.equalsIgnoreCase(cCode)) {
                throw new RuntimeException("Không dùng cùng một mã cho cả hai ô.");
            }
            booking.setVoucherCode(null);
            booking.setConcessionVoucherCode(null);
            BigDecimal totalDiscount = BigDecimal.ZERO;
            if (!tCode.isEmpty()) {
                totalDiscount = totalDiscount.add(applyVoucherForBooking(booking, tCode, Voucher.DiscountType.TICKET, ticketTotal, concessionTotal));
                booking.setVoucherCode(tCode);
            }
            if (!cCode.isEmpty()) {
                totalDiscount = totalDiscount.add(applyVoucherForBooking(booking, cCode, Voucher.DiscountType.CONCESSION, ticketTotal, concessionTotal));
                booking.setConcessionVoucherCode(cCode);
            }
            booking.setDiscountAmount(totalDiscount);
        } else {
            booking.setDiscountAmount(recalculateDiscountFromStoredCodes(booking, ticketTotal, concessionTotal));
        }

        bookingRepository.save(booking);
        paymentService.updatePaymentAmount(bookingId);
    }

    private BigDecimal recalculateDiscountFromStoredCodes(Booking booking, BigDecimal ticketTotal, BigDecimal concessionTotal) {
        BigDecimal total = BigDecimal.ZERO;
        String t = booking.getVoucherCode();
        String c = booking.getConcessionVoucherCode();
        if (t != null && !t.isBlank()) {
            total = total.add(applyVoucherForBooking(booking, t.trim(), Voucher.DiscountType.TICKET, ticketTotal, concessionTotal));
        }
        if (c != null && !c.isBlank()) {
            total = total.add(applyVoucherForBooking(booking, c.trim(), Voucher.DiscountType.CONCESSION, ticketTotal, concessionTotal));
        }
        return total;
    }

    private BigDecimal applyVoucherForBooking(Booking booking, String code, Voucher.DiscountType expectedType,
                                             BigDecimal ticketTotal, BigDecimal concessionTotal) {
        Voucher voucher = voucherService.findByCode(code);
        if (voucher == null) {
            throw new RuntimeException("Mã «" + code + "» không tồn tại hoặc đã tắt.");
        }
        if (!voucherService.isValid(voucher)) {
            if (voucher.isValid() && voucher.getLinkedAnnouncementId() != null) {
                throw new RuntimeException("Mã «" + code + "» chỉ dùng khi thông báo khuyến mãi liên kết còn đang hiển thị.");
            }
            throw new RuntimeException("Mã «" + code + "» đã hết hạn hoặc không còn hiệu lực.");
        }
        if (!voucherService.canUserUseVoucher(booking.getUser().getId(), voucher)) {
            int max = voucherService.maxUsesPerUser(voucher);
            throw new RuntimeException("Bạn đã dùng mã «" + code + "» đủ số lần cho phép (tối đa " + max + " lần / tài khoản).");
        }
        if (voucher.getDiscountType() != expectedType) {
            if (expectedType == Voucher.DiscountType.TICKET) {
                throw new RuntimeException("Mã «" + code + "» chỉ dùng cho bắp nước — nhập vào ô «Mã giảm giá bắp nước».");
            }
            throw new RuntimeException("Mã «" + code + "» chỉ dùng cho vé xem phim — nhập vào ô «Mã giảm giá vé / phim».");
        }
        return voucherService.calculateDiscount(voucher, ticketTotal, concessionTotal);
    }

    /** Check-in tại rạp theo mã vé (PAID hoặc CONFIRMED, chưa check-in). */
    @Transactional
    public Booking checkInByTicketCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập hoặc quét mã vé.");
        }
        String code = rawCode.trim();
        Booking booking = bookingRepository.findByTicketCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé với mã này."));
        if (booking.getCheckedInAt() != null) {
            String when = booking.getCheckedInAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            throw new IllegalArgumentException(
                    "Vé đã được sử dụng lúc " + when + ", không thể quét lại.");
        }
        Booking.BookingStatus st = booking.getStatus();
        if (st != Booking.BookingStatus.PAID && st != Booking.BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Vé chưa hợp lệ để vào rạp (cần đã thanh toán / đã xác nhận).");
        }
        booking.setCheckedInAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    // Today's checked-in count
    public long countTodayCheckedIn(LocalDateTime startOfDay) {
        return bookingRepository.countTodayCheckedIn(startOfDay);
    }

    public BigDecimal getTodayRevenue(LocalDateTime startOfDay) {
        return bookingRepository.getTodayRevenue(startOfDay);
    }

    public List<Booking> getRecentBookings(int limit) {
        return bookingRepository.findRecentBookings(PageRequest.of(0, limit));
    }

    public long countPendingBookings() {
        return bookingRepository.countPendingBookings();
    }

    public List<Booking> getTodayBookings(LocalDateTime startOfDay) {
        return bookingRepository.findTodayBookings(startOfDay);
    }

    public long countTodayBookings(LocalDateTime startOfDay) {
        return bookingRepository.countTodayBookings(startOfDay);
    }

    /** Hoàn tác check-in (khi khách phản ánh nhầm / lỗi quét). Chỉ dùng tại quầy. */
    @Transactional
    public void clearCheckInByStaff(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đặt vé."));
        if (booking.getCheckedInAt() == null) {
            throw new IllegalArgumentException("Vé chưa được check-in, không cần hoàn tác.");
        }
        booking.setCheckedInAt(null);
        bookingRepository.save(booking);
    }
}
