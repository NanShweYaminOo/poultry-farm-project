package com.poultry.broiler_farming_system.service.accountupgrade;

import com.poultry.broiler_farming_system.dto.accountupgrade.AccountUpgradeRequestResponse;
import com.poultry.broiler_farming_system.dto.accountupgrade.CreateAccountUpgradeRequestRequest;
import com.poultry.broiler_farming_system.dto.accountupgrade.ReviewAccountUpgradeRequestRequest;
import com.poultry.broiler_farming_system.entity.AccountUpgradeRequest;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.AccountType;
import com.poultry.broiler_farming_system.entity.enums.RequestStatus;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.DuplicateResourceException;
import com.poultry.broiler_farming_system.exception.InvalidRequestStateException;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.AccountUpgradeRequestRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountUpgradeRequestServiceImpl implements AccountUpgradeRequestService {

    private final AccountUpgradeRequestRepository accountUpgradeRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public AccountUpgradeRequestResponse create(Long userId, CreateAccountUpgradeRequestRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " was not found."));

        if (user.getAccountType() == AccountType.FARMER) {
            throw new IllegalArgumentException("User " + userId + " is already a Farmer account.");
        }
        accountUpgradeRequestRepository.findFirstByRequestedByIdAndStatus(userId, RequestStatus.PENDING)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "User " + userId + " already has a pending upgrade request (id " + existing.getId() + ").");
                });

        AccountUpgradeRequest upgradeRequest = new AccountUpgradeRequest();
        upgradeRequest.setRequestedBy(user);
        upgradeRequest.setReason(request != null ? request.reason() : null);
        // status starts PENDING via the entity's own field default.

        AccountUpgradeRequest saved = accountUpgradeRequestRepository.save(upgradeRequest);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountUpgradeRequestResponse> listMine(Long userId) {
        return accountUpgradeRequestRepository.findByRequestedByIdOrderByRequestedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountUpgradeRequestResponse> listForAdmin() {
        return accountUpgradeRequestRepository.findAllByOrderByRequestedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AccountUpgradeRequestResponse review(Long requestId, Long adminId, ReviewAccountUpgradeRequestRequest request) {
        if (request.decision() != RequestStatus.APPROVED && request.decision() != RequestStatus.REJECTED) {
            throw new IllegalArgumentException("decision must be APPROVED or REJECTED.");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user " + adminId + " was not found."));
        // SecurityConfig already restricts this endpoint to ROLE_ADMIN; this
        // is a cheap defense-in-depth re-check, not the primary gate.
        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException(
                    "User " + admin.getId() + " is not an Admin and cannot review upgrade requests.");
        }

        AccountUpgradeRequest upgradeRequest = accountUpgradeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Upgrade request " + requestId + " was not found."));
        if (upgradeRequest.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestStateException(
                    "Upgrade request " + requestId + " has already been " + upgradeRequest.getStatus() + ".");
        }

        LocalDateTime reviewedAt = LocalDateTime.now();
        upgradeRequest.setStatus(request.decision());
        upgradeRequest.setReviewedByAdmin(admin);
        upgradeRequest.setReviewedAt(reviewedAt);
        upgradeRequest.setAdminNote(request.adminNote());

        if (request.decision() == RequestStatus.APPROVED) {
            upgradeRequest.getRequestedBy().setAccountType(AccountType.FARMER);
        }

        AccountUpgradeRequest saved = accountUpgradeRequestRepository.save(upgradeRequest);

        // Same transaction as the review itself, same rationale as
        // PaymentTransactionServiceImpl's notification calls.
        if (request.decision() == RequestStatus.APPROVED) {
            notificationService.notifyAccountUpgradeApproved(saved);
        } else {
            notificationService.notifyAccountUpgradeRejected(saved);
        }

        return toResponse(saved);
    }

    private AccountUpgradeRequestResponse toResponse(AccountUpgradeRequest upgradeRequest) {
        User user = upgradeRequest.getRequestedBy();
        return new AccountUpgradeRequestResponse(
                upgradeRequest.getId(),
                user.getId(),
                user.getUsername(),
                upgradeRequest.getReason(),
                upgradeRequest.getStatus(),
                upgradeRequest.getRequestedAt(),
                upgradeRequest.getReviewedByAdmin() != null ? upgradeRequest.getReviewedByAdmin().getId() : null,
                upgradeRequest.getReviewedAt(),
                upgradeRequest.getAdminNote()
        );
    }
}
