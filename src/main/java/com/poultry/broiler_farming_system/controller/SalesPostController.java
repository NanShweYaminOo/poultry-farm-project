package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.marketplace.CreateSalesPostRequest;
import com.poultry.broiler_farming_system.dto.marketplace.SalesPostResponse;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.marketplace.SalesPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales-posts")
@RequiredArgsConstructor
public class SalesPostController {

    private final SalesPostService salesPostService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalesPostResponse create(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateSalesPostRequest request) {
        return salesPostService.createPost(principal.getId(), request);
    }

    @GetMapping
    public List<SalesPostResponse> list() {
        return salesPostService.listPosts();
    }
}
