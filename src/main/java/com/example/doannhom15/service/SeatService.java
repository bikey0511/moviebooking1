package com.example.doannhom15.service;

import com.example.doannhom15.model.Seat;
import com.example.doannhom15.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {
    
    private final SeatRepository seatRepository;
    
    public List<Seat> getSeatsByRoom(Long roomId) {
        return seatRepository.findByRoomId(roomId);
    }
    
    public Seat getSeatById(Long id) {
        return seatRepository.findById(id).orElse(null);
    }
    
    public boolean isSeatBooked(Long seatId, Long showtimeId) {
        return false;
    }
}
