package com.example.doannhom15.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** Nếu có: voucher thuộc về 1 user cụ thể (ví dụ: voucher chào mừng đăng ký). */
    @Column(name = "owner_user_id")
    private Long ownerUserId;

    /** Loại giảm: TICKET (giảm tiền vé), CONCESSION (giảm bắp nước) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    /** PERCENT: phần trăm (0-100), FIXED: số tiền cố định */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ValueType valueType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime validFrom;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime validTo;

    /** Số lần tối đa được sử dụng (null = không giới hạn) */
    private Integer maxUses;

    @Column(nullable = false)
    @Builder.Default
    private int usedCount = 0;

    @Builder.Default
    private boolean active = true;

    /**
     * Nếu có: mã chỉ được nhập / áp dụng khi thông báo KM liên kết còn đang chạy
     * (cùng khung thời gian hiển thị trên trang chủ).
     */
    @Column(name = "discount_announcement_id")
    private Long linkedAnnouncementId;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public enum DiscountType {
        TICKET,     // Giảm giá vé xem phim
        CONCESSION  // Giảm giá bắp nước
    }

    public enum ValueType {
        PERCENT,
        FIXED
    }

    public boolean isValid() {
        if (!active) return false;
        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom)) return false;
        if (validTo != null && now.isAfter(validTo)) return false;
        if (maxUses != null && usedCount >= maxUses) return false;
        return true;
    }
}
