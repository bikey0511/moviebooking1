package com.example.doannhom15.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/** Số lần một tài khoản đã dùng một mã (giới hạn theo loại vé / bắp nước). */
@Entity
@Table(name = "voucher_usages", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "user_id", "voucher_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @Column(nullable = false, columnDefinition = "INTEGER NOT NULL DEFAULT 1")
    @Builder.Default
    private int usageCount = 1;

    private LocalDateTime usedAt;

    @PrePersist
    protected void onCreate() {
        if (usedAt == null) usedAt = LocalDateTime.now();
    }
}
