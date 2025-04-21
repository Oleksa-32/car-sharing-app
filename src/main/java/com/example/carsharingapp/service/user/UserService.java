package com.example.carsharingapp.service.user;

import com.example.carsharingapp.dto.user.UpdateUserProfileRequestDto;
import com.example.carsharingapp.dto.user.UpdateUserRoleRequestDto;
import com.example.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserResponseDto updateRole(Long userId, UpdateUserRoleRequestDto dto);

    UserResponseDto getProfile(String email);

    UserResponseDto updateProfile(String email, UpdateUserProfileRequestDto dto);
}
