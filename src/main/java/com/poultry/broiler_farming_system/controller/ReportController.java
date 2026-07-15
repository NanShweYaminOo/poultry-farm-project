package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.report.AdminAuditReportService;
import com.poultry.broiler_farming_system.service.report.UserAuditReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;

// PDF audit reports. Both endpoints return a StreamingResponseBody rather
// than a ResponseEntity<byte[]> -- the PDF is written directly to the
// servlet response's OutputStream as openhtmltopdf paginates it, instead of
// first being fully materialized as a byte array in JVM heap. See
// PdfGenerationService's Javadoc for exactly what "streaming" does and
// doesn't buy here, and for the PDF-library choice writeup and the Myanmar
// complex-script shaping caveat.
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final UserAuditReportService userAuditReportService;
    private final AdminAuditReportService adminAuditReportService;

    // Any authenticated user pulls only their own report -- farmerId comes
    // from the principal, never a path/query parameter, so there's no way to
    // request someone else's financial history through this endpoint.
    @GetMapping("/my-audit")
    public ResponseEntity<StreamingResponseBody> myAuditReport(@AuthenticationPrincipal UserPrincipal principal) {
        StreamingResponseBody body = outputStream ->
                userAuditReportService.writeReport(principal.getId(), outputStream);
        return pdfResponse(body, "my-financial-audit.pdf");
    }

    // ADMIN only (SecurityConfig). from/to are inclusive calendar dates --
    // AdminAuditReportService converts them to a [from 00:00, to+1 00:00)
    // half-open LocalDateTime range internally.
    @GetMapping("/admin-audit")
    public ResponseEntity<StreamingResponseBody> adminAuditReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' cannot be before 'from'.");
        }
        StreamingResponseBody body = outputStream ->
                adminAuditReportService.writeReport(from, to, outputStream);
        return pdfResponse(body, "system-financial-audit-" + from + "-to-" + to + ".pdf");
    }

    private ResponseEntity<StreamingResponseBody> pdfResponse(StreamingResponseBody body, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }
}
