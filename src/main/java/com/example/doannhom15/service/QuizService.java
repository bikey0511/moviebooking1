package com.example.doannhom15.service;

import com.example.doannhom15.model.*;
import com.example.doannhom15.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Mini-game: 10 câu.
 * - 7–9/10: voucher 10%, loại vé hoặc bắp nước ngẫu nhiên (user không chọn).
 * - 10/10: giảm % ngẫu nhiên 15–45%, loại vé hoặc bắp nước ngẫu nhiên.
 * Voucher maxUses = 1 (chỉ dùng được 1 lần).
 * Mỗi ngày chỉ chơi 1 lần.
 */
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizParticipationRepository quizParticipationRepository;
    private final VoucherRepository voucherRepository;

    /** Lấy ngẫu nhiên 10 câu hỏi để chơi (không trả correctAnswer về client). */
    public List<Map<String, String>> getQuestionsForPlay() {
        List<QuizQuestion> qs = quizQuestionRepository.findRandomActive(10);
        if (qs == null) qs = List.of();
        return qs.stream().map(q -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("id", String.valueOf(q.getId()));
            m.put("question", q.getQuestion());
            m.put("optionA", q.getOptionA());
            m.put("optionB", q.getOptionB());
            m.put("optionC", q.getOptionC());
            m.put("optionD", q.getOptionD());
            return m;
        }).toList();
    }

    /** Kiểm tra đáp án 1 câu: true = đúng. */
    public boolean checkAnswer(Long questionId, String answer) {
        return quizQuestionRepository.findById(questionId)
                .map(q -> q.getCorrectAnswer().equalsIgnoreCase(answer.trim()))
                .orElse(false);
    }

    /** Lấy đáp án đúng của 1 câu (để verify sau khi user nộp). */
    public String getCorrectAnswer(Long questionId) {
        return quizQuestionRepository.findById(questionId)
                .map(QuizQuestion::getCorrectAnswer)
                .orElse("A");
    }

    /** Kiểm tra user đã từng thắng quiz chưa (dùng cho limit tổng). */
    public boolean hasUserWon(Long userId) {
        return quizParticipationRepository.existsByUserIdAndWonTrue(userId);
    }

    /** Kiểm tra user đã chơi quiz hôm nay chưa. */
    public boolean hasPlayedToday(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return quizParticipationRepository.hasPlayedToday(userId, startOfDay);
    }

    /** Tính số câu đúng cần để thắng (70%). */
    public int getRequiredCorrectAnswers() {
        return 7; // 7/10 = 70%
    }

    /**
     * Tạo voucher quiz: loại vé / bắp nước ngẫu nhiên; 10/10 → 15–45%, còn lại khi đạt → 10%.
     */
    @Transactional
    public Voucher createQuizRewardVoucher(Long userId, boolean perfect) {
        Random rnd = new Random();
        boolean ticketType = rnd.nextBoolean();
        int pct = perfect ? (15 + rnd.nextInt(31)) : 10; // 15..45 hoặc 10

        Voucher.DiscountType discountType = ticketType
                ? Voucher.DiscountType.TICKET
                : Voucher.DiscountType.CONCESSION;

        String typeLabel = perfect
                ? (ticketType ? "PERFT" : "PERFC")
                : (ticketType ? "QUIZV" : "QUIZB");

        String code;
        do {
            code = typeLabel + "-" + UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 6).toUpperCase();
        } while (voucherRepository.findByCodeIgnoreCaseAndActiveTrue(code).isPresent());

        Voucher voucher = Voucher.builder()
                .code(code)
                .ownerUserId(userId)
                .discountType(discountType)
                .valueType(Voucher.ValueType.PERCENT)
                .value(BigDecimal.valueOf(pct))
                .validFrom(LocalDateTime.now())
                .validTo(LocalDateTime.now().plusDays(7))
                .maxUses(1)
                .active(true)
                .build();
        return voucherRepository.save(voucher);
    }

    /**
     * Kết thúc lượt chơi: thua → chỉ ghi nhận; thắng → tạo voucher ngay (không bước claim).
     */
    @Transactional
    public Voucher finishQuizPlay(Long userId, boolean passed, boolean perfect, int correctCount) {
        if (!passed) {
            quizParticipationRepository.save(QuizParticipation.builder()
                    .userId(userId)
                    .voucherCode("-")
                    .participatedAt(LocalDateTime.now())
                    .won(false)
                    .build());
            return null;
        }
        Voucher voucher = createQuizRewardVoucher(userId, perfect);
        quizParticipationRepository.save(QuizParticipation.builder()
                .userId(userId)
                .voucherCode(voucher.getCode())
                .participatedAt(LocalDateTime.now())
                .won(true)
                .build());
        return voucher;
    }
}
