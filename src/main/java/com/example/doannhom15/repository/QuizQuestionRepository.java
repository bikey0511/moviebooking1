package com.example.doannhom15.repository;

import com.example.doannhom15.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByActiveTrue();

    /** Trả về ngẫu nhiên {limit} câu hỏi đang active, đảm bảo không trùng */
    @Query(value = """
        SELECT * FROM quiz_questions
        WHERE active = true
        ORDER BY RAND()
        LIMIT :limit
        """, nativeQuery = true)
    List<QuizQuestion> findRandomActive(@Param("limit") int limit);
}
