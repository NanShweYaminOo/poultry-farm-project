package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.UserWarning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserWarningRepository extends JpaRepository<UserWarning, Long> {

    List<UserWarning> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    void deleteByRecipientId(Long recipientId);
}
