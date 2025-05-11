package com.example.carsharingapp.dto.car;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateCarRequestDto {
    @NotNull
    @Positive
    private BigDecimal dailyFee;
    private int inventory;
}
