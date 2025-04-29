package com.example.carsharingapp.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequestDto {
    @NotNull
    private Long rentalId;

    @NotNull
    private PaymentType type;
}
