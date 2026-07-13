package com.poultry.broiler_farming_system.entity;

import com.poultry.broiler_farming_system.entity.enums.BreedStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "breeds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Breed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String origin;

    @Column(name = "avg_market_weight_kg", precision = 6, scale = 2)
    private BigDecimal avgMarketWeightKg;

    @Column(name = "growth_period_days")
    private Integer growthPeriodDays;

    @Column(precision = 6, scale = 2)
    private BigDecimal fcr;

    @Column(length = 2000)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BreedStatus status = BreedStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
