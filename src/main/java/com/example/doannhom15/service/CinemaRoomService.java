package com.example.doannhom15.service;

import com.example.doannhom15.model.CinemaRoom;
import com.example.doannhom15.model.Seat;
import com.example.doannhom15.repository.CinemaRoomRepository;
import com.example.doannhom15.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaRoomService {
    
    private final CinemaRoomRepository cinemaRoomRepository;
    private final SeatRepository seatRepository;
    
    public List<CinemaRoom> getAllRooms() {
        return cinemaRoomRepository.findAll();
    }
    
    public CinemaRoom getRoomById(Long id) {
        return cinemaRoomRepository.findById(id).orElse(null);
    }
    
    @Transactional
    public CinemaRoom saveRoom(CinemaRoom room) {
        boolean isNew = room.getId() == null;
        CinemaRoom savedRoom = cinemaRoomRepository.save(room);
        if (isNew && savedRoom.getRoomType() != null) {
            createSeatsByType(savedRoom);
        }
        return savedRoom;
    }
    
    /** Phòng thường: 120 chỗ, hàng cuối (L) có 5 ghế đôi (10 ghế) dàn cách; A–K mỗi hàng 10 ghế thường. VIP: 40 ghế nằm. */
    private void createSeatsByType(CinemaRoom room) {
        List<Seat> seats = new ArrayList<>();
        if (room.getRoomType() == CinemaRoom.RoomType.NORMAL) {
            room.setTotalSeats(120);
            cinemaRoomRepository.save(room);
            // Hàng A đến K: mỗi hàng 10 ghế thường (110 ghế)
            String[] normalRows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};
            for (String row : normalRows) {
                for (int i = 1; i <= 10; i++) {
                    seats.add(Seat.builder().seatNumber(row + i).room(room).rowName(row).seatType("STANDARD").build());
                }
            }
            // Hàng L (cuối dãy): 5 ghế đôi = 10 ghế (L1–L2, L3–L4, L5–L6, L7–L8, L9–L10)
            for (int i = 1; i <= 10; i++) {
                seats.add(Seat.builder().seatNumber("L" + i).room(room).rowName("L").seatType("DOUBLE").build());
            }
        } else if (room.getRoomType() == CinemaRoom.RoomType.VIP) {
            room.setTotalSeats(40);
            cinemaRoomRepository.save(room);
            // 40 ghế nằm: 5 hàng x 8 ghế
            String[] rows = {"A", "B", "C", "D", "E"};
            for (String row : rows) {
                for (int i = 1; i <= 8; i++) {
                    seats.add(Seat.builder().seatNumber(row + i).room(room).rowName(row).seatType("RECLINING").build());
                }
            }
        }
        if (!seats.isEmpty()) {
            seatRepository.saveAll(seats);
        }
    }
    
    @Transactional
    public CinemaRoom createRoom(CinemaRoom room) {
        CinemaRoom savedRoom = cinemaRoomRepository.save(room);
        if (savedRoom.getRoomType() != null) {
            createSeatsByType(savedRoom);
        } else {
            List<Seat> seats = new ArrayList<>();
            String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H"};
            for (String row : rows) {
                for (int i = 1; i <= 10; i++) {
                    seats.add(Seat.builder().seatNumber(row + i).room(savedRoom).rowName(row).seatType("STANDARD").build());
                }
            }
            seatRepository.saveAll(seats);
        }
        return savedRoom;
    }
    
    @Transactional
    public CinemaRoom updateRoom(CinemaRoom room) {
        return cinemaRoomRepository.save(room);
    }

    /**
     * Nếu phòng đã có loại (NORMAL/VIP) nhưng chưa có bản ghi ghế (DB cũ / lỗi import), tạo lưới ghế.
     */
    @Transactional
    public void ensureSeatsForRoomIfEmpty(Long roomId) {
        if (roomId == null || seatRepository.countByRoomId(roomId) > 0) {
            return;
        }
        CinemaRoom room = cinemaRoomRepository.findById(roomId).orElse(null);
        if (room == null || room.getRoomType() == null) {
            return;
        }
        createSeatsByType(room);
    }
    
    @Transactional
    public void deleteRoom(Long id) {
        cinemaRoomRepository.deleteById(id);
    }
    
    public long count() {
        return cinemaRoomRepository.count();
    }
}
