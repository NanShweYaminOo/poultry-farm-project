package com.poultry.broiler_farming_system.dto.payment;

import com.poultry.broiler_farming_system.entity.enums.PaymentType;

// userId is NOT a field here -- it's the authenticated caller, taken from
// the security context in PaymentTransactionController. Letting the client
// name an arbitrary userId would let anyone submit a payment "as" a
// different farmer for a batch they merely guessed the id of.
// transactionTimestamp is not accepted from the client either -- it's
// stamped server-side at submission time, same as every other
// "action happened now" field in this system (start/stop batch, task
// completion, etc).
public record CreatePaymentTransactionRequest(
        Long batchId,
        PaymentType paymentType,
        String screenshotUrl
) {
}
