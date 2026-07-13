package com.poultry.broiler_farming_system.service.chatbot;

import com.poultry.broiler_farming_system.dto.chatbot.ChatbotDiagnosisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatClient chatClient;

    @Override
    public ChatbotDiagnosisResponse diagnose(String message, MultipartFile image) {
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("message is required.");
        }

        String reply = chatClient.prompt()
                .user(u -> {
                    u.text(message);
                    if (image != null && !image.isEmpty()) {
                        u.media(resolveMimeType(image), toResource(image));
                    }
                })
                .call()
                .content();

        return new ChatbotDiagnosisResponse(reply);
    }

    private MimeType resolveMimeType(MultipartFile image) {
        String contentType = image.getContentType();
        if (!StringUtils.hasText(contentType)) {
            throw new IllegalArgumentException("Could not determine the uploaded image's content type.");
        }
        return MimeType.valueOf(contentType);
    }

    private Resource toResource(MultipartFile image) {
        try {
            return new ByteArrayResource(image.getBytes());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read the uploaded image.", ex);
        }
    }
}
