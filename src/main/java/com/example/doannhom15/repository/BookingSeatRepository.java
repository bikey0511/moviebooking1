package com.example.doannhom15.repository;

import com.example.doannhom15.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    
    List<BookingSeat> findByBookingId(Long bookingId);
    
    /**
     * Ghế bị khóa theo showtime:
     * - PAID, CONFIRMED: luôn khóa.
     * - PENDING: chỉ khóa khi còn hạn thanh toán.
     * => PENDING đã hết hạn sẽ tự nhả ghế ngay cả khi chưa chạy job hủy.
     */
    @Query("""
            SELECT bs.seat.id
            FROM BookingSeat bs
            WHERE bs.booking.showtime.id = :showtimeId
              AND (
                    bs.booking.status IN ('PAID', 'CONFIRMED')
                    OR (bs.booking.status = 'PENDING'
                        AND (bs.booking.paymentExpiry IS NULL OR bs.booking.paymentExpiry > CURRENT_TIMESTAMP))
                  )
            """)
    List<Long> findBookedSeatIdsByShowtimeId(Long showtimeId);
    
    boolean existsByBookingIdAndSeatId(Long bookingId, Long seatId);
}
