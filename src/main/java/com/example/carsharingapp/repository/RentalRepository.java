package com.example.carsharingapp.repository;

import com.example.carsharingapp.model.Rental;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserIdAndActualReturnDateIsNull(Long userId);

    List<Rental> findByUserIdAndActualReturnDateIsNotNull(Long userId);

    @Query("SELECT r FROM Rental r WHERE r.returnDate <= :cutoff AND r.actualReturnDate IS NULL")
    List<Rental> findOverdueRentals(@Param("cutoff") LocalDateTime cutoff);
}
