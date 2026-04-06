package com.example.doannhom15.service;

import com.example.doannhom15.model.DiscountAnnouncement;
import com.example.doannhom15.repository.DiscountAnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountAnnouncementService {

    private final DiscountAnnouncementRepository repository;

    public List<DiscountAnnouncement> findAllOrderByNewest() {
        return repository.findAll().stream()
                .sorted((a, b) -> b.getPublishedAt().compareTo(a.getPublishedAt()))
                .toList();
    }

    /** Thông báo còn hiệu lực (theo thời gian máy chủ). */
    public List<DiscountAnnouncement> findActiveNow() {
        LocalDateTime now = LocalDateTime.now();
        return repository.findByExpiresAtAfterOrderByPublishedAtDesc(now).stream()
                .filter(a -> !a.getPublishedAt().isAfter(now))
                .toList();
    }

    public DiscountAnnouncement getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public DiscountAnnouncement save(DiscountAnnouncement a) {
        return repository.save(a);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
