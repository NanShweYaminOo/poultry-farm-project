package com.poultry.broiler_farming_system.entity;

import com.poultry.broiler_farming_system.entity.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// A Guest account's request to become a Farmer. Purely an approval
// workflow -- no payment involved (see PaymentTransaction for the
// pay-gated flows). Approval flips requestedBy.accountType to FARMER; see
// AccountUpgradeRequestServiceImpl.applyApprovalEffect.
@Entity
@Table(name = "account_upgrade_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AccountUpgradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    @ToString.Exclude
    private User requestedBy;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_admin_id")
    @ToString.Exclude
    private User reviewedByAdmin;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Lob
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;
}
