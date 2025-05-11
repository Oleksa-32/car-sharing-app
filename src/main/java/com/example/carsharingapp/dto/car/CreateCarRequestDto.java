package com.example.carsharingapp.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateCarRequestDto {
    @NotBlank
    @Size(max = 100, message = "Car length must be less than 100 characters")
    private String model;
    @NotBlank
    private String brand;
    private String type;
    @Positive
    @NotNull
    private BigDecimal dailyFee;
}
