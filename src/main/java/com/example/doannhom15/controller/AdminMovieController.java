package com.example.doannhom15.controller;

import com.example.doannhom15.model.Movie;
import com.example.doannhom15.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/staff/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final MovieService movieService;

    @GetMapping
    public String listMovies(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
                             Model model) {
        Page<Movie> movies;
        if (releaseDate != null) {
            movies = movieService.getMoviesByReleaseDate(releaseDate, page, size);
        } else if (q != null && !q.isBlank()) {
            movies = movieService.searchMoviesPaged(q.trim(), page, size);
        } else {
            movies = movieService.getMovies(page, size);
        }
        model.addAttribute("movies", movies);
        model.addAttribute("currentPage", page);
        model.addAttribute("filterReleaseDate", releaseDate);
        String qTrim = q != null ? q.trim() : "";
        model.addAttribute("searchQuery", qTrim);
        model.addAttribute("searchQueryEncoded",
                !qTrim.isEmpty() ? URLEncoder.encode(qTrim, StandardCharsets.UTF_8) : null);
        return "admin/movies";
    }

    @GetMapping("/new")
    public String newMovieForm(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
                               Model model) {
        Movie movie = new Movie();
        if (releaseDate != null) {
            movie.setReleaseDate(releaseDate);
        }
        model.addAttribute("movie", movie);
        model.addAttribute("minReleaseDate", LocalDate.now());
        return "admin/movie-form";
    }

    @PostMapping
    public String createMovie(@ModelAttribute Movie movie,
                              @RequestParam(required = false) MultipartFile poster,
                              RedirectAttributes redirectAttributes) {
        try {
            movieService.validateMovieForSave(movie, null);
            if (poster != null && !poster.isEmpty()) {
                String filename = UUID.randomUUID() + "_" + poster.getOriginalFilename();
                Path path = Paths.get("src/main/resources/static/images", filename);
                Files.createDirectories(path.getParent());
                Files.write(path, poster.getBytes());
                movie.setPosterUrl("/images/" + filename);
            }
            movieService.saveMovie(movie);
            redirectAttributes.addFlashAttribute("success", "Thêm phim thành công!");
            return "redirect:/staff/movies";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/movies/new";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lưu ảnh poster thất bại.");
            return "redirect:/staff/movies/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editMovieForm(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id);
        if (movie == null) {
            return "redirect:/staff/movies";
        }
        model.addAttribute("movie", movie);
        model.addAttribute("minReleaseDate", LocalDate.now());
        return "admin/movie-form";
    }

    @PostMapping("/{id}")
    public String updateMovie(@PathVariable Long id,
                              @ModelAttribute Movie movie,
                              @RequestParam(required = false) MultipartFile poster,
                              RedirectAttributes redirectAttributes) {
        Movie existingMovie = movieService.getMovieById(id);
        if (existingMovie == null) {
            return "redirect:/staff/movies";
        }
        try {
            movie.setId(id);
            if (poster != null && !poster.isEmpty()) {
                String filename = UUID.randomUUID() + "_" + poster.getOriginalFilename();
                Path path = Paths.get("src/main/resources/static/images", filename);
                Files.createDirectories(path.getParent());
                Files.write(path, poster.getBytes());
                movie.setPosterUrl("/images/" + filename);
            } else {
                movie.setPosterUrl(existingMovie.getPosterUrl());
            }
            movieService.validateMovieForSave(movie, id);
            movieService.saveMovie(movie);
            redirectAttributes.addFlashAttribute("success", "Cập nhật phim thành công!");
            return "redirect:/staff/movies";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/movies/" + id + "/edit";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lưu ảnh poster thất bại.");
            return "redirect:/staff/movies/" + id + "/edit";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return "redirect:/staff/movies";
    }
}
