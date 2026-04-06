package com.example.doannhom15.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "concession_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcessionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    /** URL hình ảnh (admin upload hoặc link) */
    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConcessionType type;

    private boolean active = true;

    public enum ConcessionType {
        BAP,    // Bắp
        NUOC,   // Nước
        COMBO   // Combo bắp + nước
    }
}
