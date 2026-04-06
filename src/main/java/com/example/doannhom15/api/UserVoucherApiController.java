package com.example.doannhom15.api;

import com.example.doannhom15.model.User;
import com.example.doannhom15.model.Voucher;
import com.example.doannhom15.service.UserService;
import com.example.doannhom15.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my-vouchers")
@RequiredArgsConstructor
public class UserVoucherApiController {

    private final UserService userService;
    private final VoucherService voucherService;

    public record MyVoucherJson(
            Long id,
            String code,
            String discountType,
            String valueType,
            String value,
            String validTo,
            int maxUsesPerUser,
            int usesRemaining
    ) { }

    @GetMapping("/available")
    public List<MyVoucherJson> available(Authentication authentication) {
        if (authentication == null) return List.of();
        User u = userService.findByUsernameOrEmail(authentication.getName());
        if (u == null) return List.of();
        Long uid = u.getId();
        List<Voucher> all = voucherService.listAvailableVouchersForUser(uid, null);
        return all.stream().map(v -> {
            int max = voucherService.maxUsesPerUser(v);
            int used = voucherService.countUserUses(uid, v.getId());
            int remaining = Math.max(0, max - used);
            return new MyVoucherJson(
                    v.getId(),
                    v.getCode(),
                    v.getDiscountType() != null ? v.getDiscountType().name() : null,
                    v.getValueType() != null ? v.getValueType().name() : null,
                    v.getValue() != null ? v.getValue().toPlainString() : null,
                    v.getValidTo() != null ? v.getValidTo().toString() : null,
                    max,
                    remaining
            );
        }).toList();
    }
}

