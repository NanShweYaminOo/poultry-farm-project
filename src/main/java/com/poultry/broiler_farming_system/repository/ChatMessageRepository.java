package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    void deleteBySenderIdOrReceiverId(Long senderId, Long receiverId);

    // Full history between exactly two users, oldest first.
    @Query("SELECT m FROM ChatMessage m WHERE " +
            "(m.sender.id = :userA AND m.receiver.id = :userB) OR " +
            "(m.sender.id = :userB AND m.receiver.id = :userA) " +
            "ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversation(@Param("userA") Long userA, @Param("userB") Long userB);

    // Every message this user has sent or received, newest first --
    // ChatMessageServiceImpl.listConversations groups these by "other party"
    // in Java rather than a GROUP BY query; conversation volume for this app
    // doesn't warrant a more complex aggregate query.
    List<ChatMessage> findBySenderIdOrReceiverIdOrderBySentAtDesc(Long senderId, Long receiverId);

    long countByReceiverIdAndSenderIdAndIsReadFalse(Long receiverId, Long senderId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.receiver.id = :receiverId AND m.sender.id = :senderId AND m.isRead = false")
    void markConversationAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
}
