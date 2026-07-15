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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-transactions")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    // userId is the authenticated caller -- see CreatePaymentTransactionRequest.
    // multipart/form-data: batchId, paymentType fields + a "screenshot" image file part.
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentTransactionResponse create(
            @AuthenticationPrincipal UserPrincipal principal,
            @ModelAttribute CreatePaymentTransactionRequest request,
            @RequestParam("screenshot") MultipartFile screenshot) {
        return paymentTransactionService.createTransaction(principal.getId(), request, screenshot);
    }

    @GetMapping("/me")
    public List<PaymentTransactionResponse> listMine(@AuthenticationPrincipal UserPrincipal principal) {
        return paymentTransactionService.listMyTransactions(principal.getId());
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
