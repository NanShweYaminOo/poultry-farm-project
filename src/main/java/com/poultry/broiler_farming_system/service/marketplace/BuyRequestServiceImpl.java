package com.poultry.broiler_farming_system.service.marketplace;

import com.poultry.broiler_farming_system.dto.marketplace.AdminBuyRequestResponse;
import com.poultry.broiler_farming_system.dto.marketplace.BuyRequestResponse;
import com.poultry.broiler_farming_system.dto.marketplace.CreateBuyRequestRequest;
import com.poultry.broiler_farming_system.entity.BuyRequest;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.SystemLogAction;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.BuyRequestRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.moderation.ContentModerationService;
import com.poultry.broiler_farming_system.service.systemlog.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BuyRequestServiceImpl implements BuyRequestService {

    private final BuyRequestRepository buyRequestRepository;
    private final UserRepository userRepository;
    private final ContentModerationService contentModerationService;
    private final SystemLogService systemLogService;

    @Override
    public BuyRequestResponse createRequest(Long creatorId, CreateBuyRequestRequest request) {
        if (!StringUtils.hasText(request.title())) {
            throw new IllegalArgumentException("title is required.");
        }
        if (request.quantity() != null && request.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero when provided.");
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + creatorId + " was not found."));

        contentModerationService.moderate(creator, request.title());
        contentModerationService.moderate(creator, request.description());

        BuyRequest buyRequest = new BuyRequest();
        buyRequest.setCreator(creator);
        buyRequest.setTitle(request.title().trim());
        buyRequest.setDescription(request.description());
        buyRequest.setQuantity(request.quantity());

        BuyRequest saved = buyRequestRepository.save(buyRequest);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuyRequestResponse> listRequests() {
        return buyRequestRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminBuyRequestResponse> listAllForAdmin() {
        return buyRequestRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Override
    public void deleteByAdmin(Long adminId, Long requestId, String reason) {
        BuyRequest buyRequest = buyRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Buy request " + requestId + " was not found."));

        String description = "Deleted buy request '" + buyRequest.getTitle() + "' (id " + requestId
                + ") by user '" + buyRequest.getCreator().getUsername() + "'"
                + (StringUtils.hasText(reason) ? "; reason: " + reason.trim() : ".");

        buyRequestRepository.delete(buyRequest);

        systemLogService.record(adminId, SystemLogAction.DELETE_BUY_REQUEST, "BUY_REQUEST", requestId, description);
    }

    private BuyRequestResponse toResponse(BuyRequest buyRequest) {
        User creator = buyRequest.getCreator();
        return new BuyRequestResponse(
                buyRequest.getId(),
                creator.getId(),
                creator.getUsername(),
                creator.getProfileImageUrl(),
                buyRequest.getTitle(),
                buyRequest.getDescription(),
                buyRequest.getQuantity(),
                buyRequest.getCreatedDate()
        );
    }

    private AdminBuyRequestResponse toAdminResponse(BuyRequest buyRequest) {
        User creator = buyRequest.getCreator();
        return new AdminBuyRequestResponse(
                buyRequest.getId(),
                creator.getId(),
                creator.getUsername(),
                creator.getIsBanned(),
                creator.getIsFlaggedForReview(),
                buyRequest.getTitle(),
                buyRequest.getDescription(),
                buyRequest.getQuantity(),
                buyRequest.getCreatedDate()
        );
    }
}
