package com.poultry.broiler_farming_system.entity;

import com.poultry.broiler_farming_system.entity.enums.SystemLogAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // AdminUserServiceImpl.deleteUser() refuses to delete ADMIN accounts, so
    // this FK never dangles.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    @ToString.Exclude
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SystemLogAction action;

    @Column(name = "target_type", nullable = false, length = 40)
    private String targetType;

    // Deliberately not a foreign key -- a log entry must survive the
    // deletion of the row it describes (e.g. DELETE_USER), so this is just a
    // plain id reference for display, not a managed relationship.
    @Column(name = "target_id")
    private Long targetId;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
