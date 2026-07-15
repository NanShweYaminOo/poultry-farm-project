package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.marketplace.AdminBuyRequestResponse;
import com.poultry.broiler_farming_system.dto.marketplace.AdminSalesPostResponse;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.marketplace.BuyRequestService;
import com.poultry.broiler_farming_system.service.marketplace.SalesPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Admin-only moderation view over the marketplace (sales posts + buy
// requests). Access is restricted to ROLE_ADMIN by the existing
// "/api/v1/admin/**" rule in SecurityConfig. Farmer-facing create/list stays
// on SalesPostController/BuyRequestController.
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminMarketplaceController {

    private final SalesPostService salesPostService;
    private final BuyRequestService buyRequestService;

    @GetMapping("/sales-posts")
    public List<AdminSalesPostResponse> listSalesPosts() {
        return salesPostService.listAllForAdmin();
    }

    @DeleteMapping("/sales-posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSalesPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @RequestParam(required = false) String reason) {
        salesPostService.deleteByAdmin(principal.getId(), postId, reason);
    }

    @GetMapping("/buy-requests")
    public List<AdminBuyRequestResponse> listBuyRequests() {
        return buyRequestService.listAllForAdmin();
    }

    @DeleteMapping("/buy-requests/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBuyRequest(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long requestId,
            @RequestParam(required = false) String reason) {
        buyRequestService.deleteByAdmin(principal.getId(), requestId, reason);
    }
}
