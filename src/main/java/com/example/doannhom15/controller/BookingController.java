package com.example.doannhom15.controller;

import com.example.doannhom15.model.*;
import com.example.doannhom15.service.BookingService;
import com.example.doannhom15.service.CinemaRoomService;
import com.example.doannhom15.service.ConcessionService;
import com.example.doannhom15.service.EmailService;
import com.example.doannhom15.service.MovieService;
import com.example.doannhom15.service.PaymentService;
import com.example.doannhom15.service.SeatService;
import com.example.doannhom15.service.ShowtimeService;
import com.example.doannhom15.service.UserService;
import com.example.doannhom15.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class BookingController {
    
    private final MovieService movieService;
    private final CinemaRoomService cinemaRoomService;
    private final ShowtimeService showtimeService;
    private final SeatService seatService;
    private final BookingService bookingService;
    private final UserService userService;
    private final PaymentService paymentService;
    private final ConcessionService concessionService;
    private final VoucherService voucherService;
    private final EmailService emailService;
    
    @GetMapping("/book/{movieId}")
    public String bookMovie(@PathVariable Long movieId, Model model) {
        Movie movie = movieService.getMovieById(movieId);
        if (movie == null) {
            return "redirect:/movies";
        }
        
        List<Showtime> showtimes = showtimeService.getShowtimesByMovieUpcoming(movieId);
        
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimes);
        
        return "user/book";
    }
    
    @GetMapping("/select-seats")
    public String selectSeats(@RequestParam Long showtimeId, @RequestParam(required = false) String error, Model model) {
        Showtime showtime = showtimeService.getShowtimeById(showtimeId);
        if (showtime == null) {
            return "redirect:/movies";
        }
        
        Long roomId = showtime.getRoom().getId();
        cinemaRoomService.ensureSeatsForRoomIfEmpty(roomId);
        List<Seat> seats = seatService.getSeatsByRoom(roomId);
        List<Long> bookedSeatIds = bookingService.getBookedSeatIds(showtimeId);
        
        // Format: "A1:1:STANDARD,L1:111:DOUBLE" để frontend render đúng số ghế + loại (ghế đôi dàn cách)
        StringBuilder seatMap = new StringBuilder();
        for (Seat s : seats) {
            if (seatMap.length() > 0) seatMap.append(",");
            seatMap.append(s.getSeatNumber()).append(":").append(s.getId()).append(":").append(s.getSeatType() != null ? s.getSeatType() : "STANDARD");
        }
        StringBuilder bookedStr = new StringBuilder();
        for (Long id : bookedSeatIds) {
            if (bookedStr.length() > 0) bookedStr.append(",");
            bookedStr.append(id);
        }
        
        model.addAttribute("showtime", showtime);
        model.addAttribute("seatMapStr", seatMap.toString());
        model.addAttribute("bookedSeatIdsStr", bookedStr.toString());
        model.addAttribute("totalSeats", seats.size());
        model.addAttribute("ticketPrice", showtime.getPrice().longValue());
        model.addAttribute("error", error);
        
        return "user/select-seats";
    }
    
    @PostMapping("/confirm-booking")
