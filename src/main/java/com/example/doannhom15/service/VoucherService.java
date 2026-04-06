package com.example.doannhom15.service;

import com.example.doannhom15.model.User;
import com.example.doannhom15.model.Voucher;
import com.example.doannhom15.model.VoucherUsage;
import com.example.doannhom15.repository.DiscountAnnouncementRepository;
import com.example.doannhom15.repository.UserRepository;
import com.example.doannhom15.repository.VoucherRepository;
import com.example.doannhom15.repository.VoucherUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherService {
    /** Số lần tối đa mỗi tài khoản được áp cùng một mã (vé hoặc bắp nước); mỗi lần thanh toán thành công trừ 1. */
    public static final int MAX_USES_PER_USER_VOUCHER = 5;

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final UserRepository userRepository;
    private final DiscountAnnouncementRepository discountAnnouncementRepository;

    public Voucher findByCode(String code) {
        return voucherRepository.findByCodeIgnoreCaseAndActiveTrue(code).orElse(null);
    }

    public boolean isValid(Voucher v) {
        if (v == null || !v.isValid()) return false;
        if (v.getLinkedAnnouncementId() != null) {
            return discountAnnouncementRepository.findById(v.getLinkedAnnouncementId())
                    .map(a -> a.isActiveAt(LocalDateTime.now()))
                    .orElse(false);
        }
        return true;
    }

    public int maxUsesPerUser(Voucher voucher) {
        if (voucher == null) return 0;
        if (voucher.getMaxUses() != null) {
            return Math.min(MAX_USES_PER_USER_VOUCHER, voucher.getMaxUses());
        }
        return MAX_USES_PER_USER_VOUCHER;
    }

    public int countUserUses(Long userId, Long voucherId) {
        if (userId == null || voucherId == null) return 0;
        var u = userRepository.findById(userId);
        var v = voucherRepository.findById(voucherId);
        if (u.isEmpty() || v.isEmpty()) return 0;
        return voucherUsageRepository.findByUserAndVoucher(u.get(), v.get())
                .map(VoucherUsage::getUsageCount)
                .orElse(0);
    }

    /** Còn được phép áp dụng mã (theo số lần đã thanh toán thành công). */
    public boolean canUserUseVoucher(Long userId, Voucher voucher) {
        if (userId == null || voucher == null) return false;
        return countUserUses(userId, voucher.getId()) < maxUsesPerUser(voucher);
    }

    @Transactional
    public void recordVoucherUsage(Long userId, String voucherCode) {
        if (userId == null || voucherCode == null || voucherCode.isBlank()) return;
        var userOpt = userRepository.findById(userId);
        var voucherOpt = voucherRepository.findByCodeIgnoreCaseAndActiveTrue(voucherCode);
        if (userOpt.isEmpty() || voucherOpt.isEmpty()) return;
        User user = userOpt.get();
        Voucher voucher = voucherOpt.get();
        var existing = voucherUsageRepository.findByUserAndVoucher(user, voucher);
        if (existing.isPresent()) {
            VoucherUsage u = existing.get();
            u.setUsageCount(u.getUsageCount() + 1);
            u.setUsedAt(LocalDateTime.now());
            voucherUsageRepository.save(u);
        } else {
            voucherUsageRepository.save(VoucherUsage.builder()
                    .user(user)
                    .voucher(voucher)
                    .usageCount(1)
                    .build());
        }
    }

    /**
     * Tính số tiền giảm theo voucher.
     * @param voucher mã giảm giá
     * @param ticketTotal tổng tiền vé
     * @param concessionTotal tổng tiền bắp nước
     * @return số tiền được giảm
     */
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal ticketTotal, BigDecimal concessionTotal) {
        if (voucher == null || !isValid(voucher)) return BigDecimal.ZERO;
        BigDecimal target = voucher.getDiscountType() == Voucher.DiscountType.TICKET ? ticketTotal : concessionTotal;
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        BigDecimal discount;
        if (voucher.getValueType() == Voucher.ValueType.PERCENT) {
            discount = target.multiply(voucher.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = voucher.getValue().min(target);
        }
        return discount;
    }

    @Transactional
    public void incrementUsedCount(Long voucherId) {
        voucherRepository.findById(voucherId).ifPresent(v -> {
            v.setUsedCount(v.getUsedCount() + 1);
            voucherRepository.save(v);
        });
    }

    @Transactional
    public void incrementUsedByCode(String code) {
        if (code == null || code.isBlank()) return;
        voucherRepository.findByCodeIgnoreCaseAndActiveTrue(code).ifPresent(v -> {
            v.setUsedCount(v.getUsedCount() + 1);
            voucherRepository.save(v);
        });
    }

    public List<Voucher> findAll() {
        return voucherRepository.findAllByOrderByCreatedAtDesc();
    }

    public Voucher save(Voucher v) {
        return voucherRepository.save(v);
    }

    /** Tạo voucher chào mừng cho user mới đăng ký (1 lần). */
    @Transactional
    public Voucher createWelcomeVoucherIfNeeded(Long userId) {
        if (userId == null) return null;
        // Nếu user đã có welcome voucher trước đó thì không tạo thêm
        List<Voucher> existing = voucherRepository.findByOwnerUserIdAndActiveTrueOrderByCreatedAtDesc(userId);
        boolean hasWelcome = existing.stream().anyMatch(v ->
                v != null
                        && v.getCode() != null
                        && v.getCode().toUpperCase(Locale.ROOT).startsWith("WELCOME-"));
        if (hasWelcome) return null;

        // Mặc định: giảm 20% tiền vé, hiệu lực 30 ngày, giới hạn dùng 1 lần tổng (usedCount)
        String code;
        do {
            code = ("WELCOME-" + userId + "-" + UUID.randomUUID().toString().replace("-", "")
                    .substring(0, 6).toUpperCase(Locale.ROOT));
        } while (voucherRepository.findByCodeIgnoreCaseAndActiveTrue(code).isPresent());

        Voucher v = Voucher.builder()
                .code(code)
                .ownerUserId(userId)
                .discountType(Voucher.DiscountType.TICKET)
                .valueType(Voucher.ValueType.PERCENT)
                .value(new BigDecimal("20"))
                .validFrom(LocalDateTime.now())
                .validTo(LocalDateTime.now().plusDays(30))
                .maxUses(1)
                .active(true)
                .build();
        return voucherRepository.save(v);
    }

    /** Voucher user có thể nhìn thấy và dùng được (global + voucher riêng). */
    public List<Voucher> listAvailableVouchersForUser(Long userId, Voucher.DiscountType discountType) {
        if (userId == null) return List.of();
        return voucherRepository.findVisibleForUser(userId).stream()
                .filter(this::isValid)
                .filter(v -> discountType == null || v.getDiscountType() == discountType)
                .filter(v -> canUserUseVoucher(userId, v))
                .toList();
    }

    @Transactional
    public void deleteById(Long id) {
        // Xóa các bản ghi sử dụng voucher trước (tránh lỗi foreign key)
        voucherUsageRepository.deleteByVoucherId(id);
        voucherRepository.deleteById(id);
    }

    public Voucher getById(Long id) {
        return voucherRepository.findById(id).orElse(null);
    }
}
