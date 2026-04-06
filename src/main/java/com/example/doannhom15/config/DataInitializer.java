package com.example.doannhom15.config;

import com.example.doannhom15.model.CinemaRoom;
import com.example.doannhom15.model.Movie;
import com.example.doannhom15.model.QuizQuestion;
import com.example.doannhom15.model.Showtime;
import com.example.doannhom15.model.User;
import com.example.doannhom15.repository.BookingRepository;
import com.example.doannhom15.repository.CinemaRoomRepository;
import com.example.doannhom15.repository.MovieRepository;
import com.example.doannhom15.repository.PaymentRepository;
import com.example.doannhom15.repository.QuizQuestionRepository;
import com.example.doannhom15.repository.SeatRepository;
import com.example.doannhom15.repository.ShowtimeRepository;
import com.example.doannhom15.repository.UserRepository;
import com.example.doannhom15.service.CinemaRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final CinemaRoomRepository cinemaRoomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CinemaRoomService cinemaRoomService;
    private final QuizQuestionRepository quizQuestionRepository;
    
    @Override
    public void run(String... args) {
        ensureStaffUser();
        // Chỉ khởi tạo dữ liệu mẫu khi DB trống (lần đầu) — không xóa dữ liệu khi refresh/restart
        if (cinemaRoomRepository.count() > 0 || movieRepository.count() > 0) {
            log.info("Data already exists, skipping initializer to preserve your data.");
            // Vẫn đảm bảo có quiz questions
            ensureQuizQuestions();
            return;
        }
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        showtimeRepository.deleteAll();
        movieRepository.deleteAll();
        seatRepository.deleteAll();
        cinemaRoomRepository.deleteAll();
        initializeData();
    }

    private void ensureStaffUser() {
        if (!userRepository.existsByUsername("staff")) {
            User staff = User.builder()
                    .username("staff")
                    .email("staff@cinema.com")
                    .password(passwordEncoder.encode("staff123"))
                    .role(User.Role.STAFF)
                    .enabled(true)
                    .build();
            userRepository.save(staff);
            log.info("Created staff user: staff/staff123");
        }
    }
    
    private void initializeData() {
        log.info("Initializing default data...");
        
        // Chỉ tạo admin và user nếu chưa có (giữ tài khoản đã đăng ký)
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@cinema.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Created admin user: admin/admin123");
        }
        
        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .email("user@cinema.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(User.Role.USER)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            log.info("Created default user: user/user123");
        }
        
        // Create cinema rooms: Phòng thường 120 chỗ (5 ghế đôi hàng L), Phòng VIP 40 ghế nằm
        CinemaRoom room1 = cinemaRoomService.saveRoom(CinemaRoom.builder()
                .name("Phòng 1")
                .roomType(CinemaRoom.RoomType.NORMAL)
                .build());
        CinemaRoom room2 = cinemaRoomService.saveRoom(CinemaRoom.builder()
                .name("Phòng 2")
                .roomType(CinemaRoom.RoomType.NORMAL)
                .build());
        CinemaRoom room3 = cinemaRoomService.saveRoom(CinemaRoom.builder()
                .name("Phòng VIP")
                .roomType(CinemaRoom.RoomType.VIP)
                .build());
        
        // Create sample movies (Phim)
        Movie movie1 = Movie.builder()
                .title("Avatar: Thủy Thủ Của Người Cá")
                .description("Jake Sully sống cùng gia đình mới được hình thành trên vệ tinh Pandora. Khi một mối đe dọa quen thuộc quay trở lại để hoàn thành những gì đã bắt đầu, Jake phải làm việc với Neytiri và đội quân Na'vi để bảo vệ Pandora.")
                .duration(192)
                .posterUrl("/images/avatar.jpg")
                .releaseDate(LocalDate.of(2024, 1, 15))
                .status(Movie.MovieStatus.NOW_SHOWING)
                .build();
        
        Movie movie2 = Movie.builder()
                .title("Dune: Hồi Ba Hai")
                .description("Paul Atreides đoàn kết với Chani và người Fremen trong cuộc trả thù chống lại những kẻ âm mưu đã phá hủy gia đình anh.")
                .duration(166)
                .posterUrl("/images/dune.jpg")
                .releaseDate(LocalDate.of(2024, 3, 1))
                .status(Movie.MovieStatus.NOW_SHOWING)
                .build();
        
        Movie movie3 = Movie.builder()
                .title("Biệt Đội Marvel")
                .description("Carol Danvers mắc kẹt trong hố đen và sức mạnh của cô bị thay đổi. Giờ cô phải ổn định vũ trụ và cứu thế giới.")
                .duration(105)
                .posterUrl("/images/marvels.jpg")
                .releaseDate(LocalDate.of(2024, 5, 10))
                .status(Movie.MovieStatus.NOW_SHOWING)
                .build();
        
        Movie movie4 = Movie.builder()
                .title("Kung Fu Panda 4")
                .description("Po tiếp tục cuộc phiêu lưu mới để trở thành Thủ lĩnh Thung Lũng Hòa Bình.")
                .duration(95)
                .posterUrl("/images/panda.jpg")
                .releaseDate(LocalDate.of(2024, 6, 15))
                .status(Movie.MovieStatus.COMING_SOON)
                .build();
        
        Movie movie5 = Movie.builder()
                .title("Spider-Man: Across The Spider-Verse")
                .description("Miles Morales phiêu lưu xuyên qua đa vũ trụ Spider-Man để cứu lấy các thế giới.")
                .duration(140)
                .posterUrl("/images/spiderman.jpg")
                .releaseDate(LocalDate.of(2024, 7, 1))
                .status(Movie.MovieStatus.COMING_SOON)
                .build();
        
        movieRepository.save(movie1);
        movieRepository.save(movie2);
        movieRepository.save(movie3);
        movieRepository.save(movie4);
        movieRepository.save(movie5);
        
        log.info("Created sample movies");
        
        // Create showtimes for movies (Suất chiếu)
        LocalDateTime now = LocalDateTime.now();
        
        // Movie 1 - Avatar - multiple showtimes
        Showtime showtime1 = Showtime.builder()
                .movie(movie1)
                .room(room1)
                .startTime(now.plusDays(1).withHour(10).withMinute(0))
                .price(new BigDecimal("80000"))
                .build();
        
        Showtime showtime2 = Showtime.builder()
                .movie(movie1)
                .room(room1)
                .startTime(now.plusDays(1).withHour(14).withMinute(0))
                .price(new BigDecimal("80000"))
                .build();
        
        Showtime showtime3 = Showtime.builder()
                .movie(movie1)
                .room(room1)
                .startTime(now.plusDays(1).withHour(18).withMinute(0))
                .price(new BigDecimal("90000"))
                .build();
        
        Showtime showtime4 = Showtime.builder()
                .movie(movie1)
                .room(room2)
                .startTime(now.plusDays(2).withHour(20).withMinute(0))
                .price(new BigDecimal("90000"))
                .build();
        
        // Movie 2 - Dune
        Showtime showtime5 = Showtime.builder()
                .movie(movie2)
                .room(room1)
                .startTime(now.plusDays(1).withHour(12).withMinute(0))
                .price(new BigDecimal("80000"))
                .build();
        
        Showtime showtime6 = Showtime.builder()
                .movie(movie2)
                .room(room2)
                .startTime(now.plusDays(1).withHour(16).withMinute(0))
                .price(new BigDecimal("80000"))
                .build();
        
        Showtime showtime7 = Showtime.builder()
                .movie(movie2)
                .room(room3)
                .startTime(now.plusDays(2).withHour(19).withMinute(0))
                .price(new BigDecimal("120000"))
                .build();
        
        // Movie 3 - Marvels
        Showtime showtime8 = Showtime.builder()
                .movie(movie3)
                .room(room1)
                .startTime(now.plusDays(2).withHour(10).withMinute(0))
                .price(new BigDecimal("80000"))
                .build();
        
        Showtime showtime9 = Showtime.builder()
                .movie(movie3)
                .room(room2)
                .startTime(now.plusDays(2).withHour(14).withMinute(0))
                .price(new BigDecimal("80000"))
                .build();
        
        Showtime showtime10 = Showtime.builder()
                .movie(movie3)
                .room(room3)
                .startTime(now.plusDays(3).withHour(20).withMinute(0))
                .price(new BigDecimal("120000"))
                .build();
        
        showtimeRepository.save(showtime1);
        showtimeRepository.save(showtime2);
        showtimeRepository.save(showtime3);
        showtimeRepository.save(showtime4);
        showtimeRepository.save(showtime5);
        showtimeRepository.save(showtime6);
        showtimeRepository.save(showtime7);
        showtimeRepository.save(showtime8);
        showtimeRepository.save(showtime9);
        showtimeRepository.save(showtime10);
        
        log.info("Created sample showtimes");
        log.info("Data initialization completed!");

        // ===== Quiz Questions =====
        ensureQuizQuestions();
    }

    /**
     * Mỗi lần chạy: xóa hết câu cũ, chỉ giữ ngân hàng toán cực dễ (1+1, 2+2, ...).
     */
    private void ensureQuizQuestions() {
        quizQuestionRepository.deleteAll();
        List<QuizQuestion> questions = List.of(
                q("0 + 1 = ?", "0", "1", "2", "3", "B"),
                q("1 + 0 = ?", "0", "1", "2", "3", "B"),
                q("1 + 1 = ?", "1", "2", "3", "4", "B"),
                q("2 + 2 = ?", "3", "4", "5", "6", "B"),
                q("3 + 3 = ?", "5", "6", "7", "8", "B"),
                q("4 + 4 = ?", "6", "7", "8", "9", "C"),
                q("5 + 5 = ?", "8", "9", "10", "11", "C"),
                q("1 + 2 = ?", "2", "3", "4", "5", "B"),
                q("2 + 3 = ?", "4", "5", "6", "7", "B"),
                q("3 + 4 = ?", "6", "7", "8", "9", "B"),
                q("4 + 5 = ?", "8", "9", "10", "7", "B"),
                q("6 + 4 = ?", "8", "9", "10", "11", "C"),
                q("7 + 3 = ?", "8", "9", "10", "11", "C"),
                q("8 + 2 = ?", "8", "9", "10", "11", "C"),
                q("9 + 1 = ?", "8", "9", "10", "11", "C"),
                q("10 - 1 = ?", "7", "8", "9", "10", "C"),
                q("10 - 3 = ?", "6", "7", "8", "5", "B"),
                q("10 - 5 = ?", "3", "4", "5", "6", "C"),
                q("8 - 2 = ?", "4", "5", "6", "7", "C"),
                q("6 - 3 = ?", "1", "2", "3", "4", "C"),
                q("5 - 2 = ?", "1", "2", "3", "4", "C"),
                q("2 x 1 = ?", "1", "2", "3", "4", "B"),
                q("2 x 2 = ?", "2", "3", "4", "5", "C"),
                q("2 x 3 = ?", "4", "5", "6", "7", "C"),
                q("2 x 4 = ?", "6", "7", "8", "9", "C"),
                q("2 x 5 = ?", "8", "9", "10", "11", "C"),
                q("3 x 2 = ?", "4", "5", "6", "7", "C"),
                q("3 x 3 = ?", "6", "7", "9", "12", "C"),
                q("4 x 2 = ?", "6", "7", "8", "9", "C"),
                q("5 x 2 = ?", "8", "9", "10", "12", "C"),
                q("10 : 2 = ?", "3", "4", "5", "6", "C"),
                q("8 : 2 = ?", "2", "3", "4", "5", "C"),
                q("6 : 2 = ?", "2", "3", "4", "5", "B"),
                q("9 : 3 = ?", "2", "3", "4", "5", "B"),
                q("12 : 3 = ?", "2", "3", "4", "5", "C"),
                q("15 : 3 = ?", "3", "4", "5", "6", "C"),
                q("20 : 4 = ?", "4", "5", "6", "3", "B"),
                q("100 - 50 = ?", "40", "50", "60", "45", "B"),
                q("30 + 20 = ?", "40", "50", "60", "45", "B"),
                q("12 + 8 = ?", "18", "19", "20", "21", "C"),
                q("7 + 8 = ?", "13", "14", "15", "16", "C")
        );
        quizQuestionRepository.saveAll(questions);
        log.info("Quiz bank reset: {} câu toán đơn giản", questions.size());
    }

    private static QuizQuestion q(String question, String a, String b, String c, String d, String correct) {
        return QuizQuestion.builder()
                .question(question)
                .optionA(a).optionB(b).optionC(c).optionD(d)
                .correctAnswer(correct)
                .category("Toán")
                .active(true)
                .build();
    }
}
