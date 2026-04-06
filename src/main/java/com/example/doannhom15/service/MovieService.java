package com.example.doannhom15.service;

import com.example.doannhom15.model.Movie;
import com.example.doannhom15.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {
    
    private final MovieRepository movieRepository;
    
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }
    
    public Page<Movie> getMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending());
        return movieRepository.findAll(pageable);
    }
    
    public Page<Movie> getMoviesByStatus(Movie.MovieStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending());
        return movieRepository.findByStatus(status, pageable);
    }
    
    public List<Movie> getNowShowingMovies() {
        return movieRepository.findByStatusOrderByReleaseDateDesc(Movie.MovieStatus.NOW_SHOWING);
    }
    
    public List<Movie> getComingSoonMovies() {
        return movieRepository.findByStatusOrderByReleaseDateDesc(Movie.MovieStatus.COMING_SOON);
    }
    
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id).orElse(null);
    }
    
    /** Không cho trùng tên (không phân biệt hoa thường) + cùng ngày khởi chiếu; ngày khởi chiếu không trước hôm nay. */
    public void validateMovieForSave(Movie movie, Long excludeId) {
        if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            throw new IllegalArgumentException("Nhập tên phim.");
        }
        movie.setTitle(movie.getTitle().trim());
        if (movie.getReleaseDate() == null) {
            throw new IllegalArgumentException("Chọn ngày khởi chiếu.");
        }
        if (movie.getReleaseDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày khởi chiếu không được trước ngày hôm nay.");
        }
        if (excludeId == null) {
            if (movieRepository.existsByTitleIgnoreCaseAndReleaseDate(movie.getTitle(), movie.getReleaseDate())) {
                throw new IllegalArgumentException(
                        "Đã có phim cùng tên và cùng ngày khởi chiếu. Mỗi ngày chỉ được một bản ghi cho tên này.");
            }
        } else {
            if (movieRepository.existsByTitleIgnoreCaseAndReleaseDateAndIdNot(
                    movie.getTitle(), movie.getReleaseDate(), excludeId)) {
                throw new IllegalArgumentException(
                        "Đã có phim khác cùng tên và cùng ngày khởi chiếu.");
            }
        }
    }

    @Transactional
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }
    
    @Transactional
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
    
    public List<Movie> searchMovies(String keyword) {
        return movieRepository.findByTitleContainingIgnoreCase(
                keyword, Sort.by(Sort.Direction.DESC, "releaseDate"));
    }

    public Page<Movie> searchMoviesPaged(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending());
        return movieRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    public Page<Movie> getMoviesByReleaseDate(LocalDate releaseDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        return movieRepository.findByReleaseDate(releaseDate, pageable);
    }
    
    public long count() {
        return movieRepository.count();
    }
    
    public long countNowShowing() {
        return movieRepository.countByStatus(Movie.MovieStatus.NOW_SHOWING);
    }
}
