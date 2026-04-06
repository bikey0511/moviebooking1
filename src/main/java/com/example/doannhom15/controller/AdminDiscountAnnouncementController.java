package com.example.doannhom15.controller;

import com.example.doannhom15.model.DiscountAnnouncement;
import com.example.doannhom15.service.DiscountAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/staff/discount-announcements")
@RequiredArgsConstructor
public class AdminDiscountAnnouncementController {

    private final DiscountAnnouncementService announcementService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("announcements", announcementService.findAllOrderByNewest());
        model.addAttribute("serverNow", LocalDateTime.now());
        return "admin/discount-announcements";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("announcement", new DiscountAnnouncement());
        model.addAttribute("editMode", false);
        return "admin/discount-announcement-form";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam(defaultValue = "72") int validHours,
                         @RequestParam(required = false) MultipartFile image,
                         RedirectAttributes ra) {
        if (title == null || title.isBlank()) {
            ra.addFlashAttribute("error", "Nhập tiêu đề.");
            return "redirect:/staff/discount-announcements/new";
        }
        if (validHours < 1 || validHours > 24 * 90) {
            validHours = 72;
        }
        LocalDateTime now = LocalDateTime.now();
        DiscountAnnouncement a = DiscountAnnouncement.builder()
                .title(title.trim())
                .description(description != null ? description.trim() : null)
                .publishedAt(now)
                .expiresAt(now.plusHours(validHours))
                .build();
        try {
            if (image != null && !image.isEmpty()) {
                String filename = "promo_" + UUID.randomUUID() + "_" + sanitizeFilename(image.getOriginalFilename());
                Path path = Paths.get("src/main/resources/static/images", filename);
                Files.createDirectories(path.getParent());
                Files.write(path, image.getBytes());
                a.setImageUrl("/images/" + filename);
            }
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Không lưu được hình: " + e.getMessage());
            return "redirect:/staff/discount-announcements/new";
        }
        announcementService.save(a);
        ra.addFlashAttribute("success", "Đã đăng thông báo. Hiển thị đến "
                + a.getExpiresAt().toString().replace('T', ' '));
        return "redirect:/staff/discount-announcements";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        DiscountAnnouncement a = announcementService.getById(id);
        if (a == null) {
            ra.addFlashAttribute("error", "Không tìm thấy thông báo.");
            return "redirect:/staff/discount-announcements";
        }
        model.addAttribute("announcement", a);
        model.addAttribute("editMode", true);
        return "admin/discount-announcement-form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam(defaultValue = "0") int extendHours,
                         @RequestParam(required = false) MultipartFile image,
                         RedirectAttributes ra) {
        DiscountAnnouncement a = announcementService.getById(id);
        if (a == null) {
            ra.addFlashAttribute("error", "Không tìm thấy thông báo.");
            return "redirect:/staff/discount-announcements";
        }
        if (title == null || title.isBlank()) {
            ra.addFlashAttribute("error", "Nhập tiêu đề.");
            return "redirect:/staff/discount-announcements/" + id + "/edit";
        }
        if (extendHours < 0 || extendHours > 24 * 90) {
            extendHours = 0;
        }
        a.setTitle(title.trim());
        a.setDescription(description != null && !description.isBlank() ? description.trim() : null);
        if (extendHours > 0) {
            a.setExpiresAt(a.getExpiresAt().plusHours(extendHours));
        }
        try {
            if (image != null && !image.isEmpty()) {
                String oldUrl = a.getImageUrl();
                String filename = "promo_" + UUID.randomUUID() + "_" + sanitizeFilename(image.getOriginalFilename());
                Path path = Paths.get("src/main/resources/static/images", filename);
                Files.createDirectories(path.getParent());
                Files.write(path, image.getBytes());
                a.setImageUrl("/images/" + filename);
                deleteImageFileIfSafe(oldUrl);
            }
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Không lưu được hình: " + e.getMessage());
            return "redirect:/staff/discount-announcements/" + id + "/edit";
        }
        announcementService.save(a);
        ra.addFlashAttribute("success", "Đã cập nhật thông báo.");
        return "redirect:/staff/discount-announcements";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        DiscountAnnouncement a = announcementService.getById(id);
        if (a != null) {
            deleteImageFileIfSafe(a.getImageUrl());
        }
        announcementService.deleteById(id);
        ra.addFlashAttribute("success", "Đã xóa thông báo.");
        return "redirect:/staff/discount-announcements";
    }

    private static void deleteImageFileIfSafe(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/images/")) {
            return;
        }
        String name = imageUrl.substring("/images/".length());
        if (name.isBlank() || name.contains("..") || name.contains("/") || name.contains("\\")) {
            return;
        }
        try {
            Path p = Paths.get("src/main/resources/static/images", name);
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
        }
    }

    private static String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) return "img";
        String base = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        return base.length() > 80 ? base.substring(base.length() - 80) : base;
    }
}
