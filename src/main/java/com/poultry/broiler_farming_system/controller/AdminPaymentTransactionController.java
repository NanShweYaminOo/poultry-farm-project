package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.payment.PaymentTransactionResponse;
import com.poultry.broiler_farming_system.service.payment.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Admin-only view over every payment transaction. Access is restricted to
// ROLE_ADMIN by the existing "/api/v1/admin/**" rule in SecurityConfig.
// Approval/rejection stays on the existing
// POST /api/v1/payment-transactions/{id}/review endpoint (PaymentTransactionController).
@RestController
@RequestMapping("/api/v1/admin/payment-transactions")
@RequiredArgsConstructor
public class AdminPaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    @GetMapping
    public List<PaymentTransactionResponse> listAll() {
        return paymentTransactionService.listForAdmin();
    }
}
