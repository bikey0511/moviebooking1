package com.example.doannhom15.service;

import com.example.doannhom15.model.Movie;
import com.example.doannhom15.model.Showtime;
import com.example.doannhom15.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    /** Thời lượng mặc định (phút) khi phim chưa khai báo duration */
    public static final int DEFAULT_MOVIE_DURATION_MINUTES = 120;

    private final ShowtimeRepository showtimeRepository;
    
    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }
    
    public Page<Showtime> getShowtimes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        return showtimeRepository.findAll(pageable);
    }
    
    public Showtime getShowtimeById(Long id) {
        return showtimeRepository.findById(id).orElse(null);
    }
    
    public List<Showtime> getShowtimesByMovie(Long movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }
    
    public List<Showtime> getShowtimesByMovieUpcoming(Long movieId) {
        return showtimeRepository.findByMovieIdAndStartTimeAfter(movieId, LocalDateTime.now());
    }

    /** Suất chiếu của phim trong một ngày (theo calendar); hôm nay chỉ còn giờ chưa qua. */
    public List<Showtime> getShowtimesByMovieOnDate(Long movieId, LocalDate date) {
        if (date == null) {
            return getShowtimesByMovieUpcoming(movieId);
        }
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        List<Showtime> list = showtimeRepository
                .findByMovieIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(movieId, dayStart, dayEnd);
        LocalDateTime now = LocalDateTime.now();
        if (date.equals(LocalDate.now())) {
            // Nếu suất được tạo từ input `datetime-local` (độ chính xác theo phút, seconds=0)
            // thì khi user mở trang vài giây sau, suất có thể bị lọc vì startTime < now.
            // Nới điều kiện để vẫn hiển thị suất vừa đến trong khoảng 1 phút đầu.
            LocalDateTime bufferStart = now.minusMinutes(1);
            return list.stream().filter(s -> !s.getStartTime().isBefore(bufferStart)).toList();
        }
        return list;
    }

    public List<Showtime> getShowtimesForCalendarDay(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        return showtimeRepository.findByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(dayStart, dayEnd);
    }
    
    public List<Showtime> getShowtimesByRoom(Long roomId) {
        return showtimeRepository.findByRoomId(roomId);
    }
    
    private int movieDurationMinutes(Movie movie) {
        if (movie == null || movie.getDuration() == null || movie.getDuration() <= 0) {
            return DEFAULT_MOVIE_DURATION_MINUTES;
        }
        return movie.getDuration();
    }

    private LocalDateTime showtimeEnd(Showtime s) {
        return s.getStartTime().plusMinutes(movieDurationMinutes(s.getMovie()));
    }

    /**
     * Kiểm tra: giờ chiếu phải trong tương lai; cùng phòng không được chồng lên khoảng [bắt đầu, bắt đầu+thời lượng phim).
     *
     * @param excludeShowtimeId khi sửa suất (nếu có), bỏ qua chính nó; null khi tạo mới
     */
    public void validateNewShowtime(Long roomId, LocalDateTime startTime, Movie movie, Long excludeShowtimeId) {
        if (startTime == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày giờ chiếu.");
        }
        if (!startTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ngày giờ chiếu phải ở tương lai, không được đặt trong quá khứ.");
        }
        LocalDateTime newEnd = startTime.plusMinutes(movieDurationMinutes(movie));
        for (Showtime ex : showtimeRepository.findByRoomId(roomId)) {
            if (excludeShowtimeId != null && excludeShowtimeId.equals(ex.getId())) {
                continue;
            }
            LocalDateTime exStart = ex.getStartTime();
            LocalDateTime exEnd = showtimeEnd(ex);
            if (startTime.isBefore(exEnd) && exStart.isBefore(newEnd)) {
                throw new IllegalArgumentException(
                        "Phòng này đã có suất chiếu khác trùng hoặc chồng thời gian. Chọn giờ khác hoặc phòng khác.");
            }
        }
    }

    @Transactional
    public Showtime createShowtime(Showtime showtime) {
        return showtimeRepository.save(showtime);
    }
    
    @Transactional
    public Showtime updateShowtime(Showtime showtime) {
        return showtimeRepository.save(showtime);
    }
    
    @Transactional
    public void deleteShowtime(Long id) {
        showtimeRepository.deleteById(id);
    }
    
    public long count() {
        return showtimeRepository.count();
    }
}
