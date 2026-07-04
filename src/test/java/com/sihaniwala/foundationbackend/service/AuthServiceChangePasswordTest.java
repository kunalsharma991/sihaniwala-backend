package com.sihaniwala.foundationbackend.service;

import com.sihaniwala.foundationbackend.dto.ChangePasswordRequest;
import com.sihaniwala.foundationbackend.entity.User;
import com.sihaniwala.foundationbackend.exception.BadRequestException;
import com.sihaniwala.foundationbackend.repository.UserRepository;
import com.sihaniwala.foundationbackend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceChangePasswordTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encoded-current")
                .role(User.Role.USER)
                .enabled(true)
                .build();
    }

    @Test
    void changePasswordShouldSucceedWhenCurrentPasswordMatchesAndNewPasswordIsDifferent() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("current123");
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current123", "encoded-current")).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", "encoded-current")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encoded-new");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.changePassword("test@example.com", request);

        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordShouldFailWhenCurrentPasswordIsIncorrect() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("newPassword123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-current")).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.changePassword("test@example.com", request));

        assertEquals("Current password is incorrect", exception.getMessage());
    }

    @Test
    void changePasswordShouldFailWhenNewPasswordsDoNotMatch() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("current123");
        request.setNewPassword("newPassword123");
        request.setConfirmPassword("different123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current123", "encoded-current")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.changePassword("test@example.com", request));

        assertEquals("New password and confirm password do not match", exception.getMessage());
    }
}
