package com.example.doannhom15.repository;

import com.example.doannhom15.model.MovieReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MovieReviewRepository extends JpaRepository<MovieReview, Long> {
    List<MovieReview> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    Optional<MovieReview> findByMovie_IdAndUser_Id(Long movieId, Long userId);

    @Query("SELECT r.movie.id FROM MovieReview r WHERE r.user.id = :uid AND r.movie.id IN :mids")
    Set<Long> findMovieIdsReviewedByUserId(@Param("uid") Long userId, @Param("mids") Collection<Long> movieIds);

    @Query("SELECT r.movie.id, AVG(r.rating) FROM MovieReview r WHERE r.movie.id IN :mids GROUP BY r.movie.id")
    List<Object[]> avgRatingByMovieIds(@Param("mids") Collection<Long> movieIds);

    @Query("SELECT AVG(r.rating) FROM MovieReview r WHERE r.movie.id = :movieId")
    Double getAverageRatingByMovieId(Long movieId);
}
