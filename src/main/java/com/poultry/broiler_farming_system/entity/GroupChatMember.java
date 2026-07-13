package com.poultry.broiler_farming_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_chat_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GroupChatMember {

    @EqualsAndHashCode.Include
    @EmbeddedId
    @Builder.Default
    private GroupChatMemberId id = new GroupChatMemberId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("groupChatId")
    @JoinColumn(name = "group_chat_id", nullable = false)
    @ToString.Exclude
    private GroupChat groupChat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;
}
