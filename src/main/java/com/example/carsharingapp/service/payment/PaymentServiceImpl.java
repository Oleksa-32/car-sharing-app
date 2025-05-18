package com.example.carsharingapp.service.payment;

import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import com.example.carsharingapp.dto.payment.PaymentStatus;
import com.example.carsharingapp.dto.payment.PaymentType;
import com.example.carsharingapp.mapper.PaymentMapper;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.notification.NotificationService;
import com.example.carsharingapp.strategy.CheckoutStrategy;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;
    private final Map<String, CheckoutStrategy> strategies;
    private final NotificationService notificationService;

    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByRentalUserId(userId);
    }

    @Transactional
    public PaymentResponseDto createSession(CreatePaymentRequestDto dto,
                                            UriComponentsBuilder uriBuilder)
            throws StripeException {
        long amount = strategies.get(dto.getType().name()).calculateAmount(dto);
        String successUrl = uriBuilder.path("/payments/success")
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .build().toUriString();
        String cancelUrl = uriBuilder.path("/payments/cancel")
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .build().toUriString();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData
                                                                .ProductData.builder()
                                                                .setName(dto.getType()
                                                                        == PaymentType.FINE
                                                                        ? "Rental Fine"
                                                                        : "Rental Fee")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        Rental rental = rentalRepository.findById(dto.getRentalId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rental not found: " + dto.getRentalId()));

        Payment payment = new Payment();
        payment.setRental(rental);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());
        payment.setAmount(amount);
        payment.setCurrency("usd");
        payment.setType(dto.getType());
        payment.setStatus(PaymentStatus.OPEN);

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    public void handleSuccess(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        if ("paid".equals(session.getPaymentStatus())) {
            Payment payment = paymentRepository.findBySessionId(sessionId)
                    .orElseThrow();
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            Rental rental = payment.getRental();
            StringBuilder msg = new StringBuilder();
            msg.append("💰 *Payment Received*\n")
                    .append("• *Payment ID:* ").append(payment.getId()).append("\n")
                    .append("• *Rental ID:* ").append(rental.getId()).append("\n")
                    .append("• *Type:* ").append(payment.getType()).append("\n")
                    .append("• *Amount:* ").append(payment.getAmount() / 100.0)
                    .append(" ").append(payment.getCurrency().toUpperCase()).append("\n")
                    .append("• *User:* ")
                    .append(rental.getUser().getFirstName())
                    .append(" ").append(rental.getUser().getLastName()).append("\n")
                    .append("• *Car:* ")
                    .append(rental.getCar().getBrand())
                    .append(" ").append(rental.getCar().getModel());
            notificationService.sendNotification(msg.toString());
        }
    }

    public void handleCancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow();
        payment.setStatus(PaymentStatus.CANCELED);
        paymentRepository.save(payment);
    }
}
