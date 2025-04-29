package com.example.carsharingapp.contoller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.example.carsharingapp.dto.rental.RentalDto;
import com.example.carsharingapp.service.notification.NotificationService;
import com.example.carsharingapp.utils.TestDataUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RentalControllerTest {
    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;

    @TestConfiguration
    static class StubNotificationConfig {
        @Bean
        @Primary
        public NotificationService notificationService() {
            return Mockito.mock(NotificationService.class);
        }
    }

    @BeforeEach
    void setUp(@Autowired DataSource ds,
               @Autowired WebApplicationContext ctx) throws SQLException {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .apply(springSecurity())
                .build();
        tearDown(ds);
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    c, new ClassPathResource("database/cars/init-cars.sql"));
            ScriptUtils.executeSqlScript(
                    c, new ClassPathResource("database/rentals/init-rentals.sql"));
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource ds) {
        tearDown(ds);
    }

    @SneakyThrows
    private static void tearDown(DataSource ds) {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            ScriptUtils.executeSqlScript(
                    c, new ClassPathResource("database/rentals/delete-all-rentals.sql"));
            ScriptUtils.executeSqlScript(
                    c, new ClassPathResource("database/cars/delete-all-cars.sql"));
        }
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("POST /rentals → 201 Created")
    void createRental_ValidRequest_Success() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        var req = new CreateRentalRequestDto()
                .setCarId(1L)
                .setUserId(1L)
                .setRentalDate(now)
                .setReturnDate(now.plusDays(3));

        mockMvc.perform(post("/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carId").value(req.getCarId()))
                .andExpect(jsonPath("$.userId").value(req.getUserId()))
                .andExpect(jsonPath("$.actualReturnDate").isEmpty());

        verify(notificationService).sendNotification(anyString());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("GET /rentals/{id} → single rental")
    void getRentalById_ReturnsCorrect() throws Exception {
        RentalDto expected = TestDataUtil.rentalDto();

        MvcResult res = mockMvc.perform(get("/rentals/" + expected.getId()))
                .andExpect(status().isOk())
                .andReturn();

        RentalDto actual = objectMapper.readValue(res.getResponse().getContentAsByteArray(),
                RentalDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("GET /rentals?user_id=1&is_active=true → active rentals")
    void getByUserAndActive_ActiveRentals_Success() throws Exception {
        Long userId = 1L;
        List<RentalDto> expected = TestDataUtil.getActiveRentalsForUser(userId);

        MvcResult res = mockMvc.perform(get("/rentals")
                        .param("user_id", userId.toString())
                        .param("is_active", "true"))
                .andExpect(status().isOk())
                .andReturn();

        RentalDto[] actual = objectMapper.readValue(res.getResponse().getContentAsByteArray(),
                RentalDto[].class);
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("GET /rentals?user_id=1&is_active=false → inactive rentals")
    void getByUserAndActive_InactiveRentals_Success() throws Exception {
        Long userId = 1L;
        List<RentalDto> expected = TestDataUtil.getInactiveRentalsForUser(userId);

        MvcResult res = mockMvc.perform(get("/rentals")
                        .param("user_id", userId.toString())
                        .param("is_active", "false"))
                .andExpect(status().isOk())
                .andReturn();

        RentalDto[] actual = objectMapper.readValue(res.getResponse().getContentAsByteArray(),
                RentalDto[].class);
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("POST /rentals/1/return → return rental")
    void returnRental_Success() throws Exception {
        Long rentalId = 1L;

        MvcResult res = mockMvc.perform(post("/rentals/" + rentalId + "/return"))
                .andExpect(status().isOk())
                .andReturn();

        RentalDto actual = objectMapper.readValue(res.getResponse().getContentAsByteArray(),
                RentalDto.class);
        assertThat(actual.getId()).isEqualTo(rentalId);
        assertThat(actual.getActualReturnDate()).isNotNull();
    }
}
