package com.example.doannhom15.controller;

import com.example.doannhom15.dto.PromoAnnouncementJson;
import com.example.doannhom15.service.DiscountAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/promo-announcements")
@RequiredArgsConstructor
public class PromoAnnouncementApiController {

    private final DiscountAnnouncementService discountAnnouncementService;

    @GetMapping("/active")
    public List<PromoAnnouncementJson> active() {
        return discountAnnouncementService.findActiveNow().stream()
                .map(a -> new PromoAnnouncementJson(
                        a.getId(),
                        a.getTitle(),
                        a.getImageUrl(),
                        a.getDescription(),
                        a.getExpiresAt()))
                .toList();
    }
}
