package com.example.carsharingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.mapper.RentalMapper;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.CarRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.repository.UserRepository;
import com.example.carsharingapp.service.notification.NotificationService;
import com.example.carsharingapp.service.rental.RentalServiceImpl;
import com.example.carsharingapp.utils.TestDataUtil;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private RentalServiceImpl rentalService;

    private CreateRentalRequestDto requestDto;
    private Car car;
    private User user;
    private Rental rental;
    private RentalDto rentalDto;

    @BeforeEach
    void setUp() {
        requestDto = TestDataUtil.createRentalRequestDto();
        car = TestDataUtil.car(1L, "ModelX", "BrandY", Car.Types.SEDAN, TestDataUtil.inventory(5),
                TestDataUtil.dailyFee());
        user = TestDataUtil.user(2L, "John", "Doe", "john.doe@example.com");
        rental = TestDataUtil.rental(3L, requestDto.getRentalDate(), requestDto.getReturnDate(),
                car, user, null);
        rentalDto = TestDataUtil.rentalDto(rental);
    }

    @Test
    @DisplayName("save with valid request returns expected RentalDto and updates inventory")
    void save_withValidRequest_returnsExpectedDto() {
        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.of(car));
        when(userRepository.findById(requestDto.getUserId())).thenReturn(Optional.of(user));
        when(rentalMapper.toModel(requestDto)).thenReturn(rental);
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        RentalDto result = rentalService.save(requestDto);

        assertThat(result).isEqualTo(rentalDto);
        assertThat(car.getInventory()).isEqualTo(4);
        verify(notificationService).sendNotification(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("save with missing car throws EntityNotFoundException")
    void save_withMissingCar_throwsException() {
        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> rentalService.save(requestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Car not found");
    }

    @Test
    @DisplayName("save with no inventory throws IllegalStateException")
    void save_withNoInventory_throwsException() {
        car.setInventory(0);
        when(carRepository.findById(requestDto.getCarId())).thenReturn(Optional.of(car));
        assertThatThrownBy(() -> rentalService.save(requestDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No inventory");
    }

    @Test
    @DisplayName("getRentalById returns expected RentalDto")
    void getRentalById_returnsExpectedDto() {
        when(rentalRepository.findById(3L)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        RentalDto result = rentalService.getRentalById(3L);
        assertThat(result).isEqualTo(rentalDto);
    }

    @Test
    @DisplayName("getRentalById with invalid id throws EntityNotFoundException")
    void getRentalById_withInvalidId_throwsException() {
        when(rentalRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> rentalService.getRentalById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Rental with id");
    }

    @Test
    @DisplayName("getRentalsByUserAndActive returns active rentals when isActive=true")
    void getRentalsByUserAndActive_active_returnsList() {
        when(rentalRepository.findByUserIdAndActualReturnDateIsNull(2L))
                .thenReturn(List.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        List<RentalDto> result = rentalService.getRentalsByUserAndActive(2L, true);
        assertThat(result).containsExactly(rentalDto);
    }

    @Test
    @DisplayName("returnRental with valid id processes return and updates inventory")
    void returnRental_withValidId_returnsDto() {
        when(rentalRepository.findById(3L)).thenReturn(Optional.of(rental));
        doAnswer(invocation -> {
            rental.setActualReturnDate(LocalDateTime.now());
            return rental;
        }).when(rentalRepository).save(rental);
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        RentalDto result = rentalService.returnRental(3L);

        assertThat(result).isEqualTo(rentalDto);
        assertThat(car.getInventory()).isEqualTo(6);
        verify(notificationService).sendNotification(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("returnRental when already returned throws IllegalStateException")
    void returnRental_alreadyReturned_throwsException() {
        rental.setActualReturnDate(LocalDateTime.now());
        when(rentalRepository.findById(3L)).thenReturn(Optional.of(rental));

        assertThatThrownBy(() -> rentalService.returnRental(3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already returned");
    }

    @Test
    @DisplayName("returnRental with invalid id throws EntityNotFoundException")
    void returnRental_invalidId_throwsException() {
        when(rentalRepository.findById(50L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> rentalService.returnRental(50L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
