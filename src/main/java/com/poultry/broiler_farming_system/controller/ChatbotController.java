package com.poultry.broiler_farming_system.controller;

import com.poultry.broiler_farming_system.dto.chatbot.ChatbotDiagnosisResponse;
import com.poultry.broiler_farming_system.service.chatbot.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// Restricted to ROLE_PAID/ROLE_ADMIN at the SecurityConfig level -- FREE
// users have no AI Chatbot access per spec.
@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping(value = "/diagnose", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatbotDiagnosisResponse diagnose(
            @RequestParam String message,
            @RequestParam(required = false) MultipartFile image) {
        return chatbotService.diagnose(message, image);
    }
}
