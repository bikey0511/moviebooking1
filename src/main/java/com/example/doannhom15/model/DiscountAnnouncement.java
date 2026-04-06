package com.example.doannhom15.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "discount_announcements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountAnnouncement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    /** Đường dẫn tĩnh, ví dụ /images/xxx.jpg */
    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** Thông báo đang hiển thị cho khách (đã đăng và chưa hết hạn). */
    public boolean isActiveAt(LocalDateTime now) {
        return !publishedAt.isAfter(now) && expiresAt.isAfter(now);
    }
}
