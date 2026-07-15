package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.dto.analytics.LocationRevenueRow;
import com.poultry.broiler_farming_system.dto.report.AdminAuditFarmerBreakdownRow;
import com.poultry.broiler_farming_system.dto.report.AdminAuditRevenueByTypeRow;
import com.poultry.broiler_farming_system.dto.report.AdminAuditStatusCountRow;
import com.poultry.broiler_farming_system.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByUserIdOrderByTransactionTimestampDesc(Long userId);

    List<PaymentTransaction> findAllByOrderByTransactionTimestampDesc();

    // Aggregate DTO projections for AdminAuditReportService -- deliberately
    // group-by/SUM/COUNT in SQL rather than loading every PaymentTransaction
    // entity in the period and aggregating in Java, per the same
    // "don't load full entities to aggregate" principle the location
    // analytics endpoints follow (AdminLocationAnalyticsController).

    @Query("SELECT new com.poultry.broiler_farming_system.dto.report.AdminAuditRevenueByTypeRow(" +
            "pt.paymentType, SUM(pt.amount), COUNT(pt)) " +
            "FROM PaymentTransaction pt " +
            "WHERE pt.status = com.poultry.broiler_farming_system.entity.enums.PaymentStatus.APPROVED " +
            "AND pt.transactionTimestamp >= :from AND pt.transactionTimestamp < :to " +
            "GROUP BY pt.paymentType")
    List<AdminAuditRevenueByTypeRow> revenueByTypeBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT new com.poultry.broiler_farming_system.dto.report.AdminAuditStatusCountRow(" +
            "pt.status, COUNT(pt)) " +
            "FROM PaymentTransaction pt " +
            "WHERE pt.transactionTimestamp >= :from AND pt.transactionTimestamp < :to " +
            "GROUP BY pt.status")
    List<AdminAuditStatusCountRow> statusCountsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT new com.poultry.broiler_farming_system.dto.report.AdminAuditFarmerBreakdownRow(" +
            "pt.user.id, pt.user.username, pt.user.fullName, SUM(pt.amount), COUNT(pt)) " +
            "FROM PaymentTransaction pt " +
            "WHERE pt.status = com.poultry.broiler_farming_system.entity.enums.PaymentStatus.APPROVED " +
            "AND pt.transactionTimestamp >= :from AND pt.transactionTimestamp < :to " +
            "GROUP BY pt.user.id, pt.user.username, pt.user.fullName " +
            "ORDER BY SUM(pt.amount) DESC NULLS LAST")
    List<AdminAuditFarmerBreakdownRow> farmerBreakdownBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // All-time (no date range) revenue by the payer's normalized location --
    // see UserRepository.countUsersByLocationAccountTypeAndRole for the
    // normalization rule. Backs AdminLocationAnalyticsController, distinct
    // from the date-ranged admin PDF report queries above.
    @Query("SELECT new com.poultry.broiler_farming_system.dto.analytics.LocationRevenueRow(" +
            "COALESCE(NULLIF(UPPER(TRIM(pt.user.location)), ''), 'UNSPECIFIED'), SUM(pt.amount), COUNT(pt)) " +
            "FROM PaymentTransaction pt " +
            "WHERE pt.status = com.poultry.broiler_farming_system.entity.enums.PaymentStatus.APPROVED " +
            "GROUP BY COALESCE(NULLIF(UPPER(TRIM(pt.user.location)), ''), 'UNSPECIFIED')")
    List<LocationRevenueRow> revenueByLocation();
}
