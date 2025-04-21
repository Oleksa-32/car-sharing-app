package com.example.carsharingapp.dto.rental;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateRentalRequestDto {
    @NotNull
    private Long carId;
    @NotNull
    private Long userId;
    @NotNull
    private LocalDateTime rentalDate;

    @NotNull
    private LocalDateTime returnDate;
}
