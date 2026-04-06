package com.example.doannhom15.controller;

import com.example.doannhom15.model.User;
import com.example.doannhom15.service.UserService;
import com.example.doannhom15.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final VoucherService voucherService;
    
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           @RequestParam(required = false) String errorOAuth2,
                           @RequestParam(required = false) String disabled,
                           @RequestParam(required = false) String session,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng. Vui lòng thử lại.");
        }
        if (errorOAuth2 != null) {
            model.addAttribute("error", "Đăng nhập bằng Google thất bại. Vui lòng thử lại.");
        }
        if (disabled != null) {
            model.addAttribute("error", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }
        if (session != null) {
            model.addAttribute("error", "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
        }
        if (logout != null) {
            model.addAttribute("message", true);
        }
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String register(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            User created = userService.createUser(user.getUsername(), user.getEmail(), user.getPassword(), User.Role.USER);
            var welcome = voucherService.createWelcomeVoucherIfNeeded(created.getId());
            if (welcome != null) {
                redirectAttributes.addFlashAttribute("welcomeVoucher", welcome.getCode());
                redirectAttributes.addFlashAttribute("success",
                        "Đăng ký thành công! Bạn nhận được voucher chào mừng: " + welcome.getCode() + " (giảm 20% vé). Vui lòng đăng nhập để sử dụng.");
            } else {
                redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.");
            }
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }
    
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