public String confirmBooking(@RequestParam Long showtimeId,
                             @RequestParam String seatIds,
                             Authentication authentication,
                             Model model) {

    if (authentication == null) {
        return "redirect:/auth/login";
    }

    User user = userService.findByUsernameOrEmail(authentication.getName());
    if (user == null) {
        return "redirect:/auth/login?error=session";
    }

    try {

        List<Long> seatIdList = Arrays.stream(seatIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();

        Booking booking = bookingService.createBooking(user, showtimeId, seatIdList);
        paymentService.createPayment(booking);
        return "redirect:/user/order-concessions/" + booking.getId();

    } catch (Exception e) {
        String errMsg = java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        return "redirect:/user/select-seats?showtimeId=" + showtimeId + "&error=" + errMsg;
    }
}
    @GetMapping("/order-concessions/{bookingId}")
    public String orderConcessionsPage(@PathVariable Long bookingId, Model model, Authentication authentication) {
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) return "redirect:/user/bookings";
        User user = userService.findByUsernameOrEmail(authentication.getName());
        if (user == null || !booking.getUser().getId().equals(user.getId())) return "redirect:/user/bookings";
        if (booking.getStatus() != Booking.BookingStatus.PENDING) return "redirect:/user/payment/" + bookingId;
        java.util.Map<Long, Integer> concessionQty = new java.util.HashMap<>();
        if (booking.getBookingConcessions() != null) {
            for (com.example.doannhom15.model.BookingConcession bc : booking.getBookingConcessions()) {
                concessionQty.put(bc.getConcessionItem().getId(), bc.getQuantity());
            }
        }
        model.addAttribute("booking", booking);
        model.addAttribute("concessions", concessionService.getActiveItems());
        model.addAttribute("concessionQty", concessionQty);
        return "user/order-concessions";
    }

    @PostMapping("/order-concessions/{bookingId}")
    public String submitOrderConcessions(@PathVariable Long bookingId,
                                        @RequestParam(required = false) java.util.Map<String, String> params,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) return "redirect:/user/bookings";
        User user = userService.findByUsernameOrEmail(authentication.getName());
        if (user == null || !booking.getUser().getId().equals(user.getId())) return "redirect:/user/bookings";
        String concessionItems = params != null ? params.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().startsWith("qty_"))
                .filter(e -> e.getValue() != null && !e.getValue().trim().isEmpty() && !"0".equals(e.getValue().trim()))
                .map(e -> e.getKey().replace("qty_", "") + ":" + e.getValue().trim())
                .collect(java.util.stream.Collectors.joining(",")) : "";
        try {
            bookingService.updateOrder(bookingId, concessionItems, null, null, false);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("voucherError", e.getMessage() != null ? e.getMessage() : "Không thể cập nhật đơn. Vui lòng thử lại.");
        }
        return "redirect:/user/payment/" + bookingId;
    }

    @GetMapping("/payment/{bookingId}")
    public String paymentPage(@PathVariable Long bookingId,
                              @RequestParam(value = "success", required = false) Boolean success,
                              Model model,
                              Authentication authentication) {
    
        boolean isSuccess = Boolean.TRUE.equals(success);
        Booking booking = isSuccess
                ? bookingService.getBookingByIdForEmail(bookingId)
                : bookingService.getBookingById(bookingId);
        if (booking == null) {
            return "redirect:/home";
        }
    
        User user = userService.findByUsernameOrEmail(authentication.getName());
        if (user == null || !booking.getUser().getId().equals(user.getId())) {
            return "redirect:/user/bookings";
        }
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            return "redirect:/user/bookings?expired=1";
        }
        Payment payment = paymentService.getPaymentByBookingId(bookingId);
        boolean isExpired = payment != null && payment.getExpiredAt() != null &&
                payment.getExpiredAt().isBefore(java.time.LocalDateTime.now());
        if (isExpired && booking.getStatus() == Booking.BookingStatus.PENDING) {
            emailService.sendPaymentExpiredEmail(booking.getId());
            bookingService.cancelBooking(booking.getId());
            if (payment != null) paymentService.markAsExpired(bookingId);
            return "redirect:/user/bookings?expired=1";
        }
        String expiredAtIso = (payment != null && payment.getExpiredAt() != null)
                ? payment.getExpiredAt().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : "";
        
        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        model.addAttribute("totalPayment", paymentService.getTotalAmount(booking));
        model.addAttribute("isExpired", isExpired);
        model.addAttribute("success", isSuccess);
        model.addAttribute("expiredAtIso", expiredAtIso);
        model.addAttribute("concessions", concessionService.getActiveItems());
        java.util.Map<Long, Integer> concessionQty = new java.util.HashMap<>();
        if (booking.getBookingConcessions() != null) {
            for (com.example.doannhom15.model.BookingConcession bc : booking.getBookingConcessions()) {
                concessionQty.put(bc.getConcessionItem().getId(), bc.getQuantity());
            }
        }
        model.addAttribute("concessionQty", concessionQty);

        // Voucher thuộc tài khoản (welcome voucher / voucher riêng)
        model.addAttribute("ticketVouchers",
                voucherService.listAvailableVouchersForUser(user.getId(), Voucher.DiscountType.TICKET));
        model.addAttribute("concessionVouchers",
                voucherService.listAvailableVouchersForUser(user.getId(), Voucher.DiscountType.CONCESSION));
    
        return "user/payment";
    }

    @PostMapping("/payment/{bookingId}/update-order")
    public String updateOrder(@PathVariable Long bookingId,
                              @RequestParam(required = false) java.util.Map<String, String> params,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) return "redirect:/user/bookings";
        User user = userService.findByUsernameOrEmail(authentication.getName());
        if (user == null || !booking.getUser().getId().equals(user.getId())) return "redirect:/user/bookings";
        String ticketV = params != null ? params.get("ticketVoucherCode") : null;
        String concV = params != null ? params.get("concessionVoucherCode") : null;
        String concessionItems = "";
        if (params != null) {
            concessionItems = params.entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getKey().startsWith("qty_"))
                    .filter(e -> e.getValue() != null && !e.getValue().trim().isEmpty() && !"0".equals(e.getValue().trim()))
                    .map(e -> e.getKey().replace("qty_", "") + ":" + e.getValue().trim())
                    .collect(java.util.stream.Collectors.joining(","));
        }
        try {
            bookingService.updateOrder(bookingId, concessionItems, ticketV, concV, true);
            boolean applied = (ticketV != null && !ticketV.isBlank()) || (concV != null && !concV.isBlank());
            if (applied) redirectAttributes.addFlashAttribute("voucherApplied", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("voucherError", e.getMessage() != null ? e.getMessage() : "Không thể áp dụng mã. Vui lòng thử lại.");
        }
        return "redirect:/user/payment/" + bookingId;
    }
    // Xác nhận đã thanh toán (user bấm nút) — chỉ chủ đơn được xác nhận; email gửi đúng khách trên đơn.
    @PostMapping("/payment/{bookingId}/confirm")
    public String confirmPayment(@PathVariable Long bookingId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        User current = userService.findByUsernameOrEmail(authentication.getName());
        if (current == null) {
            return "redirect:/auth/login?error=session";
        }
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt vé.");
            return "redirect:/user/bookings";
        }
        if (!booking.getUser().getId().equals(current.getId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không thể xác nhận thanh toán cho đơn của người khác.");
            return "redirect:/user/bookings";
        }
        try {
            Payment payment = paymentService.getPaymentByBookingId(bookingId);

            if (payment == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thanh toán");
                return "redirect:/user/payment/" + bookingId;
            }

            if (payment.getExpiredAt() != null && payment.getExpiredAt().isBefore(java.time.LocalDateTime.now())) {
                paymentService.markAsExpired(bookingId);
                redirectAttributes.addFlashAttribute("error", "Thời gian thanh toán đã hết hạn");
                return "redirect:/user/payment/" + bookingId;
            }

            paymentService.confirmPayment(bookingId);
            bookingService.confirmBooking(bookingId);
            Booking b = bookingService.getBookingById(bookingId);
            if (b != null) {
                if (b.getVoucherCode() != null && !b.getVoucherCode().isBlank()) {
                    voucherService.incrementUsedByCode(b.getVoucherCode());
                    voucherService.recordVoucherUsage(b.getUser().getId(), b.getVoucherCode());
                }
                if (b.getConcessionVoucherCode() != null && !b.getConcessionVoucherCode().isBlank()) {
                    voucherService.incrementUsedByCode(b.getConcessionVoucherCode());
                    voucherService.recordVoucherUsage(b.getUser().getId(), b.getConcessionVoucherCode());
                }
                emailService.sendPaymentConfirmationEmail(bookingId);
            }
            return "redirect:/user/payment/" + bookingId + "?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/payment/" + bookingId;
        }
    }
    
    // Hiển thị chi tiết booking (cũ - giữ lại để tương thích)
    @GetMapping("/booking/{bookingId}")
    public String bookingConfirmation(@PathVariable Long bookingId, Model model, Authentication authentication) {
        return "redirect:/user/payment/" + bookingId;
    }
    
    // Xử lý thanh toán (cũ - giữ lại để tương thích)
    @PostMapping("/booking/{bookingId}/pay")
    public String processPayment(@PathVariable Long bookingId, Model model) {
        return "redirect:/user/payment/" + bookingId;
    }
    
    @GetMapping("/bookings")
    public String myBookings(Authentication authentication, Model model) {
        User user = userService.findByUsernameOrEmail(authentication.getName());
        bookingService.cancelExpiredPendingBookings(user);
        List<Booking> bookings = bookingService.getUserBookings(user);
        java.util.Map<Long, java.math.BigDecimal> totalPaymentMap = new java.util.HashMap<>();
        for (Booking b : bookings) {
            totalPaymentMap.put(b.getId(), paymentService.getTotalAmount(b));
        }
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalPaymentMap", totalPaymentMap);
        return "user/my-bookings";
    }
    
    /** Xem chi tiết đặt vé (mọi trạng thái, chỉ xem). */
    @GetMapping("/booking-detail/{bookingId}")
    public String bookingDetail(@PathVariable Long bookingId, Model model, Authentication authentication) {
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) return "redirect:/user/bookings";
        User user = userService.findByUsernameOrEmail(authentication.getName());
        if (user == null || !booking.getUser().getId().equals(user.getId())) return "redirect:/user/bookings";
        model.addAttribute("booking", booking);
        return "user/booking-detail";
    }

    // Hiển thị vé đã mua (sau khi admin xác nhận)
    @GetMapping("/ticket/{bookingId}")
    public String viewTicket(@PathVariable Long bookingId, Model model, Authentication authentication) {
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            return "redirect:/user/bookings";
        }
        
        // Kiểm tra vé thuộc về user đang đăng nhập
        User user = userService.findByUsernameOrEmail(authentication.getName());
        if (user == null || !booking.getUser().getId().equals(user.getId())) {
            return "redirect:/user/bookings";
        }
        
        // Lấy thông tin payment
        Payment payment = paymentService.getPaymentByBookingId(bookingId);
        
        // Chỉ hiển thị vé nếu đã thanh toán hoặc đã xác nhận
        if (booking.getStatus() != Booking.BookingStatus.PAID && 
            booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            return "redirect:/user/bookings";
        }
        
        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        model.addAttribute("totalPayment", paymentService.getTotalAmount(booking));
        return "user/ticket";
    }
}
