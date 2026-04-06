package com.example.doannhom15.repository;

import com.example.doannhom15.model.Booking;
import com.example.doannhom15.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Page<Booking> findByUser(User user, Pageable pageable);
    
    List<Booking> findByUserOrderByBookingTimeDesc(User user);
    
    Page<Booking> findByStatus(Booking.BookingStatus status, Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = 'PAID'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = 'PAID' AND b.bookingTime >= :startDate AND b.bookingTime <= :endDate")
    BigDecimal getRevenueBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    long countByStatus(Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = 'PENDING'")
    Optional<Booking> findPendingBookingByUserId(Long userId);
    
    @Query(value = "SELECT MONTH(booking_time) as month, SUM(total_price) as revenue FROM bookings " +
           "WHERE status = 'PAID' AND YEAR(booking_time) = :year GROUP BY MONTH(booking_time) ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyRevenue(Integer year);

    /** Load booking với đủ quan hệ để gửi email (dùng trong async, transaction mới). Cần fetch cả Seat trong BookingSeat để template không LazyInit. */
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH b.showtime s " +
           "LEFT JOIN FETCH s.movie " +
           "LEFT JOIN FETCH s.room " +
           "LEFT JOIN FETCH b.bookingSeats bs " +
           "LEFT JOIN FETCH bs.seat " +
           "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetailsForEmail(@Param("id") Long id);

    Optional<Booking> findByTicketCode(String ticketCode);

    // Today's bookings
    @Query("SELECT b FROM Booking b WHERE b.bookingTime >= :startOfDay ORDER BY b.bookingTime DESC")
    List<Booking> findTodayBookings(@Param("startOfDay") LocalDateTime startOfDay);

    // Today's revenue
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status IN ('PAID', 'CONFIRMED') AND b.bookingTime >= :startOfDay")
    BigDecimal getTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay);

    // Recent bookings (limit)
    @Query("SELECT b FROM Booking b ORDER BY b.bookingTime DESC")
    List<Booking> findRecentBookings(Pageable pageable);

    // Count today bookings
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingTime >= :startOfDay")
    long countTodayBookings(@Param("startOfDay") LocalDateTime startOfDay);

    // Count pending bookings
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'PENDING'")
    long countPendingBookings();

    // Today's checked-in count
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkedInAt >= :startOfDay AND b.checkedInAt IS NOT NULL")
    long countTodayCheckedIn(@Param("startOfDay") LocalDateTime startOfDay);
}
