package com.example.doannhom15.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;
    
    @Column(nullable = false)
    private LocalDateTime bookingTime;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingSeat> bookingSeats;
    
    /** Tổng tiền bắp nước (0 nếu không mua) */
    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal concessionTotal = java.math.BigDecimal.ZERO;
    
    /** Mã giảm giá vé / phim (loại TICKET) */
    private String voucherCode;

    /** Mã giảm giá bắp nước (loại CONCESSION) */
    private String concessionVoucherCode;
    
    /** Số tiền được giảm (từ voucher) */
    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingConcession> bookingConcessions;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime paymentExpiry; // Thời hạn thanh toán
    
    private String ticketCode; // Mã vé

    /** Thời điểm nhân viên quét mã vé check-in tại rạp (null = chưa vào). */
    private LocalDateTime checkedInAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        bookingTime = LocalDateTime.now();
        if (status == null) {
            status = BookingStatus.PENDING;
        }
        // Mã vé tự động
        ticketCode = "CINEMA-" + System.currentTimeMillis();
    }
    
    public enum BookingStatus {
        PENDING,        // Chờ thanh toán
        PAID,           // Đã thanh toán
        CONFIRMED,      // Đã xác nhận (bởi admin)
        CANCELLED       // Đã hủy
    }
}
