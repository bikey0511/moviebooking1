package com.example.doannhom15.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cinema_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    /** Phòng thường (120 chỗ, 5 ghế đôi hàng đầu) hoặc VIP (40 ghế nằm) */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoomType roomType;
    
    private Integer totalSeats;
    
    public enum RoomType {
        NORMAL,  // 120 chỗ, 5 ghế đôi trên cùng
        VIP      // 40 ghế nằm
    }
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (totalSeats == null) {
            totalSeats = 0;
        }
    }
}
