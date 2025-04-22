package com.example.carsharingapp.service;

import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OverdueRentalChecker {
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 * * * * *")
    public void reportOverdues() {
        // 1. compute “tomorrow at 00:00” cut‑off
        LocalDateTime cutoff = LocalDate.now()
                .atStartOfDay()
                .plusDays(1);

        // 2. fetch overdue rentals
        List<Rental> overdue = rentalRepository.findOverdueRentals(cutoff);

        if (overdue.isEmpty()) {
            // 3a. none overdue → single “all clear” message
            notificationService.sendNotification("✅ No rentals overdue today!");
        } else {
            // 3b. for each overdue, send detailed alert
            for (Rental r : overdue) {
                String text = new StringBuilder()
                        .append("⏰ *Overdue Rental Alert*\n")
                        .append("• *Rental ID:* ").append(r.getId()).append("\n")
                        .append("• *Due on:* ").append(r.getReturnDate()).append("\n")
                        .append("• *User:* ")
                        .append(r.getUser().getFirstName())
                        .append(" ").append(r.getUser().getLastName()).append("\n")
                        .append("• *Car:* ")
                        .append(r.getCar().getBrand())
                        .append(" ").append(r.getCar().getModel()).append("\n")
                        .append("• *Days overdue:* ")
                        .append(ChronoUnit.DAYS.between(r.getReturnDate(), LocalDateTime.now()))
                        .toString();

                notificationService.sendNotification(text);
            }
        }
    }
}
