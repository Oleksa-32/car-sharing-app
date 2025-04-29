package com.example.carsharingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CreateCarRequestDto;
import com.example.carsharingapp.dto.car.UpdateCarRequestDto;
import com.example.carsharingapp.mapper.CarMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.service.car.CarServiceImpl;
import com.example.carsharingapp.utils.TestDataUtil;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock private CarRepository carRepository;
    @Mock private CarMapper carMapper;
    @InjectMocks private CarServiceImpl carService;

    private List<Car> carList;
    private List<CarDto> dtoList;
    private Pageable pageRequest;

    @BeforeEach
    void setUp() {
        carList = TestDataUtil.carList();
        dtoList = TestDataUtil.carDtoList();
        pageRequest = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("findAll returns mapped page of CarDto")
    void findAll_returnsDtoPage() {
        Page<Car> carPage = new PageImpl<>(carList, pageRequest, carList.size());
        when(carRepository.findAll(pageRequest)).thenReturn(carPage);
        when(carMapper.toDto(carList.get(0))).thenReturn(dtoList.get(0));
        when(carMapper.toDto(carList.get(1))).thenReturn(dtoList.get(1));
        when(carMapper.toDto(carList.get(2))).thenReturn(dtoList.get(2));

        Page<CarDto> result = carService.findAll(pageRequest);

        assertThat(result.getContent())
                .usingRecursiveComparison()
                .isEqualTo(dtoList);
        verify(carRepository).findAll(pageRequest);
    }

    @Test
    @DisplayName("getCarById with valid id returns CarDto")
    void getCarById_validId_returnsDto() {
        Car car = carList.get(0);
        CarDto dto = dtoList.get(0);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(dto);

        CarDto result = carService.getCarById(1L);

        assertThat(result).isEqualTo(dto);
        verify(carRepository).findById(1L);
    }

    @Test
    @DisplayName("getCarById with invalid id throws EntityNotFoundException")
    void getCarById_invalidId_throws() {
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.getCarById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Car with id 99 not found");
        verify(carRepository).findById(99L);
    }

    @Test
    @DisplayName("save maps request, saves and returns dto")
    void save_validRequest_returnsDto() {
        CreateCarRequestDto req = TestDataUtil.createCarRequestDto();
        Car carModel = TestDataUtil.car(
                null,
                "Corolla",
                "Toyota",
                Car.Types.SEDAN,
                0,
                BigDecimal.valueOf(40));
        Car saved = TestDataUtil.car(
                3L,
                "Corolla",
                "Toyota",
                Car.Types.SEDAN,
                0,
                BigDecimal.valueOf(40));
        CarDto dto = TestDataUtil.mapToDtoFromCreate().setId(3L);

        when(carMapper.toModel(req)).thenReturn(carModel);
        when(carRepository.save(carModel)).thenReturn(saved);
        when(carMapper.toDto(saved)).thenReturn(dto);

        CarDto result = carService.save(req);

        assertThat(result).isEqualTo(dto);
        verify(carMapper).toModel(req);
        verify(carRepository).save(carModel);
        verify(carMapper).toDto(saved);
    }

    @Test
    @DisplayName("updateCar with valid id updates and returns dto")
    void updateCar_validId_returnsDto() {
        UpdateCarRequestDto req = TestDataUtil.updateCarRequestDto();
        Car existing = TestDataUtil.car(
                5L,
                "Camry",
                "Toyota",
                Car.Types.SEDAN,
                2,
                BigDecimal.valueOf(60));
        Car updated = TestDataUtil.car(
                5L,
                "Camry",
                "Toyota",
                Car.Types.SEDAN,
                7,
                BigDecimal.valueOf(45));
        CarDto dto = TestDataUtil.mapToDtoAfterUpdate(5L);

        when(carRepository.findById(5L)).thenReturn(Optional.of(existing));
        doAnswer(inv -> {
            UpdateCarRequestDto r = inv.getArgument(0);
            Car c = inv.getArgument(1);
            c.setDailyFee(r.getDailyFee());
            c.setInventory(r.getInventory());
            return null;
        }).when(carMapper).updateCarFromDto(req, existing);
        when(carRepository.save(existing)).thenReturn(updated);
        when(carMapper.toDto(updated)).thenReturn(dto);

        CarDto result = carService.updateCar(5L, req);

        assertThat(result).isEqualTo(dto);
        verify(carRepository).findById(5L);
        verify(carMapper).updateCarFromDto(req, existing);
        verify(carRepository).save(existing);
        verify(carMapper).toDto(updated);
    }

    @Test
    @DisplayName("updateCar with invalid id throws EntityNotFoundException")
    void updateCar_invalidId_throws() {
        when(carRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.updateCar(7L, TestDataUtil.updateCarRequestDto()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Car with id 7not found");
        verify(carRepository).findById(7L);
    }

    @Test
    @DisplayName("deleteCar with existing id calls repository")
    void deleteCar_existing_callsDelete() {
        when(carRepository.existsById(2L)).thenReturn(true);

        carService.deleteCar(2L);

        verify(carRepository).existsById(2L);
        verify(carRepository).deleteById(2L);
    }

    @Test
    @DisplayName("deleteCar with non‐existent id throws RuntimeException")
    void deleteCar_nonexistent_throws() {
        when(carRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> carService.deleteCar(9L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Car with id9 not found");
        verify(carRepository).existsById(9L);
    }
}
