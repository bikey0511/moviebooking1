package com.example.doannhom15.repository;

import com.example.doannhom15.model.User;
import com.example.doannhom15.model.Voucher;
import com.example.doannhom15.model.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {
    Optional<VoucherUsage> findByUserAndVoucher(User user, Voucher voucher);
    void deleteByVoucherId(Long voucherId);
}
