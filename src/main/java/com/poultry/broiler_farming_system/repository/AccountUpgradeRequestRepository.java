package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.AccountUpgradeRequest;
import com.poultry.broiler_farming_system.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountUpgradeRequestRepository extends JpaRepository<AccountUpgradeRequest, Long> {

    List<AccountUpgradeRequest> findByRequestedByIdOrderByRequestedAtDesc(Long userId);

    List<AccountUpgradeRequest> findAllByOrderByRequestedAtDesc();

    Optional<AccountUpgradeRequest> findFirstByRequestedByIdAndStatus(Long userId, RequestStatus status);
}
