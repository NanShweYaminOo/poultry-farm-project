package com.poultry.broiler_farming_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_alarms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BatchAlarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    @ToString.Exclude
    private Batch batch;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(name = "cron_expression", nullable = false, length = 100)
    private String cronExpression;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "calculated_requirement", precision = 12, scale = 2)
    private BigDecimal calculatedRequirement;

    @Column(name = "overridden_quantity", precision = 12, scale = 2)
    private BigDecimal overriddenQuantity;

    @Column(name = "final_cost_incurred", precision = 12, scale = 2)
    private BigDecimal finalCostIncurred;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Optional 1:1 link to the expense record generated from this alarm.
    @OneToOne(mappedBy = "batchAlarm", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private BatchExpense expense;
}
