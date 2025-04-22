package com.example.carsharingapp.service.rental;

import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.mapper.RentalMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;

    @Override
    public RentalDto save(CreateRentalRequestDto requestDto) {

        Car car = carRepository.findById(requestDto.getCarId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Car not found: " + requestDto.getCarId()));
        if (car.getInventory() <= 0) {
            throw new IllegalStateException("No inventory for car " + requestDto.getCarId());
        }
        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found: " + requestDto.getUserId()));

        Rental rental = rentalMapper.toModel(requestDto);

        rental.setRentalDate(requestDto.getRentalDate());
        rental.setReturnDate(requestDto.getReturnDate());

        rental.setCar(car);
        rental.setUser(user);
        rental.setActualReturnDate(null);

        Rental saved = rentalRepository.save(rental);
        return rentalMapper.toDto(saved);
    }

    @Override
    public RentalDto getRentalById(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Rental with id " + id + " not found"));
        return rentalMapper.toDto(rental);
    }

    @Override
    public List<RentalDto> getRentalsByUserAndActive(Long userId, boolean isActive) {
        List<Rental> rentals = isActive
                ? rentalRepository.findByUserIdAndActualReturnDateIsNull(userId)
                : rentalRepository.findByUserIdAndActualReturnDateIsNotNull(userId);
        return rentals.stream()
                .map(rentalMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RentalDto returnRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found: " + rentalId));

        if (rental.getActualReturnDate() != null) {
            throw new IllegalStateException("Rental already returned: " + rentalId);
        }
        rental.setActualReturnDate(LocalDateTime.now());

        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);

        Rental saved = rentalRepository.save(rental);
        return rentalMapper.toDto(saved);
    }
}
