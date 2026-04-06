package com.example.doannhom15.service;

import com.example.doannhom15.model.Movie;
import com.example.doannhom15.model.MovieReview;
import com.example.doannhom15.model.User;
import com.example.doannhom15.repository.MovieReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MovieReviewService {
    private final MovieReviewRepository reviewRepository;

    public List<MovieReview> getReviewsByMovieId(Long movieId) {
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId);
    }

    public Double getAverageRating(Long movieId) {
        Double avg = reviewRepository.getAverageRatingByMovieId(movieId);
        return avg != null ? Math.round(avg * 10) / 10.0 : null;
    }

    public Optional<MovieReview> findByMovieAndUser(Long movieId, Long userId) {
        if (movieId == null || userId == null) return Optional.empty();
        return reviewRepository.findByMovie_IdAndUser_Id(movieId, userId);
    }

    public Map<Long, Double> getAverageRatingsForMovieIds(List<Long> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) return Collections.emptyMap();
        List<Object[]> rows = reviewRepository.avgRatingByMovieIds(movieIds);
        Map<Long, Double> map = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null || row[1] == null) continue;
            Long id = (Long) row[0];
            double avg = ((Number) row[1]).doubleValue();
            map.put(id, Math.round(avg * 10) / 10.0);
        }
        return map;
    }

    public Set<Long> findReviewedMovieIds(Long userId, List<Long> movieIds) {
        if (userId == null || movieIds == null || movieIds.isEmpty()) return Collections.emptySet();
        return reviewRepository.findMovieIdsReviewedByUserId(userId, movieIds);
    }

    @Transactional
    public MovieReview saveReview(Movie movie, User user, int rating, String comment) {
        if (rating < 1) rating = 1;
        if (rating > 5) rating = 5;
        String c = comment != null ? comment.trim() : null;
        if (c != null && c.isEmpty()) c = null;
        Optional<MovieReview> existing = reviewRepository.findByMovie_IdAndUser_Id(movie.getId(), user.getId());
        if (existing.isPresent()) {
            MovieReview r = existing.get();
            r.setRating(rating);
            r.setComment(c);
            return reviewRepository.save(r);
        }
        MovieReview review = MovieReview.builder()
                .movie(movie)
                .user(user)
                .rating(rating)
                .comment(c)
                .build();
        return reviewRepository.save(review);
    }
}
