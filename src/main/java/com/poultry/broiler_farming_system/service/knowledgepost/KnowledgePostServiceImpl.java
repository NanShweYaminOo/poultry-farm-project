package com.poultry.broiler_farming_system.service.knowledgepost;

import com.poultry.broiler_farming_system.dto.knowledgepost.KnowledgePostResponse;
import com.poultry.broiler_farming_system.dto.knowledgepost.UpsertKnowledgePostRequest;
import com.poultry.broiler_farming_system.entity.KnowledgePost;
import com.poultry.broiler_farming_system.entity.KnowledgePostDocument;
import com.poultry.broiler_farming_system.entity.KnowledgePostImage;
import com.poultry.broiler_farming_system.entity.User;
import com.poultry.broiler_farming_system.entity.enums.KnowledgePostStatus;
import com.poultry.broiler_farming_system.exception.ResourceNotFoundException;
import com.poultry.broiler_farming_system.repository.KnowledgePostRepository;
import com.poultry.broiler_farming_system.repository.UserRepository;
import com.poultry.broiler_farming_system.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class KnowledgePostServiceImpl implements KnowledgePostService {

    private final KnowledgePostRepository knowledgePostRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgePostResponse> listPosts() {
        return knowledgePostRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgePostResponse getPost(Long postId) {
        return toResponse(findPost(postId));
    }

    @Override
    public KnowledgePostResponse createPost(
            Long authorId, UpsertKnowledgePostRequest request, MultipartFile image, MultipartFile document) {
        requireText(request.titleEn(), "titleEn");
        requireText(request.titleMy(), "titleMy");
        requireText(request.contentEn(), "contentEn");
        requireText(request.contentMy(), "contentMy");
        if (request.postType() == null) {
            throw new IllegalArgumentException("postType is required.");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + authorId + " was not found."));

        KnowledgePost post = new KnowledgePost();
        post.setAdmin(author);
        applyFields(post, request);
        post.setStatus(request.status() != null ? request.status() : KnowledgePostStatus.PUBLISHED);

        if (image != null && !image.isEmpty()) {
            attachImage(post, image, "knowledge-post");
        }
        if (document != null && !document.isEmpty()) {
            attachDocument(post, document, "knowledge-post");
        }

        return toResponse(knowledgePostRepository.save(post));
    }

    @Override
    public KnowledgePostResponse updatePost(
            Long postId, UpsertKnowledgePostRequest request, MultipartFile image, MultipartFile document) {
        requireText(request.titleEn(), "titleEn");
        requireText(request.titleMy(), "titleMy");
        requireText(request.contentEn(), "contentEn");
        requireText(request.contentMy(), "contentMy");
        if (request.postType() == null) {
            throw new IllegalArgumentException("postType is required.");
        }

        KnowledgePost post = findPost(postId);
        applyFields(post, request);
        if (request.status() != null) {
            post.setStatus(request.status());
        }

        if (image != null && !image.isEmpty()) {
            post.getImages().forEach(existing -> fileStorageService.delete(existing.getImageUrl()));
            post.getImages().clear();
            attachImage(post, image, "knowledge-post-" + postId);
        }
        if (document != null && !document.isEmpty()) {
            post.getDocuments().forEach(existing -> fileStorageService.delete(existing.getDocumentUrl()));
            post.getDocuments().clear();
            attachDocument(post, document, "knowledge-post-" + postId);
        }

        return toResponse(knowledgePostRepository.save(post));
    }

    @Override
    public void deletePost(Long postId) {
        KnowledgePost post = findPost(postId);
        post.getImages().forEach(image -> fileStorageService.delete(image.getImageUrl()));
        post.getDocuments().forEach(doc -> fileStorageService.delete(doc.getDocumentUrl()));
        knowledgePostRepository.delete(post);
    }

    private void applyFields(KnowledgePost post, UpsertKnowledgePostRequest request) {
        post.setTitleEn(request.titleEn().trim());
        post.setTitleMy(request.titleMy().trim());
        post.setContentEn(request.contentEn().trim());
        post.setContentMy(request.contentMy().trim());
        post.setPostType(request.postType());
    }

    private void attachImage(KnowledgePost post, MultipartFile image, String filenamePrefix) {
        String imageUrl = fileStorageService.store(image, "knowledge-post-photos", filenamePrefix);
        KnowledgePostImage postImage = new KnowledgePostImage();
        postImage.setKnowledgePost(post);
        postImage.setImageUrl(imageUrl);
        post.getImages().add(postImage);
    }

    private void attachDocument(KnowledgePost post, MultipartFile document, String filenamePrefix) {
        String documentUrl = fileStorageService.storeDocument(document, "knowledge-post-documents", filenamePrefix);
        KnowledgePostDocument postDocument = new KnowledgePostDocument();
        postDocument.setKnowledgePost(post);
        postDocument.setDocumentUrl(documentUrl);
        post.getDocuments().add(postDocument);
    }

    private KnowledgePost findPost(Long postId) {
        return knowledgePostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge post " + postId + " was not found."));
    }

    private void requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private KnowledgePostResponse toResponse(KnowledgePost post) {
        String imageUrl = post.getImages().isEmpty() ? null : post.getImages().get(0).getImageUrl();
        String documentUrl = post.getDocuments().isEmpty() ? null : post.getDocuments().get(0).getDocumentUrl();
        return new KnowledgePostResponse(
                post.getId(),
                post.getPostType(),
                post.getTitleEn(),
                post.getTitleMy(),
                post.getContentEn(),
                post.getContentMy(),
                post.getStatus(),
                post.getAdmin().getUsername(),
                imageUrl,
                documentUrl,
                post.getCreatedDate()
        );
    }
}
