package com.poultry.broiler_farming_system.service.knowledgepost;

import com.poultry.broiler_farming_system.dto.knowledgepost.KnowledgePostResponse;
import com.poultry.broiler_farming_system.dto.knowledgepost.UpsertKnowledgePostRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgePostService {

    List<KnowledgePostResponse> listPosts();

    KnowledgePostResponse getPost(Long postId);

    KnowledgePostResponse createPost(
            Long authorId, UpsertKnowledgePostRequest request, MultipartFile image, MultipartFile document);

    KnowledgePostResponse updatePost(
            Long postId, UpsertKnowledgePostRequest request, MultipartFile image, MultipartFile document);

    void deletePost(Long postId);
}
