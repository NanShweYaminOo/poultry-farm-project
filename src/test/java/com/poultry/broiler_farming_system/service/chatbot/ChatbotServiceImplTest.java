package com.poultry.broiler_farming_system.service.chatbot;

import com.poultry.broiler_farming_system.dto.chatbot.ChatbotDiagnosisResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MimeType;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceImplTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private ChatClient.PromptUserSpec userSpec;

    private ChatbotServiceImpl chatbotService;

    private void stubFluentChain(String content) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(org.mockito.ArgumentMatchers.<Consumer<ChatClient.PromptUserSpec>>any()))
                .thenAnswer(invocation -> {
                    Consumer<ChatClient.PromptUserSpec> consumer = invocation.getArgument(0);
                    consumer.accept(userSpec);
                    return requestSpec;
                });
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(content);
        chatbotService = new ChatbotServiceImpl(chatClient);
    }

    @Test
    void sendsTextOnlyMessageAndReturnsTheModelReply() {
        stubFluentChain("Preliminary assessment: likely coccidiosis.");

        ChatbotDiagnosisResponse response = chatbotService.diagnose("My chickens have bloody droppings.", null);

        assertThat(response.reply()).isEqualTo("Preliminary assessment: likely coccidiosis.");
        verify(userSpec).text("My chickens have bloody droppings.");
        verify(userSpec, never()).media(any(MimeType.class), any(Resource.class));
    }

    @Test
    void attachesUploadedImageAsMediaWithItsMimeType() throws Exception {
        stubFluentChain("Visible comb discoloration suggests...");
        MockMultipartFile image = new MockMultipartFile(
                "image", "lesion.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        chatbotService.diagnose("What is wrong with this chicken?", image);

        ArgumentCaptor<MimeType> mimeTypeCaptor = ArgumentCaptor.forClass(MimeType.class);
        ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
        verify(userSpec).media(mimeTypeCaptor.capture(), resourceCaptor.capture());
        assertThat(mimeTypeCaptor.getValue()).isEqualTo(MimeType.valueOf("image/jpeg"));
        assertThat(resourceCaptor.getValue().getContentAsByteArray()).isEqualTo("fake-image-bytes".getBytes());
    }

    @Test
    void ignoresAnEmptyImagePart() {
        stubFluentChain("Text-only reply.");
        MockMultipartFile emptyImage = new MockMultipartFile("image", "empty.jpg", "image/jpeg", new byte[0]);

        chatbotService.diagnose("Just a question, no real image attached.", emptyImage);

        verify(userSpec, never()).media(any(MimeType.class), any(Resource.class));
    }

    @Test
    void rejectsAnImageWithNoDeterminableContentType() {
        stubFluentChain("unused");
        MockMultipartFile image = new MockMultipartFile("image", "mystery", null, "bytes".getBytes());

        assertThatThrownBy(() -> chatbotService.diagnose("message", image))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankMessagesWithoutCallingTheModel() {
        chatbotService = new ChatbotServiceImpl(chatClient);

        assertThatThrownBy(() -> chatbotService.diagnose("   ", null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(chatClient);
    }
}
