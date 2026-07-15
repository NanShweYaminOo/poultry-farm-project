package com.poultry.broiler_farming_system.service.payment;

import com.poultry.broiler_farming_system.dto.payment.CreatePaymentTransactionRequest;
import com.poultry.broiler_farming_system.dto.payment.PaymentTransactionResponse;
import com.poultry.broiler_farming_system.dto.payment.ReviewPaymentRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PaymentTransactionService {

    // Farmer uploads a payment screenshot (batch registration fee or
    // posting-extension fee) as an image file. Always lands as PENDING;
    // review is separate.
    PaymentTransactionResponse createTransaction(Long userId, CreatePaymentTransactionRequest request, MultipartFile screenshot);

    // The caller's own payment transactions, most recent first.
    List<PaymentTransactionResponse> listMyTransactions(Long userId);

    // Every payment transaction in the system, most recent first -- for the
    // admin review screen.
    List<PaymentTransactionResponse> listForAdmin();

    /**
     * Admin approves or rejects a pending payment screenshot. On approval,
     * branches by payment_type:
     *  - BATCH_REGISTRATION: stamps batches.admin_approved_at = now() and,
     *    if the farmer is still FREE, promotes them to PAID. This only
     *    unlocks the "Start Batch" action -- the farming cycle clock itself
     *    does not start here (see BatchService.startBatch).
     *  - POSTING_EXTENSION: stamps users.posting_extension_expiry = now() +
     *    the Admin's configured extension duration, counting down from this
     *    exact approval timestamp.
     * Rejection just records the review with no further side effects.
     */
    PaymentTransactionResponse reviewPayment(Long paymentTransactionId, Long adminId, ReviewPaymentRequest request);
}
