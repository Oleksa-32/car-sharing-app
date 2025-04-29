package com.example.carsharingapp.contoller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingapp.dto.user.UpdateUserProfileRequestDto;
import com.example.carsharingapp.dto.user.UpdateUserRoleRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.service.user.UserService;
import com.example.carsharingapp.utils.TestDataUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    private static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @TestConfiguration
    static class StubUserConfig {
        @Bean
        @Primary
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @BeforeEach
    void setUp(@Autowired WebApplicationContext context) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("PUT /users/{id}/role → 200 OK")
    @SneakyThrows
    void updateRole_Manager_Success() {
        Long userId = 42L;
        UpdateUserRoleRequestDto dto = TestDataUtil.createUpdateUserRoleRequestDto();
        UserResponseDto expected = TestDataUtil.mapToUserResponseDto(
                TestDataUtil.user(userId, dto.getRole().name(), "First", "Last"));

        when(userService.updateRole(eq(userId), eq(dto)))
                .thenReturn(expected);

        mockMvc.perform(put("/users/{id}/role", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expected.getId()))
                .andExpect(jsonPath("$.email").value(expected.getEmail()))
                .andExpect(jsonPath("$.firstName").value(expected.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(expected.getLastName()));

        verify(userService).updateRole(eq(userId), eq(dto));
    }

    @Test
    @WithMockUser(username = "alice")
    @DisplayName("GET /users/me → 200 OK")
    @SneakyThrows
    void getMe_Authenticated_Success() {
        String username = "alice";
        UserResponseDto expected = TestDataUtil.mapToUserResponseDto(
                TestDataUtil.user(1L, "Alice", "Smith", username + "@example.com"));

        when(userService.getProfile(eq(username)))
                .thenReturn(expected);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expected.getId()))
                .andExpect(jsonPath("$.email").value(expected.getEmail()))
                .andExpect(jsonPath("$.firstName").value(expected.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(expected.getLastName()));

        verify(userService).getProfile(eq(username));
    }

    @Test
    @WithMockUser(username = "bob")
    @DisplayName("PUT /users/me → 200 OK")
    @SneakyThrows
    void updateMe_Authenticated_Success() {
        String username = "bob";
        UpdateUserProfileRequestDto dto = TestDataUtil.createUpdateUserProfileRequestDto();

        var userModel = TestDataUtil.user(1L, username + "@example.com", dto.getFirstName(),
                dto.getLastName());
        UserResponseDto expected = TestDataUtil.mapToUserResponseDto(userModel);

        when(userService.updateProfile(eq(username), eq(dto)))
                .thenReturn(expected);

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expected.getId()))
                .andExpect(jsonPath("$.email").value(expected.getEmail()))
                .andExpect(jsonPath("$.firstName").value(expected.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(expected.getLastName()));

        verify(userService).updateProfile(eq(username), eq(dto));
    }
}

