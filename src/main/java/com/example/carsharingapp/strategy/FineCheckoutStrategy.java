package com.example.carsharingapp.strategy;

import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.RentalRepository;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("FINE")
@RequiredArgsConstructor
public class FineCheckoutStrategy implements CheckoutStrategy {
    private final RentalRepository rentalRepository;
    private final double multiplier = Double.parseDouble(Dotenv.load().get("FINE_MULTIPLIER"));

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
