package com.example.carsharingapp.dto.car;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateCarRequestDto {
    @NotBlank
    private BigDecimal dailyFee;
}
