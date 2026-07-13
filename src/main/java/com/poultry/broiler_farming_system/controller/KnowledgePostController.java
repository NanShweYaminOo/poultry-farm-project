package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.knowledgepost.KnowledgePostResponse;
import com.poultry.broiler_farming_system.dto.knowledgepost.UpsertKnowledgePostRequest;
import com.poultry.broiler_farming_system.security.UserPrincipal;
import com.poultry.broiler_farming_system.service.knowledgepost.KnowledgePostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/knowledge-posts")
@RequiredArgsConstructor
public class KnowledgePostController {

    private final KnowledgePostService knowledgePostService;

    // Any authenticated user (articles feed the in-app knowledge base) --
    // see anyRequest().authenticated() in SecurityConfig. Only the write
    // endpoints below are ADMIN-restricted.
    @GetMapping
    public List<KnowledgePostResponse> listPosts() {
        return knowledgePostService.listPosts();
    }

    @GetMapping("/{postId}")
    public KnowledgePostResponse getPost(@PathVariable Long postId) {
        return knowledgePostService.getPost(postId);
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgePostResponse createPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @ModelAttribute UpsertKnowledgePostRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "document", required = false) MultipartFile document) {
        return knowledgePostService.createPost(principal.getId(), request, image, document);
    }

    @PutMapping(value = "/{postId}", consumes = "multipart/form-data")
    public KnowledgePostResponse updatePost(
            @PathVariable Long postId,
            @ModelAttribute UpsertKnowledgePostRequest request,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "document", required = false) MultipartFile document) {
        return knowledgePostService.updatePost(postId, request, image, document);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId) {
        knowledgePostService.deletePost(postId);
    }
}
