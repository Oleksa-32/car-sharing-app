package com.example.carsharingapp.strategy;

import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.RentalRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("RENTAL")
@RequiredArgsConstructor
public class RentalCheckoutStrategy implements CheckoutStrategy {
    private final RentalRepository rentalRepository;

    @Override
    public long calculateAmount(CreatePaymentRequestDto dto) {
        Rental rental = rentalRepository.findById(dto.getRentalId())
                .orElseThrow(() -> new EntityNotFoundException("Rental not found: "
                        + dto.getRentalId()));

        long days = ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate());
        BigDecimal dailyFee = rental.getCar().getDailyFee();
        BigDecimal total = dailyFee.multiply(BigDecimal.valueOf(days));
        return total.multiply(BigDecimal.valueOf(100)).longValue(); // in cents
    }
}
