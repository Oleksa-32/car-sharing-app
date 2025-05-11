package com.example.carsharingapp.repository;

import com.example.carsharingapp.dto.payment.PaymentResponseDto;
import com.example.carsharingapp.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findByRentalUserId(Long userId);
}
