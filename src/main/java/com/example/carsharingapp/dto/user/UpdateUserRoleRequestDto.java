package com.example.carsharingapp.dto.user;

import com.example.carsharingapp.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRoleRequestDto {
    @NotNull
    private Role.Roles role;
}
