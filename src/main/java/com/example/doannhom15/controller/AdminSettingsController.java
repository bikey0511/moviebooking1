package com.example.doannhom15.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {
    
    @GetMapping
    public String settings(Model model) {
        return "admin/settings";
    }
    
    @PostMapping("/update")
    public String updateSettings(@RequestParam String siteName, 
                                  @RequestParam String contactEmail,
                                  Model model) {
        // Settings update logic would go here
        model.addAttribute("message", "Cập nhật cài đặt thành công!");
        return "admin/settings";
    }
}
