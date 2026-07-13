package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.faq.FaqResponse;
import com.poultry.broiler_farming_system.dto.faq.UpsertFaqRequest;
import com.poultry.broiler_farming_system.service.faq.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // Any authenticated user (FAQs are shown on the user-facing help page)
    // -- see anyRequest().authenticated() in SecurityConfig. Only the write
    // endpoints below are ADMIN-restricted.
    @GetMapping
    public List<FaqResponse> listFaqs() {
        return faqService.listFaqs();
    }

    @GetMapping("/{faqId}")
    public FaqResponse getFaq(@PathVariable Long faqId) {
        return faqService.getFaq(faqId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FaqResponse createFaq(@RequestBody UpsertFaqRequest request) {
        return faqService.createFaq(request);
    }

    @PutMapping("/{faqId}")
    public FaqResponse updateFaq(@PathVariable Long faqId, @RequestBody UpsertFaqRequest request) {
        return faqService.updateFaq(faqId, request);
    }

    @DeleteMapping("/{faqId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFaq(@PathVariable Long faqId) {
        faqService.deleteFaq(faqId);
    }
}
