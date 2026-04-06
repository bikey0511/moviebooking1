package com.example.doannhom15.controller;

import com.example.doannhom15.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/staff/check-in")
@RequiredArgsConstructor
public class StaffCheckInController {

    private final BookingService bookingService;

    @GetMapping
    public String form(Model model) {
        return "staff/check-in";
    }

    /** API cho trang quét QR (fetch): trả JSON để biết thành công / lỗi (vé đã dùng, v.v.). */
    @PostMapping(value = "/api", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> submitApi(@RequestParam(required = false) String ticketCode) {
        Map<String, Object> body = new LinkedHashMap<>();
        try {
            bookingService.checkInByTicketCode(ticketCode);
            body.put("ok", true);
        } catch (IllegalArgumentException ex) {
            body.put("ok", false);
            body.put("message", ex.getMessage());
        }
        return body;
    }

    @PostMapping
    public String submit(@RequestParam(required = false) String ticketCode,
                         RedirectAttributes redirectAttributes) {
        try {
            bookingService.checkInByTicketCode(ticketCode);
            redirectAttributes.addFlashAttribute("success", "Check-in thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/staff/check-in";
    }
}
