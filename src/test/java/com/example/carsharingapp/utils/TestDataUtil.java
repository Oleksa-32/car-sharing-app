package com.example.carsharingapp.utils;

import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.dto.car.CreateCarRequestDto;
import com.example.carsharingapp.dto.car.UpdateCarRequestDto;
import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import com.example.carsharingapp.dto.payment.PaymentStatus;
import com.example.carsharingapp.dto.payment.PaymentType;
import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.dto.user.UpdateUserProfileRequestDto;
import com.example.carsharingapp.dto.user.UpdateUserRoleRequestDto;
import com.example.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.model.Car;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.model.Role;
import com.example.carsharingapp.model.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataUtil {
    private TestDataUtil() {

    }

    public static CarDto carDto(Long id, String model, String brand, String type,
                                int inventory, BigDecimal dailyFee) {
        return new CarDto()
                .setId(id)
                .setModel(model)
                .setBrand(brand)
                .setType(type)
                .setInventory(inventory)
                .setDailyFee(dailyFee);
    }

    public static CarDto carDto() {
        return carDto(2L, "Civic", "Honda", "SEDAN", 5, BigDecimal.valueOf(50).setScale(2));
    }

    public static CarDto mapToCarDto(CreateCarRequestDto req) {
        return new CarDto()
                .setModel(req.getModel())
                .setBrand(req.getBrand())
                .setType(req.getType())
                .setInventory(0)
                .setDailyFee(req.getDailyFee());
    }

    public static CarDto mapToCarDto(Long id,
                                     UpdateCarRequestDto req,
                                     CarDto before) {
        return new CarDto()
                .setId(id)
                .setModel(before.getModel())
                .setBrand(before.getBrand())
                .setType(before.getType())
                .setInventory(req.getInventory())
                .setDailyFee(req.getDailyFee());
    }

    public static List<CarDto> carDtoList() {
        List<CarDto> list = new ArrayList<>();
        list.add(carDto(1L, "Model S", "Tesla", "SEDAN", 3, BigDecimal.valueOf(100.0)));
        list.add(carDto(2L, "Civic", "Honda", "SEDAN", 5, BigDecimal.valueOf(50.0)));
        list.add(carDto(3L, "Golf", "Volkswagen", "HATCHBACK", 4, BigDecimal.valueOf(40.0)));
        return list;
    }

    public static Car car(Long id, String model, String brand, Car.Types type,
                          int inventory, BigDecimal dailyFee) {
        return new Car()
                .setId(id)
                .setModel(model)
                .setBrand(brand)
                .setType(type)
                .setInventory(inventory)
                .setDailyFee(dailyFee);
    }

    public static Car car() {
        return new Car()
                .setId(1L)
                .setBrand("BrandX")
                .setModel("ModelY")
                .setType(Car.Types.SEDAN)
                .setInventory(5)
                .setDailyFee(java.math.BigDecimal.valueOf(100));
    }

    public static List<Car> carList() {
        List<Car> list = new ArrayList<>();
        list.add(car(1L, "Model S", "Tesla", Car.Types.SEDAN, 3, BigDecimal.valueOf(100)));
        list.add(car(2L, "Civic", "Honda", Car.Types.SEDAN, 5, BigDecimal.valueOf(50)));
        list.add(car(3L, "Golf", "Volkswagen", Car.Types.HATCHBACK, 4, BigDecimal.valueOf(40)));
        return list;
    }

    public static CreateCarRequestDto createCarReq() {
        return new CreateCarRequestDto()
                .setModel("Civic")
                .setBrand("Honda")
                .setType("SEDAN")
                .setDailyFee(BigDecimal.valueOf(55.00));
    }

    public static UpdateCarRequestDto updateCarReq() {
        return new UpdateCarRequestDto()
                .setDailyFee(BigDecimal.valueOf(48.50))
                .setInventory(10);
    }

    public static CreateCarRequestDto createCarRequestDto() {
        return new CreateCarRequestDto()
                .setModel("Corolla")
                .setBrand("Toyota")
                .setType("Sedan")
                .setDailyFee(BigDecimal.valueOf(40));
    }

    public static CarDto mapToDtoFromCreate() {
        // same fields, id=null until saved
        return new CarDto()
                .setModel("Corolla")
                .setBrand("Toyota")
                .setType("Sedan")
                .setInventory(0)
                .setDailyFee(BigDecimal.valueOf(40));
    }

    public static UpdateCarRequestDto updateCarRequestDto() {
        return new UpdateCarRequestDto()
                .setDailyFee(BigDecimal.valueOf(45))
                .setInventory(7);
    }

    public static CarDto mapToDtoAfterUpdate(Long id) {
        return new CarDto()
                .setId(id)
                .setModel("Any")
                .setBrand("Any")
                .setType("Any")
                .setInventory(7)
                .setDailyFee(BigDecimal.valueOf(45));
    }

    public static CreateRentalRequestDto createRentalRequestDto() {
        LocalDateTime now = LocalDateTime.now();
        return new CreateRentalRequestDto()
                .setCarId(1L)
                .setUserId(2L)
                .setRentalDate(now)
                .setReturnDate(now.plusDays(3));
    }

    public static Rental rental(Long id, LocalDateTime rentalDate,
                                LocalDateTime returnDate,
                                Car car, User user,
                                LocalDateTime actualReturnDate) {
        return new Rental()
                .setId(id)
                .setRentalDate(rentalDate)
                .setReturnDate(returnDate)
                .setActualReturnDate(actualReturnDate)
                .setCar(car)
                .setUser(user);
    }

    public static Rental rental() {
        Rental r = new Rental();
        r.setId(1L);
        r.setRentalDate(java.time.LocalDateTime.now());
        r.setReturnDate(java.time.LocalDateTime.now().plusDays(1));
        r.setActualReturnDate(null);
        r.setCar(car());
        r.setUser(user());
        return r;
    }

    public static RentalDto rentalDto(Rental r) {
        return new RentalDto()
                .setId(r.getId())
                .setRentalDate(r.getRentalDate())
                .setReturnDate(r.getReturnDate())
                .setActualReturnDate(r.getActualReturnDate())
                .setCarId(r.getCar().getId())
                .setUserId(r.getUser().getId());
    }

    public static RentalDto rentalDto(Long id,
                                      LocalDateTime rentalDate,
                                      LocalDateTime returnDate,
                                      LocalDateTime actualReturnDate,
                                      Long carId, Long userId) {
        return new RentalDto()
                .setId(id)
                .setRentalDate(rentalDate)
                .setReturnDate(returnDate)
                .setActualReturnDate(actualReturnDate)
                .setCarId(carId)
                .setUserId(userId);
    }

    public static RentalDto rentalDto() {
        return rentalDto(1L,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 10, 10, 0),
                null, 1L, 1L);
    }

    public static List<RentalDto> seededRentalDtoList() {
        List<RentalDto> list = new ArrayList<>();
        list.add(rentalDto(1L,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 10, 10, 0),
                null, 1L, 1L));
        list.add(rentalDto(2L,
                LocalDateTime.of(2024, 1, 2, 10, 0),
                LocalDateTime.of(2024, 1, 12, 10, 0),
                LocalDateTime.of(2024, 1, 11, 10, 0), 2L, 1L));
        list.add(rentalDto(3L,
                LocalDateTime.of(2024, 1, 3, 10, 0),
                LocalDateTime.of(2024, 1, 13, 10, 0),
                null, 3L, 2L));
        return list;
    }

    public static List<RentalDto> getActiveRentalsForUser(Long userId) {
        return seededRentalDtoList().stream()
                .filter(r -> r.getUserId().equals(userId))
                .filter(r -> r.getActualReturnDate() == null)
                .toList();
    }

    public static List<RentalDto> getInactiveRentalsForUser(Long userId) {
        return seededRentalDtoList().stream()
                .filter(r -> r.getUserId().equals(userId))
                .filter(r -> r.getActualReturnDate() != null)
                .toList();
    }

    public static User user(Long id, String firstName, String lastName, String email) {
        return new User()
                .setId(id)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email);
    }

    public static User user(Long id, String email, String firstName, String lastName,
                            String password, Role... roles) {
        User u = new User()
                .setId(id)
                .setEmail(email)
                .setPassword(password)
                .setFirstName(firstName)
                .setLastName(lastName);
        u.getRoles().clear();
        for (Role r : roles) {
            u.getRoles().add(r);
        }
        return u;
    }

    public static User user() {
        return new User()
                .setId(1L)
                .setFirstName("John")
                .setLastName("Doe");
    }

    public static int inventory(int i) {
        return i;
    }

    public static BigDecimal dailyFee() {
        return BigDecimal.valueOf(100);
    }

    public static Role role(Long id, Role.Roles name) {
        return new Role().setId(id).setName(name);
    }

    public static UserRegistrationRequestDto createUserRegistrationRequestDto() {
        return new UserRegistrationRequestDto()
                .setEmail("test@domain.com")
                .setPassword("password123")
                .setRepeatPassword("password123")
                .setFirstName("Test")
                .setLastName("User");
    }

    public static UserResponseDto mapToUserResponseDto(User user) {
        return new UserResponseDto()
                .setId(user.getId())
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName());
    }

    public static UpdateUserRoleRequestDto createUpdateUserRoleRequestDto() {
        return new UpdateUserRoleRequestDto().setRole(Role.Roles.ROLE_MANAGER);
    }

    public static UpdateUserProfileRequestDto createUpdateUserProfileRequestDto() {
        return new UpdateUserProfileRequestDto()
                .setFirstName("NewFirst")
                .setLastName("NewLast");
    }

    public static void applyProfileUpdate(UpdateUserProfileRequestDto dto, User user) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
    }

    public static CreatePaymentRequestDto createPaymentRequestDto() {
        CreatePaymentRequestDto dto = new CreatePaymentRequestDto();
        dto.setRentalId(1L);
        dto.setType(PaymentType.RENTAL);
        return dto;
    }

    public static Payment payment(Rental rental) {
        Payment p = new Payment();
        p.setId(1L);
        p.setRental(rental);
        p.setSessionId("sess123");
        p.setSessionUrl("http://url");
        p.setAmount(5000L);
        p.setCurrency("usd");
        p.setType(PaymentType.RENTAL);
        p.setStatus(PaymentStatus.OPEN);
        return p;
    }

    public static PaymentResponseDto paymentResponseDto() {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setSessionId("sess123");
        dto.setSessionUrl("http://url");
        return dto;
    }

    public static Rental overdueRental(Long id) {
        Car car = new Car()
                .setId(1L)
                .setModel("Model S")
                .setBrand("Tesla")
                .setType(Car.Types.SEDAN)
                .setInventory(2)
                .setDailyFee(BigDecimal.valueOf(100));

        User user = new User()
                .setId(1L)
                .setEmail("user@example.com")
                .setPassword("encodedpassword")
                .setFirstName("John")
                .setLastName("Doe");

        Rental rental = new Rental();
        rental.setId(id);
        rental.setCar(car);
        rental.setUser(user);
        rental.setRentalDate(LocalDateTime.now().minusDays(10));
        rental.setReturnDate(LocalDateTime.now().minusDays(3));
        rental.setActualReturnDate(null);

        return rental;
    }
}
