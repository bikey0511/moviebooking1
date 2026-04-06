package com.example.doannhom15.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private Integer duration;
    
    private String posterUrl;
    
    @Column(name = "trailer_url")
    private String trailerUrl;
    
    private LocalDate releaseDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;
    
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Showtime> showtimes = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = MovieStatus.NOW_SHOWING;
        }
    }
    
    public enum MovieStatus {
        NOW_SHOWING, COMING_SOON
    }
}
