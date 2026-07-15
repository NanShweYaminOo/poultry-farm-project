package com.poultry.broiler_farming_system.service.marketplace;

import com.poultry.broiler_farming_system.dto.marketplace.AdminSalesPostResponse;
import com.poultry.broiler_farming_system.dto.marketplace.CreateSalesPostRequest;
import com.poultry.broiler_farming_system.dto.marketplace.SalesPostResponse;
import com.poultry.broiler_farming_system.entity.SalesPost;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.AccountType;
import com.poultry.broiler_farming_system.entity.enums.SystemLogAction;
import com.poultry.broiler_farming_system.entity.enums.UserRole;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.exception.UnauthorizedActionException;
import com.poultry.broiler_farming_system.repository.SalesPostRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.moderation.ContentModerationService;
import com.poultry.broiler_farming_system.service.systemlog.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesPostServiceImpl implements SalesPostService {

    private final SalesPostRepository salesPostRepository;
    private final UserRepository userRepository;
    private final ContentModerationService contentModerationService;
    private final SystemLogService systemLogService;

    @Override
    public SalesPostResponse createPost(Long creatorId, CreateSalesPostRequest request) {
        if (!StringUtils.hasText(request.title())) {
            throw new IllegalArgumentException("title is required.");
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + creatorId + " was not found."));
        requirePostingPrivilege(creator);

        contentModerationService.moderate(creator, request.title());
        contentModerationService.moderate(creator, request.description());

        SalesPost post = new SalesPost();
        post.setCreator(creator);
        post.setTitle(request.title().trim());
        post.setDescription(request.description());
        post.setPrice(request.price());

        SalesPost saved = salesPostRepository.save(post);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesPostResponse> listPosts() {
        return salesPostRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSalesPostResponse> listAllForAdmin() {
        return salesPostRepository.findAllByOrderByCreatedDateDesc().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Override
    public void deleteByAdmin(Long adminId, Long postId, String reason) {
        SalesPost post = salesPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales post " + postId + " was not found."));

        String description = "Deleted sales post '" + post.getTitle() + "' (id " + postId
                + ") by user '" + post.getCreator().getUsername() + "'"
                + (StringUtils.hasText(reason) ? "; reason: " + reason.trim() : ".");

        salesPostRepository.delete(post);

        systemLogService.record(adminId, SystemLogAction.DELETE_SALES_POST, "SALES_POST", postId, description);
    }

    private void requirePostingPrivilege(User creator) {
        // posting_extension_expiry is written on payment approval
        // (PaymentTransactionServiceImpl) but was never checked anywhere --
        // this is the enforcement point. ADMIN is exempt, same as every
        // other superuser bypass in this system.
        if (creator.getRole() == UserRole.ADMIN) {
            return;
        }
        // GUEST accounts can never post regardless of payment state -- only
        // a FARMER account can pay for/hold a posting extension in the
        // first place. Checked explicitly (not just left implicit) because
        // accountType and the posting-extension expiry are two independent
        // fields on User; a bug elsewhere setting one without the other
        // must not accidentally let a Guest post.
        if (creator.getAccountType() != AccountType.FARMER) {
            throw new UnauthorizedActionException(
                    "User " + creator.getId() + " is a Guest account; only Farmer accounts can create sales posts.");
        }
        LocalDateTime expiry = creator.getPostingExtensionExpiry();
        if (expiry == null || expiry.isBefore(LocalDateTime.now())) {
            throw new UnauthorizedActionException(
                    "User " + creator.getId() + " does not have an active posting privilege; "
                            + "a payment-approved posting extension is required before creating a sales post.");
        }
    }

    private SalesPostResponse toResponse(SalesPost post) {
        User creator = post.getCreator();
        return new SalesPostResponse(
                post.getId(),
                creator.getId(),
                creator.getUsername(),
                creator.getProfileImageUrl(),
                post.getTitle(),
                post.getDescription(),
                post.getPrice(),
                post.getCreatedDate()
        );
    }

    private AdminSalesPostResponse toAdminResponse(SalesPost post) {
        User creator = post.getCreator();
        return new AdminSalesPostResponse(
                post.getId(),
                creator.getId(),
                creator.getUsername(),
                creator.getIsBanned(),
                creator.getIsFlaggedForReview(),
                post.getTitle(),
                post.getDescription(),
                post.getPrice(),
                post.getCreatedDate()
        );
    }
}
