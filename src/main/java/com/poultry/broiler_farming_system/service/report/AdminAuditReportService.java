package com.poultry.broiler_farming_system.service.report;

import com.poultry.broiler_farming_system.dto.report.AdminAuditFarmerBreakdownRow;
import com.poultry.broiler_farming_system.dto.report.AdminAuditRevenueByTypeRow;
import com.poultry.broiler_farming_system.dto.report.AdminAuditStatusCountRow;
import com.poultry.broiler_farming_system.entity.enums.PaymentStatus;
import com.poultry.broiler_farming_system.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// System-wide financial audit for a given [from, to] date range: revenue by
// payment type, approval/rejection rates, and a per-farmer breakdown --
// entirely SQL-side aggregation (see PaymentTransactionRepository's
// constructor-expression @Query methods), never a full entity scan.
@Service
@RequiredArgsConstructor
public class AdminAuditReportService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PdfGenerationService pdfGenerationService;

    @Transactional(readOnly = true)
    public void writeReport(LocalDate from, LocalDate to, OutputStream outputStream) {
        LocalDateTime fromInclusive = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();

        List<AdminAuditRevenueByTypeRow> revenueByType =
                paymentTransactionRepository.revenueByTypeBetween(fromInclusive, toExclusive);
        List<AdminAuditStatusCountRow> statusCounts =
                paymentTransactionRepository.statusCountsBetween(fromInclusive, toExclusive);
        List<AdminAuditFarmerBreakdownRow> farmerBreakdown =
                paymentTransactionRepository.farmerBreakdownBetween(fromInclusive, toExclusive);

        Map<PaymentStatus, Long> countByStatus = new HashMap<>();
        statusCounts.forEach(row -> countByStatus.put(row.status(), row.count()));
        long approvedCount = countByStatus.getOrDefault(PaymentStatus.APPROVED, 0L);
        long rejectedCount = countByStatus.getOrDefault(PaymentStatus.REJECTED, 0L);
        long pendingCount = countByStatus.getOrDefault(PaymentStatus.PENDING, 0L);
        long decidedCount = approvedCount + rejectedCount;

        BigDecimal approvalRatePercent = ratePercent(approvedCount, decidedCount);
        BigDecimal rejectionRatePercent = ratePercent(rejectedCount, decidedCount);

        BigDecimal totalRevenue = revenueByType.stream()
                .map(AdminAuditRevenueByTypeRow::totalRevenue)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> model = new HashMap<>();
        model.put("fromDate", from);
        model.put("toDate", to);
        model.put("generatedAt", LocalDateTime.now());
        model.put("revenueByType", revenueByType);
        model.put("totalRevenue", totalRevenue);
        model.put("approvedCount", approvedCount);
        model.put("rejectedCount", rejectedCount);
        model.put("pendingCount", pendingCount);
        model.put("approvalRatePercent", approvalRatePercent);
        model.put("rejectionRatePercent", rejectionRatePercent);
        model.put("farmerBreakdown", farmerBreakdown);

        pdfGenerationService.render("reports/admin-audit", model, outputStream);
    }

    private BigDecimal ratePercent(long numerator, long denominator) {
        if (denominator == 0) {
            return null;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
    }
}
