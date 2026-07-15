package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.dto.analytics.LocationUserCountRow;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// JpaSpecificationExecutor backs AdminUserServiceImpl's dynamic filtering
// (role/accountType/isFlaggedForReview/isBanned/location, any combination of
// which may be absent) -- the first pagination/dynamic-query use case in
// this codebase, see AdminUserServiceImpl.buildSpecification.
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    List<User> findByPostingExtensionExpiryIsNotNull();

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRole(UserRole role);

    // Used to pick an owner for the single, system-wide group chat when it's
    // auto-provisioned on first access -- see GroupChatServiceImpl.
    Optional<User> findFirstByRole(UserRole role);

    // location is free text captured at registration -- normalized
    // (UPPER+TRIM, blank/null -> 'UNSPECIFIED') at the query level so
    // "Yangon", "yangon ", and null all bucket together instead of
    // fragmenting the breakdown. DTO projection, no full-entity load.
    @Query("SELECT new com.poultry.broiler_farming_system.dto.analytics.LocationUserCountRow(" +
            "COALESCE(NULLIF(UPPER(TRIM(u.location)), ''), 'UNSPECIFIED'), u.accountType, u.role, COUNT(u)) " +
            "FROM User u " +
            "GROUP BY COALESCE(NULLIF(UPPER(TRIM(u.location)), ''), 'UNSPECIFIED'), u.accountType, u.role")
    List<LocationUserCountRow> countUsersByLocationAccountTypeAndRole();
}
