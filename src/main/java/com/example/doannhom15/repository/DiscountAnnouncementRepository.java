package com.example.doannhom15.repository;

import com.example.doannhom15.model.DiscountAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DiscountAnnouncementRepository extends JpaRepository<DiscountAnnouncement, Long> {

    List<DiscountAnnouncement> findByExpiresAtAfterOrderByPublishedAtDesc(LocalDateTime now);
}
