package com.example.doannhom15.dto;

import java.time.LocalDateTime;

/** JSON cho chuông thông báo khuyến mãi (header). */
public record PromoAnnouncementJson(
        Long id,
        String title,
        String imageUrl,
        String description,
        LocalDateTime expiresAt
) {}
