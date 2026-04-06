package com.example.doannhom15.controller;

import com.example.doannhom15.model.CinemaRoom;
import com.example.doannhom15.service.CinemaRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/rooms")
@RequiredArgsConstructor
public class AdminRoomController {
    
    private final CinemaRoomService cinemaRoomService;
    
    @GetMapping
    public String rooms(Model model) {
        model.addAttribute("rooms", cinemaRoomService.getAllRooms());
        return "admin/rooms";
    }
    
    @GetMapping("/new")
    public String newRoom(Model model) {
        model.addAttribute("room", new CinemaRoom());
        return "admin/room-form";
    }
    
    @PostMapping("/save")
    public String saveRoom(@ModelAttribute CinemaRoom room, RedirectAttributes redirectAttributes) {
        cinemaRoomService.saveRoom(room);
        if (room.getId() == null) {
            redirectAttributes.addFlashAttribute("success", "Thêm phòng chiếu thành công!");
        } else {
            redirectAttributes.addFlashAttribute("success", "Cập nhật phòng chiếu thành công!");
        }
        return "redirect:/staff/rooms";
    }
    
    @GetMapping("/edit/{id}")
    public String editRoom(@PathVariable Long id, Model model) {
        CinemaRoom room = cinemaRoomService.getRoomById(id);
        model.addAttribute("room", room);
        return "admin/room-form";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        cinemaRoomService.deleteRoom(id);
        redirectAttributes.addFlashAttribute("success", "Xóa phòng chiếu thành công!");
        return "redirect:/staff/rooms";
    }
}
