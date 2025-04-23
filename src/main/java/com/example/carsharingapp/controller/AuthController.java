package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.user.UserLoginRequestDto;
import com.example.carsharingapp.dto.user.UserLoginResponseDto;
import com.example.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.exception.RegistrationException;
import com.example.carsharingapp.security.AuthenticationService;
import com.example.carsharingapp.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user registration")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authService;

    @PostMapping("registration")
    @Operation(summary = "Register a new user",
            description = "Creates a new user account using the provided registration details")
    public UserResponseDto registerUser(@RequestBody @Valid UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        return userService.register(requestDto);
    }

    @PostMapping("login")
    @Operation(
            summary = "Login an authenticated user",
            description = "Login an authenticated user"
    )
    public UserLoginResponseDto login(@RequestBody @Valid UserLoginRequestDto requestDto) {
        return authService.authenticate(requestDto);
    }
}
