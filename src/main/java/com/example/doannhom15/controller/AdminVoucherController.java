package com.example.doannhom15.controller;

import com.example.doannhom15.model.Voucher;
import com.example.doannhom15.service.DiscountAnnouncementService;
import com.example.doannhom15.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/staff/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;
    private final DiscountAnnouncementService discountAnnouncementService;

    private static final DateTimeFormatter VOUCHER_DATETIME_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private void addVoucherFormAttrs(Voucher v, Model model) {
        String minFrom = LocalDate.now().atStartOfDay().format(VOUCHER_DATETIME_LOCAL);
        model.addAttribute("minDateTime", minFrom);
        boolean fromInPast = v.getValidFrom() != null && v.getValidFrom().toLocalDate().isBefore(LocalDate.now());
        model.addAttribute("applyMinToValidFrom", v.getId() == null || !fromInPast);
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vouchers", voucherService.findAll());
        return "admin/vouchers";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        Voucher v = Voucher.builder()
                .value(BigDecimal.ZERO)
                .validFrom(LocalDateTime.now().withSecond(0).withNano(0))
                .validTo(LocalDateTime.now().plusMonths(1).withSecond(0).withNano(0))
                .build();
        model.addAttribute("voucher", v);
        model.addAttribute("promoAnnouncements", discountAnnouncementService.findAllOrderByNewest());
        addVoucherFormAttrs(v, model);
        return "admin/voucher-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Voucher voucher, RedirectAttributes ra) {
        LocalDate today = LocalDate.now();
        if (voucher.getValidFrom() != null && voucher.getValidTo() != null
                && !voucher.getValidTo().isAfter(voucher.getValidFrom())) {
            ra.addFlashAttribute("error", "Thời điểm \"Đến hết\" phải sau \"Có hiệu lực từ\".");
            if (voucher.getId() == null) {
                return "redirect:/staff/vouchers/new";
            }
            return "redirect:/staff/vouchers/edit/" + voucher.getId();
        }
        if (voucher.getId() == null) {
            if (voucher.getValidFrom() != null && voucher.getValidFrom().toLocalDate().isBefore(today)) {
                ra.addFlashAttribute("error", "Ngày bắt đầu hiệu lực không được trước hôm nay.");
                return "redirect:/staff/vouchers/new";
            }
        }
        if (voucher.getValidTo() != null && voucher.getValidTo().toLocalDate().isBefore(today)) {
            ra.addFlashAttribute("error", "Ngày kết thúc không được trước hôm nay.");
            if (voucher.getId() == null) {
                return "redirect:/staff/vouchers/new";
            }
            return "redirect:/staff/vouchers/edit/" + voucher.getId();
        }
        if (voucher.getDiscountType() == null) {
            voucher.setDiscountType(Voucher.DiscountType.TICKET);
        }
        if (voucher.getValueType() == null) {
            voucher.setValueType(Voucher.ValueType.PERCENT);
        }
        if (voucher.getValue() == null) {
            voucher.setValue(BigDecimal.ZERO);
        }
        if (voucher.getLinkedAnnouncementId() != null && voucher.getLinkedAnnouncementId() <= 0) {
            voucher.setLinkedAnnouncementId(null);
        }
        boolean isNew = voucher.getId() == null;
        voucherService.save(voucher);
        ra.addFlashAttribute("success", isNew ? "Thêm mã giảm giá thành công!" : "Cập nhật thành công!");
        return "redirect:/staff/vouchers";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Voucher v = voucherService.getById(id);
        if (v == null) return "redirect:/staff/vouchers";
        model.addAttribute("voucher", v);
        model.addAttribute("promoAnnouncements", discountAnnouncementService.findAllOrderByNewest());
        addVoucherFormAttrs(v, model);
        return "admin/voucher-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        voucherService.deleteById(id);
        ra.addFlashAttribute("success", "Đã xóa mã giảm giá!");
        return "redirect:/staff/vouchers";
    }
}
