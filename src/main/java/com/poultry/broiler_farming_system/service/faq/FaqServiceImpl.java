package com.poultry.broiler_farming_system.service.faq;

import com.poultry.broiler_farming_system.dto.faq.FaqResponse;
import com.poultry.broiler_farming_system.dto.faq.UpsertFaqRequest;
import com.poultry.broiler_farming_system.entity.Faq;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FaqServiceImpl implements FaqService {

    private final FaqRepository faqRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FaqResponse> listFaqs() {
        return faqRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FaqResponse getFaq(Long faqId) {
        return toResponse(findFaq(faqId));
    }

    @Override
    public FaqResponse createFaq(UpsertFaqRequest request) {
        requireText(request.question(), "question");
        requireText(request.answer(), "answer");

        Faq faq = new Faq();
        faq.setQuestion(request.question().trim());
        faq.setAnswer(request.answer().trim());
        return toResponse(faqRepository.save(faq));
    }

    @Override
    public FaqResponse updateFaq(Long faqId, UpsertFaqRequest request) {
        requireText(request.question(), "question");
        requireText(request.answer(), "answer");

        Faq faq = findFaq(faqId);
        faq.setQuestion(request.question().trim());
        faq.setAnswer(request.answer().trim());
        return toResponse(faqRepository.save(faq));
    }

    @Override
    public void deleteFaq(Long faqId) {
        faqRepository.delete(findFaq(faqId));
    }

    private Faq findFaq(Long faqId) {
        return faqRepository.findById(faqId)
                .orElseThrow(() -> new ResourceNotFoundException("FAQ " + faqId + " was not found."));
    }

    private void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private FaqResponse toResponse(Faq faq) {
        return new FaqResponse(faq.getId(), faq.getQuestion(), faq.getAnswer(), faq.getCreatedDate());
    }
}
