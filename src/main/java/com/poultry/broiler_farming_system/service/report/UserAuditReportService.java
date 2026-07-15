package com.poultry.broiler_farming_system.service.report;

import com.poultry.broiler_farming_system.dto.report.UserAuditExpenseRow;
import com.poultry.broiler_farming_system.dto.report.UserAuditPaymentRow;
import com.poultry.broiler_farming_system.entity.Batch;
import com.poultry.broiler_farming_system.entity.BatchAlarm;
import com.poultry.broiler_farming_system.entity.BatchExpense;
import com.poultry.broiler_farming_system.entity.PaymentTransaction;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.BatchExpenseRepository;
import com.poultry.broiler_farming_system.repository.BatchRepository;
import com.poultry.broiler_farming_system.repository.PaymentTransactionRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// A farmer's own financial audit: their payment history (batch
// registrations + posting extensions) plus a per-expense breakdown that
// surfaces the variance between what the system calculated a medicine task
// would need and what the farmer actually recorded using -- that variance is
// the entire point of this report, per spec. Any authenticated user may pull
// their own report; there is no cross-user access here (farmerId always
// comes from the caller's own principal, never client input -- see
// ReportController).
@Service
@RequiredArgsConstructor
public class UserAuditReportService {

    private final UserRepository userRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BatchRepository batchRepository;
    private final BatchExpenseRepository batchExpenseRepository;
    private final PdfGenerationService pdfGenerationService;

    @Transactional(readOnly = true)
    public void writeReport(Long farmerId, OutputStream outputStream) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + farmerId + " was not found."));

        List<UserAuditPaymentRow> payments = paymentTransactionRepository
                .findByUserIdOrderByTransactionTimestampDesc(farmerId).stream()
                .map(this::toPaymentRow)
                .toList();

        List<Batch> batches = batchRepository.findByFarmerIdOrderByCreatedDateDesc(farmerId);
        List<UserAuditExpenseRow> expenses = batches.stream()
                .flatMap(batch -> batchExpenseRepository.findByBatchIdOrderByExpenseDateDesc(batch.getId()).stream()
                        .map(expense -> toExpenseRow(batch, expense)))
                .toList();

        BigDecimal totalPaid = payments.stream()
                .filter(row -> "APPROVED".equals(row.status()) && row.amount() != null)
                .map(UserAuditPaymentRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenses.stream()
                .map(UserAuditExpenseRow::finalCostIncurred)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> model = new HashMap<>();
        model.put("farmerName", farmer.getFullName());
        model.put("farmerUsername", farmer.getUsername());
        model.put("generatedAt", LocalDateTime.now());
        model.put("payments", payments);
        model.put("expenses", expenses);
        model.put("totalPaid", totalPaid);
        model.put("totalExpenses", totalExpenses);

        pdfGenerationService.render("reports/user-audit", model, outputStream);
    }

    private UserAuditPaymentRow toPaymentRow(PaymentTransaction transaction) {
        return new UserAuditPaymentRow(
                transaction.getBatch().getBatchName(),
                transaction.getPaymentType().name(),
                transaction.getStatus().name(),
                transaction.getAmount(),
                transaction.getTransactionTimestamp());
    }

    private UserAuditExpenseRow toExpenseRow(Batch batch, BatchExpense expense) {
        BatchAlarm alarm = expense.getBatchAlarm();
        BigDecimal calculatedRequirement = alarm != null ? alarm.getCalculatedRequirement() : null;
        BigDecimal overriddenQuantity = alarm != null ? alarm.getOverriddenQuantity() : null;
        BigDecimal quantityVariance = (calculatedRequirement != null && overriddenQuantity != null)
                ? overriddenQuantity.subtract(calculatedRequirement)
                : null;

        return new UserAuditExpenseRow(
                batch.getBatchName(),
                expense.getDescription(),
                calculatedRequirement,
                overriddenQuantity,
                quantityVariance,
                expense.getAmount(),
                expense.getExpenseDate());
    }
}
