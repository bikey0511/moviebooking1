package com.example.doannhom15.controller;

import com.example.doannhom15.model.User;
import com.example.doannhom15.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final UserRepository userRepository;
    
    @GetMapping
    public String users(Model model, Authentication auth) {
        User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("currentUserId", currentUser != null ? currentUser.getId() : null);
        return "admin/users";
    }
    
    @PostMapping("/{id}/enable")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(true);
                    userRepository.save(user);
                    redirectAttributes.addFlashAttribute("success", "Đã mở khóa tài khoản thành công");
                    return "redirect:/admin/users";
                })
                .orElse("redirect:/admin/users");
    }
    
    @PostMapping("/{id}/disable")
    public String disableUser(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication auth) {
        User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
        User targetUser = userRepository.findById(id).orElse(null);
        
        if (targetUser == null) {
            return "redirect:/admin/users";
        }
        // Không cho phép khóa chính mình
        if (currentUser != null && currentUser.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Không thể khóa tài khoản của chính bạn");
            return "redirect:/admin/users";
        }
        // Không cho phép khóa tài khoản Admin khác
        if (targetUser.getRole() == User.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Không thể khóa tài khoản Quản trị viên");
            return "redirect:/admin/users";
        }
        
        targetUser.setEnabled(false);
        userRepository.save(targetUser);
        redirectAttributes.addFlashAttribute("success", "Đã khóa tài khoản thành công");
        return "redirect:/admin/users";
    }
}
