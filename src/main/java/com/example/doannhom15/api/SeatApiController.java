package com.example.doannhom15.api;

import com.example.doannhom15.model.Booking;
import com.example.doannhom15.model.Payment;
import com.example.doannhom15.model.Seat;
import com.example.doannhom15.model.User;
import com.example.doannhom15.repository.SeatRepository;
import com.example.doannhom15.service.BookingService;
import com.example.doannhom15.service.PaymentService;
import com.example.doannhom15.service.ShowtimeService;
import com.example.doannhom15.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeatApiController {
    
    private final ShowtimeService showtimeService;
    private final BookingService bookingService;
    private final SeatRepository seatRepository;
    private final UserService userService;
    private final PaymentService paymentService;
    
    @GetMapping("/showtime/{id}/seats")
    public ResponseEntity<Map<String, Object>> getSeats(@PathVariable Long id) {
        var showtime = showtimeService.getShowtimeById(id);
        if (showtime == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Long> bookedSeatIds = bookingService.getBookedSeatIds(id);
        List<Seat> allSeats = seatRepository.findByRoomId(showtime.getRoom().getId());
        
        List<String> availableSeats = allSeats.stream()
                .filter(seat -> !bookedSeatIds.contains(seat.getId()))
                .map(Seat::getSeatNumber)
                .collect(Collectors.toList());
        
        List<String> bookedSeats = allSeats.stream()
                .filter(seat -> bookedSeatIds.contains(seat.getId()))
                .map(Seat::getSeatNumber)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("availableSeats", availableSeats);
        response.put("bookedSeats", bookedSeats);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/bookings")
    public ResponseEntity<Map<String, Object>> createBooking(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long showtimeId = Long.parseLong(request.get("showtimeId").toString());
            List<Long> seatIds = ((List<Number>) request.get("seatIds")).stream()
                    .map(Number::longValue)
                    .collect(Collectors.toList());
            
            User user = userService.findByUsernameOrEmail(authentication.getName());
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để đặt vé");
                return ResponseEntity.status(401).body(response);
            }
            
            Booking booking = bookingService.createBooking(user, showtimeId, seatIds);
            
            // Tạo payment
            Payment payment = paymentService.createPayment(booking);
            
            response.put("success", true);
            response.put("message", "Đặt vé thành công!");
            response.put("bookingId", booking.getId());
            response.put("paymentCode", payment.getPaymentCode());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
