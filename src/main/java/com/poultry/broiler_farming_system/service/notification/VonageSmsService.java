package com.poultry.broiler_farming_system.service.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poultry.broiler_farming_system.exception.SmsDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Real SMS delivery via Vonage's (formerly Nexmo) SMS API, not Twilio.
 *
 * Twilio's own supported-destinations documentation lists Myanmar as a
 * restricted/limited route: outbound A2P SMS to Myanmar numbers commonly
 * requires a pre-approved alphanumeric Sender ID and is not guaranteed to
 * be deliverable on all three local carriers (MPT, Ooredoo, Mytel/ATOM),
 * which is exactly the failure mode this feature exists to avoid. Vonage
 * has broader direct-carrier SMSC connectivity into Southeast Asia and is
 * commonly used for Myanmar A2P traffic instead. (Telecom routing
 * agreements change over time -- verify current deliverability with
 * whichever provider you actually credential before relying on this in
 * production.)
 *
 * Uses Spring's built-in RestClient (spring-boot-starter-restclient is
 * already a transitive dependency here via spring-ai-starter-model-openai)
 * against Vonage's plain REST endpoint instead of pulling in a vendor SDK
 * -- no new pom.xml dependency needed for this class.
 *
 * Only active under the "sms-live" profile; LoggingSmsService
 * (@Profile("!sms-live")) is the default everywhere else, including every
 * test, so nothing here ever fires unless SPRING_PROFILES_ACTIVE=sms-live
 * is set deliberately alongside real Vonage credentials.
 */
@Service
@Profile("sms-live")
public class VonageSmsService implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(VonageSmsService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final SmsRateLimiter rateLimiter;
    private final String apiKey;
    private final String apiSecret;
    private final String senderId;

    public VonageSmsService(RestClient.Builder restClientBuilder,
                             ObjectMapper objectMapper,
                             SmsRateLimiter rateLimiter,
                             @Value("${app.sms.vonage.api-key}") String apiKey,
                             @Value("${app.sms.vonage.api-secret}") String apiSecret,
                             @Value("${app.sms.vonage.sender-id}") String senderId) {
        this.restClient = restClientBuilder.baseUrl("https://rest.nexmo.com").build();
        this.objectMapper = objectMapper;
        this.rateLimiter = rateLimiter;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.senderId = senderId;
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
        // Deliberately not caught here: an unnormalizable number is a data
        // problem, not a transient gateway failure, but the caller
        // (SmsRequestedEventListener) treats every RuntimeException the
        // same way -- log and drop -- so there's no behavioral difference
        // in letting it propagate unchanged.
        String e164 = PhoneNumberNormalizer.toE164(phoneNumber);
        String toParam = e164.substring(1); // Vonage's `to` field takes the number without the leading '+'

        if (!rateLimiter.tryAcquire()) {
            throw new SmsDeliveryException("SMS rate limit exceeded; dropped message to " + mask(toParam) + ".");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("api_key", apiKey);
        form.add("api_secret", apiSecret);
        form.add("from", senderId);
        form.add("to", toParam);
        form.add("text", message);

        String responseBody;
        try {
            responseBody = restClient.post()
                    .uri("/sms/json")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw new SmsDeliveryException("SMS gateway call failed for " + mask(toParam) + ": " + ex.getMessage(), ex);
        }

        JsonNode firstMessage;
        try {
            firstMessage = objectMapper.readTree(responseBody).path("messages").path(0);
        } catch (Exception ex) {
            throw new SmsDeliveryException("Could not parse SMS gateway response for " + mask(toParam) + ": " + responseBody, ex);
        }

        // Vonage's per-message "status" is "0" for success; anything else
        // (throttled, blacklisted, insufficient balance, invalid number,
        // etc.) is a delivery failure even though the HTTP call itself
        // returned 200.
        String status = firstMessage.path("status").asText("");
        if (!"0".equals(status)) {
            String errorText = firstMessage.path("error-text").asText("unknown error");
            throw new SmsDeliveryException(
                    "Vonage rejected SMS to " + mask(toParam) + ": " + errorText + " (status " + status + ")");
        }

        log.info("SMS sent to {} (message-id {})", mask(toParam), firstMessage.path("message-id").asText());
    }

    // Never log a full subscriber number -- only enough to correlate
    // against a support ticket.
    private String mask(String phoneNumberDigits) {
        if (phoneNumberDigits.length() <= 4) {
            return "***" + phoneNumberDigits;
        }
        return "***" + phoneNumberDigits.substring(phoneNumberDigits.length() - 4);
    }
}
