package com.example.doannhom15.config;

import com.example.doannhom15.model.User;
import com.example.doannhom15.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý đăng nhập thất bại.
 * Kiểm tra nếu tài khoản bị khóa thì hiển thị thông báo riêng.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        
        String username = request.getParameter("username");
        
        // Kiểm tra nếu tài khoản bị khóa
        if (username != null && !username.isBlank()) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && !user.isEnabled()) {
                // Tài khoản bị khóa - chuyển hướng với thông báo riêng
                response.sendRedirect("/auth/login?error=disabled");
                return;
            }
        }
        
        // Các trường hợp đăng nhập thất bại khác
        if (exception instanceof BadCredentialsException) {
            response.sendRedirect("/auth/login?error=bad_credentials");
        } else if (exception instanceof UsernameNotFoundException) {
            response.sendRedirect("/auth/login?error=user_not_found");
        } else {
            response.sendRedirect("/auth/login?error=true");
        }
    }
}
