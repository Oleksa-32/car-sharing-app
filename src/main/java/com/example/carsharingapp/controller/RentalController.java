package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.service.rental.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Rental management", description = "Endpoints for managing rentals")
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create rental", description = "Create a new rental (manager only)")
    public RentalDto save(@RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.save(requestDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @Operation(summary = "Get rental by ID", description = "Retrieve rental details by rental ID")
    public RentalDto getRentalById(@PathVariable Long id) {
        return rentalService.getRentalById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','CUSTOMER')")
    @Operation(summary = "Get user rentals", description = "Retrieve rentals by user"
            + " ID and active status")
    public List<RentalDto> getByUserAndActive(
            @RequestParam("user_id") Long userId,
            @RequestParam("is_active") boolean isActive
    ) {
        return rentalService.getRentalsByUserAndActive(userId, isActive);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('MANAGER','CUSTOMER')")
    @Operation(summary = "Return rental", description = "Mark a rental as returned")
    public RentalDto returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }
}
