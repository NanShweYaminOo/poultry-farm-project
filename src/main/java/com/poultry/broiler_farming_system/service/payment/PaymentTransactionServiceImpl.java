package com.poultry.broiler_farming_system.service.payment;

import com.poultry.broiler_farming_system.dto.payment.CreatePaymentTransactionRequest;
import com.poultry.broiler_farming_system.dto.payment.PaymentTransactionResponse;
import com.poultry.broiler_farming_system.dto.payment.ReviewPaymentRequest;
import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.PaymentTransaction;
import com.poultry.broiler_farming_system.entity.SystemConfiguration;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.PaymentStatus;
import com.poultry.broiler_farming_system.entity.enums.PaymentType;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.InvalidPaymentStateException;
import com.poultry.broiler_farming_system.exception.MissingSystemConfigurationException;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.repository.PaymentTransactionRepository;
import com.poultry.broiler_farming_system.repository.SystemConfigurationRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private static final String POSTING_EXTENSION_DURATION_MONTHS_KEY = "posting_extension_duration_months";

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;

    @Override
    public PaymentTransactionResponse createTransaction(Long userId, CreatePaymentTransactionRequest request) {
        if (request.paymentType() == null) {
            throw new IllegalArgumentException("paymentType is required.");
        }
        if (!StringUtils.hasText(request.screenshotUrl())) {
            throw new IllegalArgumentException("screenshotUrl is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " was not found."));
        Batch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + request.batchId() + " was not found."));

        if (!batch.getFarmer().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You are not the farmer on batch " + batch.getId() + "; payment rejected.");
        }

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setUser(user);
        transaction.setBatch(batch);
        transaction.setPaymentType(request.paymentType());
        transaction.setScreenshotUrl(request.screenshotUrl().trim());
        transaction.setTransactionTimestamp(LocalDateTime.now());
        // status starts PENDING via the entity's own field default.

        PaymentTransaction saved = paymentTransactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Override
    public PaymentTransactionResponse reviewPayment(
            Long paymentTransactionId, Long adminId, ReviewPaymentRequest request) {
        if (request.decision() != PaymentStatus.APPROVED && request.decision() != PaymentStatus.REJECTED) {
            throw new IllegalArgumentException("decision must be APPROVED or REJECTED.");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user " + adminId + " was not found."));
        // SecurityConfig already restricts this endpoint to ROLE_ADMIN; this
        // is a cheap defense-in-depth re-check, not the primary gate.
        if (admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException(
                    "User " + admin.getId() + " is not an Admin and cannot review payment transactions.");
        }

        PaymentTransaction transaction = paymentTransactionRepository.findById(paymentTransactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment transaction " + paymentTransactionId + " was not found."));
        if (transaction.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Payment transaction " + paymentTransactionId + " has already been " + transaction.getStatus() + ".");
        }

        LocalDateTime reviewedAt = LocalDateTime.now();
        transaction.setStatus(request.decision());
        transaction.setReviewedByAdmin(admin);
        transaction.setReviewedAt(reviewedAt);

        if (request.decision() == PaymentStatus.APPROVED) {
            applyApprovalEffect(transaction, reviewedAt);
        }

        PaymentTransaction saved = paymentTransactionRepository.save(transaction);
        return toResponse(saved);
    }

    private void applyApprovalEffect(PaymentTransaction transaction, LocalDateTime reviewedAt) {
        if (transaction.getPaymentType() == PaymentType.BATCH_REGISTRATION) {
            // Only unlocks "Start Batch" -- the farming cycle clock itself
            // starts later, when the farmer explicitly starts the batch.
            Batch batch = transaction.getBatch();
            batch.setAdminApprovedAt(reviewedAt);

            // The role-based security gate on PAID-only endpoints
            // (startBatch, medicine estimation, alarms, inventory) only
            // works if a farmer actually becomes PAID at some point --
            // this is that promotion. Never downgrades; only escalates.
            User farmer = batch.getFarmer();
            if (farmer.getRole() == UserRole.FREE) {
                farmer.setRole(UserRole.PAID);
            }
        } else if (transaction.getPaymentType() == PaymentType.POSTING_EXTENSION) {
            // Counts down from this exact approval timestamp, not from
            // whenever the farmer originally uploaded the screenshot.
            int durationMonths = readPostingExtensionDurationMonths();
            User farmer = transaction.getUser();
            farmer.setPostingExtensionExpiry(reviewedAt.plusMonths(durationMonths));
        }
    }

    private int readPostingExtensionDurationMonths() {
        String raw = systemConfigurationRepository.findByConfigKey(POSTING_EXTENSION_DURATION_MONTHS_KEY)
                .map(SystemConfiguration::getConfigValue)
                .orElseThrow(() -> new MissingSystemConfigurationException(
                        "Admin has not configured '" + POSTING_EXTENSION_DURATION_MONTHS_KEY
                                + "' yet. Set this value in System Configurations before approving posting extensions."));
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new MissingSystemConfigurationException(
                    "Configured value for '" + POSTING_EXTENSION_DURATION_MONTHS_KEY + "' is not a valid integer: '" + raw + "'.");
        }
    }

    private PaymentTransactionResponse toResponse(PaymentTransaction transaction) {
        return new PaymentTransactionResponse(
                transaction.getId(),
                transaction.getUser().getId(),
                transaction.getBatch().getId(),
                transaction.getPaymentType(),
                transaction.getScreenshotUrl(),
                transaction.getStatus(),
                transaction.getTransactionTimestamp(),
                transaction.getReviewedByAdmin() != null ? transaction.getReviewedByAdmin().getId() : null,
                transaction.getReviewedAt()
        );
    }
}
