package com.poultry.broiler_farming_system.service.faq;

import com.poultry.broiler_farming_system.dto.faq.FaqResponse;
import com.poultry.broiler_farming_system.dto.faq.UpsertFaqRequest;

import java.util.List;

public interface FaqService {

    List<FaqResponse> listFaqs();

    FaqResponse getFaq(Long faqId);

    FaqResponse createFaq(UpsertFaqRequest request);

    FaqResponse updateFaq(Long faqId, UpsertFaqRequest request);

    void deleteFaq(Long faqId);
}
