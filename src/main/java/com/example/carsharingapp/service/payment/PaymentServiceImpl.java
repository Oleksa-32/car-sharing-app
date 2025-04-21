package com.example.carsharingapp.service.payment;

import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import com.example.carsharingapp.dto.payment.PaymentStatus;
import com.example.carsharingapp.dto.payment.PaymentType;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.PaymentRepository;
import com.example.carsharingapp.repository.RentalRepository;
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
    private final RentalRepository rentalRepository;
    private final Map<String, CheckoutStrategy> strategies;

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByRentalUserId(userId);
    }

    @Transactional
    public PaymentResponseDto createSession(CreatePaymentRequestDto dto,
                                            UriComponentsBuilder uriBuilder)
            throws StripeException {
        //calculate total
        long amount = strategies.get(dto.getType().name()).calculateAmount(dto);

        //build redirect URLs
        String successUrl = uriBuilder.path("/payments/success")
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .build().toUriString();
        String cancelUrl = uriBuilder.path("/payments/cancel")
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .build().toUriString();

        //create Stripe Checkout session
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

        //persist Payment record
        Payment p = new Payment();
        p.setRental(rental);
        p.setSessionId(session.getId());
        p.setSessionUrl(session.getUrl());
        p.setAmount(amount);
        p.setCurrency("usd");
        p.setType(dto.getType());
        p.setStatus(PaymentStatus.OPEN);
        paymentRepository.save(p);

        //return to client
        PaymentResponseDto resp = new PaymentResponseDto();
        resp.setSessionId(session.getId());
        resp.setSessionUrl(session.getUrl());
        return resp;
    }

    @Transactional
    public void handleSuccess(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        if ("paid".equals(session.getPaymentStatus())) {
            Payment p = paymentRepository.findBySessionId(sessionId)
                    .orElseThrow();
            p.setStatus(PaymentStatus.PAID);
            paymentRepository.save(p);
        }
    }

    @Transactional
    public void handleCancel(String sessionId) {
        Payment p = paymentRepository.findBySessionId(sessionId)
                .orElseThrow();
        p.setStatus(PaymentStatus.CANCELED);
        paymentRepository.save(p);
    }
}
