package com.example.doannhom15.controller;

import com.example.doannhom15.model.Voucher;
import com.example.doannhom15.service.QuizService;
import com.example.doannhom15.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final UserService userService;

    /** Trang chơi mini-game (yêu cầu đăng nhập). */
    @GetMapping("/play")
    public String playPage(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        Long userId = userService.findByUsernameOrEmail(authentication.getName()).getId();

        // Kiểm tra đã chơi hôm nay chưa
        if (quizService.hasPlayedToday(userId)) {
            model.addAttribute("alreadyPlayedToday", true);
            return "user/quiz-already";
        }

        List<Map<String, String>> questions = quizService.getQuestionsForPlay();
        if (questions.isEmpty()) {
            model.addAttribute("noQuestions", true);
            return "user/quiz-no-questions";
        }

        model.addAttribute("questions", questions);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("requiredCorrect", quizService.getRequiredCorrectAnswers());
        return "user/quiz-play";
    }

    /** API nộp bài — kiểm tra đúng >= 7 câu. */
    @PostMapping("/api/submit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> submit(
            @RequestParam List<Long> questionIds,
            @RequestParam List<String> answers,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }
        Long userId = userService.findByUsernameOrEmail(authentication.getName()).getId();

        if (quizService.hasPlayedToday(userId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bạn đã chơi quiz hôm nay rồi."));
        }

        // Đếm đúng
        int correctCount = 0;
        for (int i = 0; i < questionIds.size(); i++) {
            if (quizService.checkAnswer(questionIds.get(i), answers.get(i))) {
                correctCount++;
            }
        }

        int required = quizService.getRequiredCorrectAnswers();
        boolean passed = correctCount >= required;
        boolean perfect = correctCount == questionIds.size();

        Voucher voucher = quizService.finishQuizPlay(userId, passed, perfect, correctCount);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("passed", passed);
        body.put("perfect", perfect);
        body.put("correctCount", correctCount);
        body.put("total", questionIds.size());
        body.put("required", required);
        if (passed && voucher != null) {
            String typeDisplay = voucher.getDiscountType() == Voucher.DiscountType.TICKET
                    ? "tiền vé" : "bắp nước";
            body.put("voucherCode", voucher.getCode());
            body.put("valueDisplay", "giảm " + voucher.getValue().stripTrailingZeros().toPlainString() + "%");
            body.put("typeDisplay", typeDisplay);
            body.put("discountType", voucher.getDiscountType().name());
            if (voucher.getValidTo() != null) {
                body.put("validTo", voucher.getValidTo().toString());
            }
        }
        return ResponseEntity.ok(body);
    }
}
