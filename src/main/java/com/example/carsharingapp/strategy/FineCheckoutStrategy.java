package com.example.carsharingapp.strategy;

import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.RentalRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("FINE")
@RequiredArgsConstructor
public class FineCheckoutStrategy implements CheckoutStrategy {
    private final RentalRepository rentalRepository;
    @Value("${FINE_MULTIPLIER}")
    private double multiplier;

    @Override
    public long calculateAmount(CreatePaymentRequestDto dto) {
        Rental rental = rentalRepository.findById(dto.getRentalId())
                .orElseThrow(() -> new EntityNotFoundException("Rental not found: "
                        + dto.getRentalId()));

        long overdueDays = ChronoUnit.DAYS.between(rental.getReturnDate(), LocalDateTime.now());
        if (overdueDays < 1) {
            overdueDays = 1; // at least one day fine
        }
        BigDecimal dailyFee = rental.getCar().getDailyFee();
        BigDecimal total = dailyFee
                .multiply(BigDecimal.valueOf(overdueDays))
                .multiply(BigDecimal.valueOf(multiplier));

        return total.multiply(BigDecimal.valueOf(100)).longValue();
    }
}
