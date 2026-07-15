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
import com.poultry.broiler_farming_system.service.notification.NotificationService;
import com.poultry.broiler_farming_system.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private static final String POSTING_EXTENSION_DURATION_MONTHS_KEY = "posting_extension_duration_months";
    private static final String BATCH_REGISTRATION_FEE_KEY = "batch_registration_fee";
    private static final String POSTING_EXTENSION_FEE_KEY = "posting_extension_fee";

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    @Override
    public PaymentTransactionResponse createTransaction(
            Long userId, CreatePaymentTransactionRequest request, MultipartFile screenshot) {
        if (request.paymentType() == null) {
            throw new IllegalArgumentException("paymentType is required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " was not found."));
        Batch batch = batchRepository.findById(request.batchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch " + request.batchId() + " was not found."));

        if (!batch.getFarmer().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You are not the farmer on batch " + batch.getId() + "; payment rejected.");
        }

        // store() itself validates the file is present and is an image type.
        String screenshotUrl = fileStorageService.store(screenshot, "payment-screenshots", "payment");

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setUser(user);
        transaction.setBatch(batch);
        transaction.setPaymentType(request.paymentType());
        transaction.setScreenshotUrl(screenshotUrl);
        transaction.setTransactionTimestamp(LocalDateTime.now());
        // Left null (not blocked/rejected) if the admin hasn't configured this
        // fee yet -- see the field's own Javadoc on PaymentTransaction.amount.
        transaction.setAmount(readFeeForType(request.paymentType()));
        // status starts PENDING via the entity's own field default.

        PaymentTransaction saved = paymentTransactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Override
    public List<PaymentTransactionResponse> listMyTransactions(Long userId) {
        return paymentTransactionRepository.findByUserIdOrderByTransactionTimestampDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<PaymentTransactionResponse> listForAdmin() {
        return paymentTransactionRepository.findAllByOrderByTransactionTimestampDesc().stream()
                .map(this::toResponse)
                .toList();
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

        // Same transaction as the review itself -- see NotificationService's
        // Javadoc: a "payment approved/rejected" notification should only
        // exist if the review actually committed.
        if (request.decision() == PaymentStatus.APPROVED) {
            notificationService.notifyPaymentApproved(saved);
        } else {
            notificationService.notifyPaymentRejected(saved);
        }

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

    private BigDecimal readFeeForType(PaymentType paymentType) {
        String key = paymentType == PaymentType.BATCH_REGISTRATION
                ? BATCH_REGISTRATION_FEE_KEY
                : POSTING_EXTENSION_FEE_KEY;
        return systemConfigurationRepository.findByConfigKey(key)
                .map(SystemConfiguration::getConfigValue)
                .flatMap(this::parseAmount)
                .orElse(null);
    }

    private Optional<BigDecimal> parseAmount(String raw) {
        try {
            return Optional.of(new BigDecimal(raw.trim()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
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
                transaction.getUser().getUsername(),
                transaction.getBatch().getId(),
                transaction.getBatch().getBatchName(),
                transaction.getPaymentType(),
                transaction.getScreenshotUrl(),
                transaction.getStatus(),
                transaction.getTransactionTimestamp(),
                transaction.getReviewedByAdmin() != null ? transaction.getReviewedByAdmin().getId() : null,
                transaction.getReviewedAt(),
                transaction.getAmount()
        );
    }
}
