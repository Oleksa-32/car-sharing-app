package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.service.rental.RentalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("rentals")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalDto save(@RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.save(requestDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    public RentalDto getRentalById(@PathVariable Long id) {
        return rentalService.getRentalById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','CUSTOMER')")
    public List<RentalDto> getByUserAndActive(
            @RequestParam("user_id") Long userId,
            @RequestParam("is_active") boolean isActive
    ) {
        return rentalService.getRentalsByUserAndActive(userId, isActive);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('MANAGER','CUSTOMER')")
    public RentalDto returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }
}
