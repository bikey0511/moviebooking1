package com.example.doannhom15.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Ghi nhận lượt chơi quiz thắng của user — dùng để kiểm soát mỗi user chỉ nhận
 * được 1 voucher quiz (hoặc giới hạn theo ngày nếu muốn).
 */
@Entity
@Table(name = "quiz_participations")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Mã voucher sau khi đổi; null / "-" nếu chưa có hoặc không đạt */
    @Column(length = 50)
    private String voucherCode;

    @Column(nullable = false)
    private LocalDateTime participatedAt;

    @Builder.Default
    private boolean won = false;
}
