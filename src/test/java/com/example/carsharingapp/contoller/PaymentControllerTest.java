package com.example.carsharingapp.contoller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.service.notification.NotificationService;
import com.example.carsharingapp.service.payment.PaymentService;
import com.example.carsharingapp.utils.TestDataUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentService paymentService;

    @TestConfiguration
    static class StubConfig {
        @Bean
        @Primary
        public PaymentService paymentService() {
            return Mockito.mock(PaymentService.class);
        }

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

        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            ScriptUtils.executeSqlScript(c,
                    new ClassPathResource("database/payments/delete-all-payments.sql"));
            ScriptUtils.executeSqlScript(c,
                    new ClassPathResource("database/payments/init-payments.sql"));
        }
    }

    @AfterAll
    static void cleanup(@Autowired DataSource ds) throws SQLException {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            ScriptUtils.executeSqlScript(c,
                    new ClassPathResource("database/payments/delete-all-payments.sql"));
        }
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /payments?user_id=1 → list of sessions")
    void getPaymentsByUser_ReturnsList() throws Exception {
        Long userId = 1L;
        Payment p = TestDataUtil.payment(TestDataUtil.rental());
        List<Payment> payments = List.of(p);

        when(paymentService.getPaymentsByUser(userId))
                .thenReturn(payments);

        mockMvc.perform(get("/payments")
                        .param("user_id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionId").value(p.getSessionId()))
                .andExpect(jsonPath("$[0].sessionUrl").value(p.getSessionUrl()));

        verify(paymentService).getPaymentsByUser(userId);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("POST /payments → create Stripe session")
    void createSession_ValidRequest_Success() throws Exception {
        var dto = TestDataUtil.createPaymentRequestDto();
        var resp = TestDataUtil.paymentResponseDto();

        when(paymentService.createSession(eq(dto), any()))
                .thenReturn(resp);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(resp.getSessionId()))
                .andExpect(jsonPath("$.sessionUrl").value(resp.getSessionUrl()));

        verify(paymentService).createSession(eq(dto), any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /payments/success?session_id=… → mark paid and notify")
    void handleSuccess_NotifiesOnPaid() throws Exception {
        String sessionId = "sess123";

        mockMvc.perform(get("/payments/success")
                        .param("session_id", sessionId))
                .andExpect(status().isOk());

        verify(paymentService).handleSuccess(sessionId);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /payments/cancel?session_id=… → mark canceled")
    void handleCancel_SetsCanceled() throws Exception {
        String sessionId = "sess123";

        mockMvc.perform(get("/payments/cancel")
                        .param("session_id", sessionId))
                .andExpect(status().isOk());

        verify(paymentService).handleCancel(sessionId);
    }
}
