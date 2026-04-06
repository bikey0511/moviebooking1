package com.example.doannhom15.repository;

import com.example.doannhom15.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCaseAndActiveTrue(String code);
    List<Voucher> findAllByOrderByCreatedAtDesc();

    List<Voucher> findByOwnerUserIdAndActiveTrueOrderByCreatedAtDesc(Long ownerUserId);

    @Query("""
            SELECT v FROM Voucher v
            WHERE v.active = true
              AND (v.ownerUserId IS NULL OR v.ownerUserId = :userId)
            ORDER BY v.createdAt DESC
            """)
    List<Voucher> findVisibleForUser(Long userId);
}
