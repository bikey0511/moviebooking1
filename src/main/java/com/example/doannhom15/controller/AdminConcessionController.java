package com.example.doannhom15.controller;

import com.example.doannhom15.model.ConcessionItem;
import com.example.doannhom15.service.ConcessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/staff/concessions")
@RequiredArgsConstructor
public class AdminConcessionController {

    private final ConcessionService concessionService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", concessionService.findAll());
        return "admin/concessions";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("item", ConcessionItem.builder()
                .price(BigDecimal.ZERO)
                .type(ConcessionItem.ConcessionType.BAP)
                .build());
        return "admin/concession-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ConcessionItem item,
                        @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                        RedirectAttributes ra) {

        // Nếu user upload ảnh mới thì lưu vào static/images/concession và cập nhật imageUrl
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String filename = UUID.randomUUID() + "_" + (imageFile.getOriginalFilename() != null
                        ? imageFile.getOriginalFilename().replaceAll("\\s+", "_")
                        : "image");

                Path dir = Paths.get("src/main/resources/static/images/concession");
                Files.createDirectories(dir);
                Path filePath = dir.resolve(filename);

                imageFile.transferTo(filePath);

                item.setImageUrl("/images/concession/" + filename);
            }
        } catch (Exception e) {
            // Không phá flow CRUD; trường hợp upload lỗi thì giữ imageUrl cũ
            ra.addFlashAttribute("error", "Lỗi upload ảnh: " + (e.getMessage() != null ? e.getMessage() : "unknown"));
        }

        concessionService.save(item);
        ra.addFlashAttribute("success", item.getId() == null ? "Thêm đồ ăn/uống thành công!" : "Cập nhật thành công!");
        return "redirect:/staff/concessions";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        ConcessionItem item = concessionService.getById(id);
        if (item == null) return "redirect:/staff/concessions";
        model.addAttribute("item", item);
        return "admin/concession-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        concessionService.deleteById(id);
        ra.addFlashAttribute("success", "Đã xóa!");
        return "redirect:/staff/concessions";
    }
}
