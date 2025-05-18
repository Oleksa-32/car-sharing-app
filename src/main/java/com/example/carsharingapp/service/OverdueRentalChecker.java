package com.example.carsharingapp.service;

import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.notification.NotificationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OverdueRentalChecker {
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void reportOverdues() {
        LocalDateTime cutoff = LocalDate.now()
                .atStartOfDay()
                .plusDays(1);
        List<Rental> overdue = rentalRepository.findOverdueRentals(cutoff);

        if (overdue.isEmpty()) {
            notificationService.sendNotification("✅ No rentals overdue today!");
        } else {
            for (Rental rental : overdue) {
                String text = new StringBuilder()
                        .append("⏰ *Overdue Rental Alert*\n")
                        .append("• *Rental ID:* ").append(rental.getId()).append("\n")
                        .append("• *Due on:* ").append(rental.getReturnDate()).append("\n")
                        .append("• *User:* ")
                        .append(rental.getUser().getFirstName())
                        .append(" ").append(rental.getUser().getLastName()).append("\n")
                        .append("• *Car:* ")
                        .append(rental.getCar().getBrand())
                        .append(" ").append(rental.getCar().getModel()).append("\n")
                        .append("• *Days overdue:* ")
                        .append(ChronoUnit.DAYS.between(rental.getReturnDate(),
                                LocalDateTime.now()))
                        .toString();
                notificationService.sendNotification(text);
            }
        }
    }
}
