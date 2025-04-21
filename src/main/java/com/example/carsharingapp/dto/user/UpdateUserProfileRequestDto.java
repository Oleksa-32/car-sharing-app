package com.example.carsharingapp.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileRequestDto {
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;
}
