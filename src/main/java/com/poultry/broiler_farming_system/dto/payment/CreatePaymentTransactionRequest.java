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
// screenshotUrl is NOT a field here either -- the farmer uploads the
// screenshot as an image file (see PaymentTransactionController), and the
// URL is computed server-side by FileStorageService once it's stored.
// Bound via @ModelAttribute from multipart/form-data fields (sent alongside
// the image file), not a JSON @RequestBody -- a request can't mix a JSON
// body with a file part.
public record CreatePaymentTransactionRequest(
        Long batchId,
        PaymentType paymentType
) {
}
