package com.example.doannhom15.repository;

import com.example.doannhom15.model.BookingConcession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingConcessionRepository extends JpaRepository<BookingConcession, Long> {
    List<BookingConcession> findByBookingId(Long bookingId);
    void deleteByBookingId(Long bookingId);
}
