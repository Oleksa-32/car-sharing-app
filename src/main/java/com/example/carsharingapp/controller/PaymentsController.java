package com.example.carsharingapp.controller;

import com.example.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import com.example.carsharingapp.model.Payment;
import com.example.carsharingapp.service.payment.PaymentService;
import com.stripe.exception.StripeException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentsController {
    private final PaymentService paymentService;

    @GetMapping
    public List<Payment> getPayments(@RequestParam("user_id") Long userId) {
        return paymentService.getPaymentsByUser(userId);
    }

    @PostMapping
    public PaymentResponseDto createPayment(
            @RequestBody CreatePaymentRequestDto dto,
            UriComponentsBuilder uriBuilder
    ) throws StripeException {
        return paymentService.createSession(dto, uriBuilder);
    }

    @GetMapping("/success")
    public String success(@RequestParam("session_id") String sessionId)
            throws StripeException {
        paymentService.handleSuccess(sessionId);
        return "Payment successful for session " + sessionId;
    }

    @GetMapping("/cancel")
    public String cancel(@RequestParam("session_id") String sessionId) {
        paymentService.handleCancel(sessionId);
        return "Payment cancelled; you have 24 hrs to retry using the same link.";
    }
}
