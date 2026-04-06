package com.example.doannhom15.repository;

import com.example.doannhom15.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Page<Movie> findByStatus(Movie.MovieStatus status, Pageable pageable);

    List<Movie> findByStatusOrderByReleaseDateDesc(Movie.MovieStatus status);

    List<Movie> findByTitleContainingIgnoreCase(String title, Sort sort);

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Movie> findByReleaseDate(LocalDate releaseDate, Pageable pageable);

    boolean existsByTitleIgnoreCaseAndReleaseDate(String title, LocalDate releaseDate);

    boolean existsByTitleIgnoreCaseAndReleaseDateAndIdNot(String title, LocalDate releaseDate, Long id);

    long countByStatus(Movie.MovieStatus status);
}
