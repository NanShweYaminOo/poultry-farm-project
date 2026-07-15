package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    // Ownership-checked lookup for markAsRead -- a user can only ever load
    // their own notification row, never another user's by guessing an id.
    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);
}
