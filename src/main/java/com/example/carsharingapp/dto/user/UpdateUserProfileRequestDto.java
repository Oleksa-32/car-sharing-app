package com.example.carsharingapp.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateUserProfileRequestDto {
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;
}
