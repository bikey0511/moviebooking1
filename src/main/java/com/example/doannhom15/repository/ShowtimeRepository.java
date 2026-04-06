package com.example.doannhom15.repository;

import com.example.doannhom15.model.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    
    List<Showtime> findByMovieId(Long movieId);
    
    List<Showtime> findByRoomId(Long roomId);
    
    Page<Showtime> findAll(Pageable pageable);
    
    @Query("SELECT s FROM Showtime s WHERE s.startTime >= :startTime ORDER BY s.startTime ASC")
    List<Showtime> findUpcomingShowtimes(LocalDateTime startTime);
    
    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.startTime >= :startTime ORDER BY s.startTime ASC")
    List<Showtime> findByMovieIdAndStartTimeAfter(Long movieId, LocalDateTime startTime);

    List<Showtime> findByMovieIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            Long movieId, LocalDateTime fromInclusive, LocalDateTime toExclusive);

    List<Showtime> findByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            LocalDateTime fromInclusive, LocalDateTime toExclusive);
}
