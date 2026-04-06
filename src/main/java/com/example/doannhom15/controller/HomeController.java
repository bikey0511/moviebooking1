package com.example.doannhom15.controller;

import com.example.doannhom15.model.DiscountAnnouncement;
import com.example.doannhom15.model.Movie;
import com.example.doannhom15.model.MovieReview;
import com.example.doannhom15.model.Showtime;
import com.example.doannhom15.model.User;
import com.example.doannhom15.service.DiscountAnnouncementService;
import com.example.doannhom15.service.MovieReviewService;
import com.example.doannhom15.service.MovieService;
import com.example.doannhom15.service.ShowtimeService;
import com.example.doannhom15.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
@Controller
@RequestMapping
@RequiredArgsConstructor
public class HomeController {
    
    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final MovieReviewService movieReviewService;
    private final UserService userService;
    private final DiscountAnnouncementService discountAnnouncementService;
    
    /** Redirect /login → /auth/login (tránh 404 khi user gõ nhầm hoặc bookmark /login). */
    @GetMapping("/login")
    public String loginRedirect() {
        return "redirect:/auth/login";
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        List<Movie> nowShowing = movieService.getNowShowingMovies();
        List<Movie> comingSoon = movieService.getComingSoonMovies();
        List<Movie> bannerMovies = nowShowing.isEmpty() ? comingSoon : nowShowing;
        model.addAttribute("nowShowing", nowShowing);
        model.addAttribute("comingSoon", comingSoon);
        model.addAttribute("bannerMovies", bannerMovies.size() > 5 ? bannerMovies.subList(0, 5) : bannerMovies);
        model.addAttribute("promoAnnouncements", discountAnnouncementService.findActiveNow());

        List<Long> nowIds = nowShowing.stream().map(Movie::getId).toList();
        Map<Long, Double> avgMap = movieReviewService.getAverageRatingsForMovieIds(nowIds);
        model.addAttribute("movieAvgRatings", avgMap);
        if (authentication != null) {
            User u = userService.findByUsernameOrEmail(authentication.getName());
            if (u != null && !nowIds.isEmpty()) {
                model.addAttribute("reviewedMovieIds", new HashSet<>(movieReviewService.findReviewedMovieIds(u.getId(), nowIds)));
            }
        }
        return "user/home";
    }

    @GetMapping("/promo-announcement/{id}")
    public String promoAnnouncementDetail(@PathVariable Long id, Model model) {
        DiscountAnnouncement a = discountAnnouncementService.getById(id);
        if (a == null) {
            return "redirect:/";
        }
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("announcement", a);
        model.addAttribute("promoStillActive", a.isActiveAt(now));
        return "user/promo-announcement-detail";
    }
    
    @GetMapping("/movies")
    public String movies(@RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "12") int size,
                         Model model) {
        if (status != null && status.equals("coming-soon")) {
            model.addAttribute("movies", movieService.getMoviesByStatus(Movie.MovieStatus.COMING_SOON, page, size));
            model.addAttribute("currentStatus", "coming-soon");
        } else {
            model.addAttribute("movies", movieService.getMoviesByStatus(Movie.MovieStatus.NOW_SHOWING, page, size));
            model.addAttribute("currentStatus", "now-showing");
        }
        return "user/movies";
    }
    
    @GetMapping("/movie/{id}")
    public String movieDetail(@PathVariable Long id,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                            Model model,
                            Authentication authentication) {
        Movie movie = movieService.getMovieById(id);
        if (movie == null) return "redirect:/movies";
        LocalDate today = LocalDate.now();
        List<LocalDate> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDays.add(today.plusDays(i));
        }
        LocalDate selected = date != null ? date : today;
        if (selected.isBefore(today)) {
            selected = today;
        }
        if (selected.isAfter(today.plusDays(6))) {
            selected = today.plusDays(6);
        }
        List<Showtime> showtimes = showtimeService.getShowtimesByMovieOnDate(id, selected);
        List<MovieReview> reviews = movieReviewService.getReviewsByMovieId(id);
        Double avgRating = movieReviewService.getAverageRating(id);
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimes);
        model.addAttribute("weekDays", weekDays);
        model.addAttribute("selectedShowDate", selected);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("userReview", null);
        if (authentication != null) {
            User u = userService.findByUsernameOrEmail(authentication.getName());
            if (u != null) {
                model.addAttribute("userReview", movieReviewService.findByMovieAndUser(id, u.getId()).orElse(null));
            }
        }
        return "user/movie-detail";
    }

    @PostMapping("/movie/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @RequestParam int rating,
                               @RequestParam(required = false) String comment,
                               Authentication auth) {
        Movie movie = movieService.getMovieById(id);
        if (movie == null) return "redirect:/movies";
        User user = userService.findByUsernameOrEmail(auth != null ? auth.getName() : null);
        if (user == null) return "redirect:/auth/login?next=/movie/" + id;
        movieReviewService.saveReview(movie, user, rating, comment);
        return "redirect:/movie/" + id + "#reviews";
    }
    
    @GetMapping("/search")
    public String search(@RequestParam String q, Model model) {
        List<Movie> movies = movieService.searchMovies(q);
        model.addAttribute("movies", movies);
        model.addAttribute("query", q);
        return "user/search";
    }
}
