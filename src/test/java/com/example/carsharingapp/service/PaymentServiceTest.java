package com.example.carsharingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import com.example.carsharingapp.dto.payment.PaymentStatus;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.notification.NotificationService;
import com.example.carsharingapp.service.payment.PaymentServiceImpl;
import com.example.carsharingapp.strategy.CheckoutStrategy;
import com.example.carsharingapp.utils.TestDataUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private Map<String, CheckoutStrategy> strategies;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UriComponentsBuilder uriBuilder;

    @BeforeEach
    void setUp() {
        uriBuilder = UriComponentsBuilder.fromUriString("http://localhost");
    }

//    @Test
//    @DisplayName("getPaymentsByUser returns list of payments from repository")
//    void getPaymentsByUser_returnsList() {
//        Long userId = 1L;
//        Rental rental = TestDataUtil.rental();
//        Payment payment = TestDataUtil.payment(rental);
//        List<Payment> payments = List.of(payment);
//
//        when(paymentRepository.findByRentalUserId(userId)).thenReturn(payments);
//
//        List<Payment> result = paymentService.getPaymentsByUser(userId);
//
//        assertThat(result).isEqualTo(payments);
//        verify(paymentRepository).findByRentalUserId(userId);
//    }

    @Test
    @DisplayName("createSession builds Stripe session, persists payment and returns dto")
    void createSession_success() throws StripeException {
        CreatePaymentRequestDto dto = TestDataUtil.createPaymentRequestDto();
        Rental rental = TestDataUtil.rental();
        when(rentalRepository.findById(dto.getRentalId())).thenReturn(Optional.of(rental));

        CheckoutStrategy strategy = mock(CheckoutStrategy.class);
        when(strategies.get(dto.getType().name())).thenReturn(strategy);
        long amount = 5000L;
        when(strategy.calculateAmount(dto)).thenReturn(amount);

        Session stripeSession = mock(Session.class);
        when(stripeSession.getId()).thenReturn("sess123");
        when(stripeSession.getUrl()).thenReturn("http://url");

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            // ✅ This avoids the ambiguity by specifying the type explicitly
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(stripeSession);

            Payment savedPayment = TestDataUtil.payment(rental);
            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

            PaymentResponseDto expectedDto = TestDataUtil.paymentResponseDto();
            when(paymentMapper.toDto(savedPayment)).thenReturn(expectedDto);

            PaymentResponseDto actual = paymentService.createSession(dto, uriBuilder);

            assertThat(actual).isEqualTo(expectedDto);
            verify(strategy).calculateAmount(dto);
            verify(paymentRepository).save(any(Payment.class));
            verify(paymentMapper).toDto(savedPayment);
        }
    }

    @Test
    @DisplayName("handleSuccess with paid session updates status and sends notification")
    void handleSuccess_paid_updatesStatusAndSendsNotification() throws StripeException {
        String sessionId = "sess123";
        Session stripeSession = mock(Session.class);
        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.retrieve(sessionId)).thenReturn(stripeSession);
            when(stripeSession.getPaymentStatus()).thenReturn("paid");

            Rental rental = TestDataUtil.rental();
            Payment payment = TestDataUtil.payment(rental);
            when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

            paymentService.handleSuccess(sessionId);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
            verify(paymentRepository).save(payment);
            verify(notificationService).sendNotification(contains("Payment Received"));
        }
    }

    @Test
    @DisplayName("handleCancel sets payment status to CANCELED and persists")
    void handleCancel_setsStatusCanceled() {
        String sessionId = "sess123";
        Rental rental = TestDataUtil.rental();
        Payment payment = TestDataUtil.payment(rental);
        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        paymentService.handleCancel(sessionId);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        verify(paymentRepository).save(payment);
    }
}
