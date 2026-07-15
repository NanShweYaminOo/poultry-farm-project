package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {

    // Used by AdminUserServiceImpl.deleteUser's cleanup cascade. Always
    // empty for a deletable target in practice: the system-wide shared
    // group's createdBy is always an ADMIN account (see
    // GroupChatServiceImpl), and admins can never be deleted through that
    // endpoint (requireNonAdminTarget) -- so the shared group can never be
    // wiped out as a side effect of deleting some other user.
    List<GroupChat> findByCreatedById(Long createdById);

    // The whole system has exactly one group chat (see GroupChatServiceImpl's
    // Javadoc) -- this deterministically picks the same row every time
    // (oldest by id) rather than relying on findAll()'s unspecified order,
    // so a stray duplicate row can never get "created" by two callers
    // resolving to different rows.
    Optional<GroupChat> findFirstByOrderByIdAsc();
}
