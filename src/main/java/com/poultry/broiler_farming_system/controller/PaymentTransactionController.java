package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.payment.CreatePaymentTransactionRequest;
import com.poultry.broiler_farming_system.dto.payment.PaymentTransactionResponse;
import com.poultry.broiler_farming_system.dto.payment.ReviewPaymentRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.payment.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-transactions")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    // userId is the authenticated caller -- see CreatePaymentTransactionRequest.
    // e.g. { "batchId": 12, "paymentType": "BATCH_REGISTRATION", "screenshotUrl": "https://..." }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentTransactionResponse create(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody CreatePaymentTransactionRequest request) {
        return paymentTransactionService.createTransaction(principal.getId(), request);
    }

    // adminId is the authenticated caller (SecurityConfig requires ROLE_ADMIN
    // here) -- see ReviewPaymentRequest.
    // e.g. { "decision": "APPROVED" } or { "decision": "REJECTED" }
    @PostMapping("/{paymentTransactionId}/review")
    public PaymentTransactionResponse review(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long paymentTransactionId,
            @RequestBody ReviewPaymentRequest request) {
        return paymentTransactionService.reviewPayment(paymentTransactionId, principal.getId(), request);
    }
}
