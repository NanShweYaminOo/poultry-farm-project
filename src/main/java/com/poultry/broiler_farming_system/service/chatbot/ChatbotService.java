package com.poultry.broiler_farming_system.service.chatbot;

import com.poultry.broiler_farming_system.dto.chatbot.ChatbotDiagnosisResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ChatbotService {

    // image is optional -- text-only symptom descriptions are supported.
    // Language detection and topic guardrails are handled by the system
    // prompt (ChatbotConfig), not by separate logic here.
    ChatbotDiagnosisResponse diagnose(String message, MultipartFile image);
}
