package com.example.doannhom15.controller;

import com.example.doannhom15.model.User;
import com.example.doannhom15.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        User user = userService.findByUsernameOrEmail(authentication.getName());
        if (user == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("user", user);
        return "staff/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        User user = userService.findByUsernameOrEmail(authentication.getName());
        if (user == null) {
            return "redirect:/auth/login";
        }
        try {
            userService.updateProfile(user.getId(), email, phone);
            redirectAttributes.addFlashAttribute("success", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/profile";
    }
}
