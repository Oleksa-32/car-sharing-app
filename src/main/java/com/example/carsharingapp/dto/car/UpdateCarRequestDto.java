package com.example.carsharingapp.dto.car;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateCarRequestDto {
    @NotNull
    private BigDecimal dailyFee;
    @NotNull
    private int inventory;
}
