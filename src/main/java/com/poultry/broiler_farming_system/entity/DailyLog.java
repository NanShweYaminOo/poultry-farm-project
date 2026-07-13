package com.poultry.broiler_farming_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_logs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_logs_batch_date", columnNames = {"batch_id", "log_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    @ToString.Exclude
    private Batch batch;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "daily_mortality_count", nullable = false)
    @Builder.Default
    private Integer dailyMortalityCount = 0;

    @Column(name = "total_remaining_chicken_count", nullable = false)
    private Integer totalRemainingChickenCount;
}
