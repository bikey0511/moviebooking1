package com.example.doannhom15.repository;

import com.example.doannhom15.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByRoomId(Long roomId);

    long countByRoomId(Long roomId);
    
    boolean existsByRoomIdAndSeatNumber(Long roomId, String seatNumber);
}
