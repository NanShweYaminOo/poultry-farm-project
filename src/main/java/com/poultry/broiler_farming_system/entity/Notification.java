package com.poultry.broiler_farming_system.entity;

import com.poultry.broiler_farming_system.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    @ToString.Exclude
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Column(name = "title_en", nullable = false, length = 255)
    private String titleEn;

    @Column(name = "title_my", nullable = false, length = 255)
    private String titleMy;

    @Lob
    @Column(name = "message_en", nullable = false, columnDefinition = "TEXT")
    private String messageEn;

    @Lob
    @Column(name = "message_my", nullable = false, columnDefinition = "TEXT")
    private String messageMy;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    // Deliberately not a foreign key, same rationale as SystemLog.targetType/
    // targetId -- a notification must survive the deletion of the row it
    // refers to (e.g. a batch getting deleted), so this is just a plain id
    // reference for the frontend to build a link from, not a managed
    // relationship.
    @Column(name = "related_entity_type", length = 40)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
