package com.example.doannhom15.service;

import com.example.doannhom15.model.Booking;
import com.example.doannhom15.model.User;
import com.example.doannhom15.service.BookingService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final BookingService bookingService;
    private final UserService userService;

    @Async
    public void sendTicketEmail(Long bookingId) {
        try {
            Booking booking = bookingService.getBookingByIdForEmail(bookingId);
            if (booking == null) {
                log.warn("Cannot send email: booking not found {}", bookingId);
                return;
            }
            User user = resolveBookingUser(booking);
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("Cannot send email: user or email is null for booking {}", bookingId);
                return;
            }

            String to = user.getEmail().trim();
            String subject = "🎫 Vé đã xác nhận — " + booking.getTicketCode();

            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("user", user);
            context.setVariable("movie", booking.getShowtime().getMovie());
            context.setVariable("showtime", booking.getShowtime());
            context.setVariable("room", booking.getShowtime().getRoom());
            context.setVariable("seats", booking.getBookingSeats());
            context.setVariable("totalPrice", booking.getTotalPrice());
            context.setVariable("concessionTotal", booking.getConcessionTotal());
            context.setVariable("discountAmount", booking.getDiscountAmount());
            context.setVariable("voucherCode", booking.getVoucherCode());
            context.setVariable("concessionVoucherCode", booking.getConcessionVoucherCode());

            BigDecimal ticketPrice = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
            BigDecimal concession = booking.getConcessionTotal() != null ? booking.getConcessionTotal() : BigDecimal.ZERO;
            BigDecimal discount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal grossTotal = ticketPrice.add(concession);
            BigDecimal totalPayment = grossTotal.subtract(discount).max(BigDecimal.ZERO);
            context.setVariable("grossTotal", grossTotal);
            context.setVariable("totalPayment", totalPayment);

            String htmlContent = templateEngine.process("email/ticket-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("Cinema <tongphat33@gmail.com>");

            mailSender.send(message);
            log.info("Sent ticket email to {} (user id={}, username={}) for booking {}",
                    to, user.getId(), user.getUsername(), booking.getTicketCode());
        } catch (Exception e) {
            log.error("Failed to send ticket email for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }

    /** Gửi email xác nhận thanh toán thành công (sau khi user chuyển khoản). */
    @Async
    public void sendPaymentConfirmationEmail(Long bookingId) {
        try {
            Booking booking = bookingService.getBookingByIdForEmail(bookingId);
            if (booking == null) {
                log.warn("Cannot send email: booking not found {}", bookingId);
                return;
            }
            User user = resolveBookingUser(booking);
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("Cannot send email: user or email is null for booking {}", bookingId);
                return;
            }

            String to = user.getEmail().trim();
            String subject = "✅ Thanh toán đã ghi nhận — chờ rạp xác nhận vé";

            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("user", user);
            context.setVariable("movie", booking.getShowtime().getMovie());
            context.setVariable("showtime", booking.getShowtime());
            context.setVariable("room", booking.getShowtime().getRoom());
            context.setVariable("seats", booking.getBookingSeats());

            BigDecimal ticketPrice = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
            BigDecimal concession = booking.getConcessionTotal() != null ? booking.getConcessionTotal() : BigDecimal.ZERO;
            BigDecimal discount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
            BigDecimal grossTotal = ticketPrice.add(concession);
            BigDecimal totalPayment = grossTotal.subtract(discount).max(BigDecimal.ZERO);
            context.setVariable("grossTotal", grossTotal);
            context.setVariable("totalPayment", totalPayment);
            context.setVariable("discountAmount", discount);

            String htmlContent = templateEngine.process("email/payment-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("Cinema <tongphat33@gmail.com>");

            mailSender.send(message);
            log.info("Sent payment confirmation email to {} (user id={}, username={}) for booking {}",
                    to, user.getId(), user.getUsername(), booking.getTicketCode());
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }

    /** Gửi email thông báo hết hạn thanh toán (trước khi hủy vé). */
    @Async
    public void sendPaymentExpiredEmail(Long bookingId) {
        try {
            Booking booking = bookingService.getBookingByIdForEmail(bookingId);
            if (booking == null) {
                log.warn("Cannot send expiry email: booking not found {}", bookingId);
                return;
            }
            User user = resolveBookingUser(booking);
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                log.warn("Cannot send expiry email: user or email is null for booking {}", bookingId);
                return;
            }

            String to = user.getEmail().trim();
            String subject = "⏰ Hết hạn thanh toán - Đơn đặt vé đã bị hủy";

            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("user", user);
            context.setVariable("movie", booking.getShowtime().getMovie());
            context.setVariable("showtime", booking.getShowtime());
            context.setVariable("room", booking.getShowtime().getRoom());
            context.setVariable("seats", booking.getBookingSeats());

            String htmlContent = templateEngine.process("email/payment-expired", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("Cinema <tongphat33@gmail.com>");

            mailSender.send(message);
            log.info("Sent expiry email to {} (user id={}, username={}) for booking {}",
                    to, user.getId(), user.getUsername(), booking.getTicketCode());
        } catch (Exception e) {
            log.error("Failed to send expiry email for booking {}: {}", bookingId, e.getMessage(), e);
        }
    }

    /** Luôn lấy user từ DB theo id đơn — tránh proxy/Hibernate lệch, đảm bảo email mới nhất. */
    private User resolveBookingUser(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return null;
        }
        Long uid = booking.getUser().getId();
        if (uid == null) {
            return booking.getUser();
        }
        User fresh = userService.findById(uid);
        return fresh != null ? fresh : booking.getUser();
    }
}
