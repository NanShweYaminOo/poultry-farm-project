package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.GroupChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long> {

    List<GroupChatMessage> findByGroupChatIdOrderBySentAtAsc(Long groupChatId);

    long countByGroupChatId(Long groupChatId);

    Optional<GroupChatMessage> findTopByGroupChatIdOrderBySentAtDesc(Long groupChatId);

    void deleteBySenderId(Long senderId);
}
