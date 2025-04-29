package com.example.carsharingapp.contoller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingapp.dto.car.CarDto;
import com.example.carsharingapp.utils.TestDataUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarControllerTest {
    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                    c, new ClassPathResource("database/cars/delete-all-cars.sql"));
        }
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("POST /cars → 201 Created")
    void createCar_ValidRequest_Success() throws Exception {
        var req = TestDataUtil.createCarReq();
        var expected = TestDataUtil.mapToCarDto(req);
        String json = objectMapper.writeValueAsString(req);

        MvcResult res = mockMvc.perform(post("/cars")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        CarDto actual = objectMapper.readValue(res.getResponse().getContentAsByteArray(),
                CarDto.class);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("GET /cars → list all")
    void getAllCars_ReturnsAll() throws Exception {
        List<CarDto> expected = TestDataUtil.carDtoList();

        MvcResult res = mockMvc.perform(get("/cars"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = objectMapper.readTree(res.getResponse().getContentAsByteArray())
                .get("content");

        CarDto[] actual = objectMapper.treeToValue(content, CarDto[].class);
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    @DisplayName("GET /cars/{id} → single car")
    void getCarById_ReturnsCorrect() throws Exception {
        CarDto expected = TestDataUtil.carDto();

        MvcResult res = mockMvc.perform(get("/cars/" + expected.getId()))
                .andExpect(status().isOk())
                .andReturn();

        CarDto actual = objectMapper.readValue(res.getResponse().getContentAsByteArray(),
                CarDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("PUT /cars/{id} → update")
    void updateCar_ValidRequest_Success() throws Exception {
        Long id = 1L;
        var updateReq = TestDataUtil.updateCarReq();
        var before = TestDataUtil.carDtoList().get(0);
        var expected = TestDataUtil.mapToCarDto(id, updateReq, before);

        MvcResult res = mockMvc.perform(put("/cars/" + id)
                        .content(objectMapper.writeValueAsString(updateReq))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarDto actual = objectMapper.readValue(res.getResponse().getContentAsByteArray(),
                CarDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("DELETE /cars/{id} → 204 No Content")
    void deleteCar_ExistingId_NoContent() throws Exception {
        mockMvc.perform(delete("/cars/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    @DisplayName("POST /cars with negative fee → 400")
    void createCar_NegativeFee_BadRequest() throws Exception {
        var dto = TestDataUtil.createCarReq();
        dto.setDailyFee(BigDecimal.valueOf(-5));
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/cars").content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
