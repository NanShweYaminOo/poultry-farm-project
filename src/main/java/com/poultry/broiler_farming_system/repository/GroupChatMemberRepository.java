package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.GroupChatMember;
import com.poultry.broiler_farming_system.entity.GroupChatMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupChatMemberRepository extends JpaRepository<GroupChatMember, GroupChatMemberId> {

    long countByIdGroupChatId(Long groupChatId);

    List<GroupChatMember> findByIdGroupChatId(Long groupChatId);
}
