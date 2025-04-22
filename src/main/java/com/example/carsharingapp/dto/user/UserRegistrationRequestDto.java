package com.example.carsharingapp.dto.user;

import com.example.carsharingapp.validation.FieldMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@FieldMatches(field = "password", fieldMatch = "repeatPassword",
        message = "Password and repeated password must be equal")
public class UserRegistrationRequestDto {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Length(min = 3, max = 30)
    private String password;
    @NotBlank
    @Length(min = 3, max = 30)
    private String repeatPassword;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
}
