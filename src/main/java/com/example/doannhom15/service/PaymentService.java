package com.example.doannhom15.service;

import com.example.doannhom15.model.Booking;
import com.example.doannhom15.model.Payment;
import com.example.doannhom15.repository.BookingRepository;
import com.example.doannhom15.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    
    @Transactional
    public Payment createPayment(Booking booking) {
        String paymentCode = "PAY" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal amount = getTotalAmount(booking);
        Payment payment = Payment.builder()
                .booking(booking)
                .paymentCode(paymentCode)
                .amount(amount)
                .status(Payment.PaymentStatus.PENDING)
                .expiredAt(LocalDateTime.now().plusMinutes(6))
                .build();
        return paymentRepository.save(payment);
    }

    /** Tổng tiền thanh toán = vé + bắp nước - giảm giá */
    public BigDecimal getTotalAmount(Booking booking) {
        BigDecimal ticket = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal concession = booking.getConcessionTotal() != null ? booking.getConcessionTotal() : BigDecimal.ZERO;
        BigDecimal discount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
        return ticket.add(concession).subtract(discount).max(BigDecimal.ZERO);
    }

    @Transactional
    public void updatePaymentAmount(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return;
        BigDecimal amount = getTotalAmount(booking);
        paymentRepository.findByBookingId(bookingId).ifPresent(p -> {
            p.setAmount(amount);
            paymentRepository.save(p);
        });
    }
    
    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).orElse(null);
    }
    
    public Payment getPaymentByPaymentCode(String paymentCode) {
        return paymentRepository.findByPaymentCode(paymentCode).orElse(null);
    }
    
    @Transactional
    public Payment confirmPayment(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }
    
    @Transactional
    public Payment markAsExpired(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setStatus(Payment.PaymentStatus.EXPIRED);
        return paymentRepository.save(payment);
    }
}
