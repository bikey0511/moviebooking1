package com.example.doannhom15.config;

import com.example.doannhom15.model.User;
import com.example.doannhom15.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String googleId = oauth2User.getAttribute("sub");

        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            
            // Kiểm tra nếu tài khoản bị vô hiệu hóa
            if (!user.isEnabled()) {
                throw new OAuth2AuthenticationException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
            }
            
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }
        } else {
            user = User.builder()
                    .username(email != null ? email.split("@")[0] : "google_" + googleId)
                    .email(email)
                    .password(null)
                    .role(User.Role.USER)
                    .enabled(true)
                    .googleId(googleId)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(user);
        }

        // Return OAuth2User with proper authorities including role
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oauth2User.getAttributes(),
                "email"
        );
    }
}
