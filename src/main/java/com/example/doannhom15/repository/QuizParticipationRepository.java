package com.example.doannhom15.repository;

import com.example.doannhom15.model.QuizParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizParticipationRepository extends JpaRepository<QuizParticipation, Long> {

    List<QuizParticipation> findByUserId(Long userId);

    Optional<QuizParticipation> findByUserIdAndWonTrue(Long userId);

    boolean existsByUserIdAndWonTrue(Long userId);

    /** Kiểm tra user đã chơi quiz trong ngày hôm nay chưa */
    @Query("SELECT COUNT(qp) > 0 FROM QuizParticipation qp WHERE qp.userId = :userId AND qp.participatedAt >= :startOfDay")
    boolean hasPlayedToday(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);

    /** Lấy lượt chơi gần nhất của user hôm nay */
    Optional<QuizParticipation> findTopByUserIdAndParticipatedAtGreaterThanEqualOrderByParticipatedAtDesc(Long userId, LocalDateTime startOfDay);
}
