package com.example.doannhom15.controller;

import com.example.doannhom15.model.CinemaRoom;
import com.example.doannhom15.model.Movie;
import com.example.doannhom15.model.Showtime;
import com.example.doannhom15.service.CinemaRoomService;
import com.example.doannhom15.service.MovieService;
import com.example.doannhom15.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/staff/showtimes")
@RequiredArgsConstructor
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final CinemaRoomService cinemaRoomService;

    private List<LocalDate> weekFromToday() {
        LocalDate t = LocalDate.now();
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(t.plusDays(i));
        }
        return days;
    }

    private LocalDate clampToWeek(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate last = today.plusDays(6);
        if (date == null) {
            return today;
        }
        if (date.isBefore(today)) {
            return today;
        }
        if (date.isAfter(last)) {
            return last;
        }
        return date;
    }

    @GetMapping
    public String showtimes(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            Model model) {
        LocalDate selected = clampToWeek(date);
        List<Showtime> showtimes = showtimeService.getShowtimesForCalendarDay(selected);
        model.addAttribute("weekDays", weekFromToday());
        model.addAttribute("selectedDate", selected);
        model.addAttribute("showtimes", showtimes);
        return "admin/showtimes";
    }

    @GetMapping("/new")
    public String newShowtime(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              Model model) {
        LocalDate d = clampToWeek(date);
        List<Movie> movies = movieService.getAllMovies();
        List<CinemaRoom> rooms = cinemaRoomService.getAllRooms();

        boolean isSelectedToday = d.equals(LocalDate.now());
        String minLocalTime = isSelectedToday
                ? LocalTime.now().truncatedTo(ChronoUnit.MINUTES).format(DateTimeFormatter.ISO_LOCAL_TIME)
                : null;

        model.addAttribute("movies", movies);
        model.addAttribute("rooms", rooms);
        model.addAttribute("showtime", new Showtime());
        model.addAttribute("returnDate", d);
        model.addAttribute("prefillDate", d);
        model.addAttribute("isSelectedToday", isSelectedToday);
        model.addAttribute("minLocalTime", minLocalTime);
        model.addAttribute("defaultShowTime", "19:00");
        return "admin/showtime-form";
    }

    @PostMapping("/save")
    public String saveShowtime(@RequestParam Long movieId,
                               @RequestParam Long roomId,
                               @RequestParam String showTime,
                               @RequestParam String price,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                               RedirectAttributes redirectAttributes) {

        Movie movie = movieService.getMovieById(movieId);
        CinemaRoom room = cinemaRoomService.getRoomById(roomId);
        LocalDate d = clampToWeek(returnDate != null ? returnDate : LocalDate.now());

        if (movie == null || room == null) {
            redirectAttributes.addFlashAttribute("error", "Phim hoặc phòng không hợp lệ.");
            return "redirect:/staff/showtimes/new?date=" + d;
        }

        LocalTime t;
        try {
            t = LocalTime.parse(showTime);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Giờ chiếu không hợp lệ.");
            return "redirect:/staff/showtimes/new?date=" + d;
        }
        LocalDateTime start = LocalDateTime.of(d, t);
        if (!start.isAfter(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Suất chiếu phải ở thời điểm trong tương lai.");
            return "redirect:/staff/showtimes/new?date=" + d;
        }
        try {
            showtimeService.validateNewShowtime(roomId, start, movie, null);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/staff/showtimes/new?date=" + d;
        }

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .startTime(start)
                .price(new BigDecimal(price))
                .build();
        showtimeService.createShowtime(showtime);
        redirectAttributes.addFlashAttribute("success", "Thêm suất chiếu thành công!");
        return "redirect:/staff/showtimes?date=" + d;
    }

    @PostMapping("/delete/{id}")
    public String deleteShowtime(@PathVariable Long id,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 RedirectAttributes redirectAttributes) {
        try {
            showtimeService.deleteShowtime(id);
            redirectAttributes.addFlashAttribute("success", "Xóa suất chiếu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Không thể xóa suất chiếu (có đơn đặt vé liên quan hoặc lỗi dữ liệu).");
        }
        LocalDate rd = clampToWeek(date);
        return "redirect:/staff/showtimes?date=" + rd;
    }
}
