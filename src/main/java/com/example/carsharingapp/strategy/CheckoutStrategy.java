package com.example.carsharingapp.strategy;

import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;

public interface CheckoutStrategy {
    long calculateAmount(CreatePaymentRequestDto dto);
}
