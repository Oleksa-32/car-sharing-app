package com.example.carsharingapp.service.car;

import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CreateCarRequestDto;
import com.example.carsharingapp.dto.car.UpdateCarRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarDto save(CreateCarRequestDto requestDto);

    CarDto getCarById(Long id);

    Page<CarDto> findAll(Pageable pageable);

    CarDto updateCar(Long id, UpdateCarRequestDto updateCarRequestDto);

    void deleteCar(Long id);
}
