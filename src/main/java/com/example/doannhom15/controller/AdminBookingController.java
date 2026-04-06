package com.example.doannhom15.controller;

import com.example.doannhom15.model.Booking;
import com.example.doannhom15.service.BookingService;
import com.example.doannhom15.service.EmailService;
import com.example.doannhom15.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/bookings")
@RequiredArgsConstructor
public class AdminBookingController {
    
    private final BookingService bookingService;
    private final EmailService emailService;
    private final PaymentService paymentService;
    
    @GetMapping
    public String listBookings(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String status,
                               Model model) {
        Page<Booking> bookings;
        if (status != null && !status.isEmpty()) {
            try {
                Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
                bookings = bookingService.getBookingsByStatus(bookingStatus, page, size);
                model.addAttribute("currentStatus", status);
            } catch (IllegalArgumentException e) {
                bookings = bookingService.getAllBookings(page, size);
            }
        } else {
            bookings = bookingService.getAllBookings(page, size);
        }
        
        Map<Long, java.math.BigDecimal> totalPaymentMap = new HashMap<>();
        bookings.getContent().forEach(b -> totalPaymentMap.put(b.getId(), paymentService.getTotalAmount(b)));
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalPaymentMap", totalPaymentMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookings.getTotalPages());
        
        return "admin/bookings";
    }
    
    @GetMapping("/{id}")
    public String viewBooking(@PathVariable Long id, Model model) {
        Booking booking = bookingService.getBookingById(id);
        if (booking == null) {
            return "redirect:/staff/bookings";
        }
        model.addAttribute("booking", booking);
        model.addAttribute("totalPayment", paymentService.getTotalAmount(booking));
        BigDecimal tp = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal ct = booking.getConcessionTotal() != null ? booking.getConcessionTotal() : BigDecimal.ZERO;
        model.addAttribute("grossTotal", tp.add(ct));
        return "admin/booking-detail";
    }
    
    @PostMapping("/{id}/confirm")
    public String confirmBooking(@PathVariable Long id) {
        bookingService.confirmPayment(id);
        emailService.sendTicketEmail(id);
        return "redirect:/staff/bookings/" + id;
    }
    
    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return "redirect:/staff/bookings/" + id;
    }

    @PostMapping("/{id}/reset-check-in")
    public String resetCheckIn(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.clearCheckInByStaff(id);
            redirectAttributes.addFlashAttribute("success",
                    "Đã hoàn tác check-in. Khách có thể quét mã vé lại.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/bookings/" + id;
    }
    
    @GetMapping("/{id}/delete")
    public String deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return "redirect:/staff/bookings";
    }
}
