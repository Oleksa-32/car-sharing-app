package com.example.carsharingapp.dto.rental;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RentalDto {
    private Long id;
    private LocalDateTime rentalDate;
    private LocalDateTime returnDate;
    private LocalDateTime actualReturnDate;
    private Long carId;
    private Long userId;
}
