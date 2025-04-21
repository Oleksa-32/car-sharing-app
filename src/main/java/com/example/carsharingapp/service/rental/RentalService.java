package com.example.carsharingapp.service.rental;

import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalDto;
import java.util.List;

public interface RentalService {
    RentalDto save(CreateRentalRequestDto requestDto);

    RentalDto getRentalById(Long id);

    List<RentalDto> getRentalsByUserAndActive(Long userId, boolean isActive);

    RentalDto returnRental(Long rentalId);
}
