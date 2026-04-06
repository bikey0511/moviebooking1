package com.example.doannhom15.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_questions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false, length = 200)
    private String optionA;

    @Column(nullable = false, length = 200)
    private String optionB;

    @Column(nullable = false, length = 200)
    private String optionC;

    @Column(nullable = false, length = 200)
    private String optionD;

    /** A | B | C | D */
    @Column(nullable = false, length = 1)
    private String correctAnswer;

    /** Chỉ để gợi ý, không hiển thị cho user */
    @Column(length = 200)
    private String category;

    @Builder.Default
    private boolean active = true;
}
