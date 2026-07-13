package com.poultry.broiler_farming_system.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GroupChatMemberId implements Serializable {

    private Long groupChatId;

    private Long userId;
}
