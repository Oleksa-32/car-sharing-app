package com.example.carsharingapp.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharingapp.model.Rental;
import com.example.carsharingapp.repository.RentalRepository;
import com.example.carsharingapp.service.notification.NotificationService;
import com.example.carsharingapp.utils.TestDataUtil;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OverdueRentalCheckerTest {
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private OverdueRentalChecker checker;

    @Test
    void reportOverdues_noOverdueRentals_sendsAllClearNotification() {
        when(rentalRepository.findOverdueRentals(any())).thenReturn(List.of());

        checker.reportOverdues();

        verify(notificationService).sendNotification("✅ No rentals overdue today!");
    }

    @Test
    void reportOverdues_withOverdueRentals_sendsOverdueNotificationPerRental() {
        Rental overdue1 = TestDataUtil.overdueRental(1L);
        Rental overdue2 = TestDataUtil.overdueRental(2L);
        when(rentalRepository.findOverdueRentals(any())).thenReturn(List.of(overdue1, overdue2));

        checker.reportOverdues();

        verify(notificationService, times(2)).sendNotification(argThat(msg ->
                msg.startsWith("⏰ *Overdue Rental Alert*")
                        && msg.contains("Rental ID:")
                        && msg.contains("User:")
                        && msg.contains("Car:")
                        && msg.contains("Days overdue:")
        ));
    }
}
