package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.marketplace.BuyRequestResponse;
import com.poultry.broiler_farming_system.dto.marketplace.CreateBuyRequestRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.marketplace.BuyRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/buy-requests")
@RequiredArgsConstructor
public class BuyRequestController {

    private final BuyRequestService buyRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BuyRequestResponse create(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateBuyRequestRequest request) {
        return buyRequestService.createRequest(principal.getId(), request);
    }

    @GetMapping
    public List<BuyRequestResponse> list() {
        return buyRequestService.listRequests();
    }
}
