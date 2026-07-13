package com.poultry.broiler_farming_system.service.marketplace;

import com.poultry.broiler_farming_system.dto.marketplace.CreateSalesPostRequest;
import com.poultry.broiler_farming_system.dto.marketplace.SalesPostResponse;
import com.poultry.broiler_farming_system.entity.SalesPost;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.SalesPostRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.moderation.ContentModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesPostServiceImpl implements SalesPostService {

    private final SalesPostRepository salesPostRepository;
    private final UserRepository userRepository;
    private final ContentModerationService contentModerationService;

    @Override
    public SalesPostResponse createPost(Long creatorId, CreateSalesPostRequest request) {
        if (!StringUtils.hasText(request.title())) {
            throw new IllegalArgumentException("title is required.");
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + creatorId + " was not found."));

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
}
