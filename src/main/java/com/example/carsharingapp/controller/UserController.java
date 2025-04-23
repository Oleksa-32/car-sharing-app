package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.user.UpdateUserProfileRequestDto;
import com.example.carsharingapp.dto.user.UpdateUserRoleRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User management", description = "Endpoints for user profile and role management")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update user role", description = "Update a user's role (manager only)")
    public UserResponseDto updateRole(
            @PathVariable("id") Long userId,
            @RequestBody @Valid UpdateUserRoleRequestDto dto
    ) {
        return userService.updateRole(userId, dto);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile", description = "Retrieve the current user's"
            + " profile information")
    public UserResponseDto getMe(@AuthenticationPrincipal UserDetails principal) {
        return userService.getProfile(principal.getUsername());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile", description = "Update profile information"
            + " for the current user")
    public UserResponseDto updateMe(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody @Valid UpdateUserProfileRequestDto dto
    ) {
        return userService.updateProfile(principal.getUsername(), dto);
    }
}
