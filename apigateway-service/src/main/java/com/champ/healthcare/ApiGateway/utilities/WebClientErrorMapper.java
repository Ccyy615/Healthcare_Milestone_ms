package com.champ.healthcare.ApiGateway.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public final class WebClientErrorMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private WebClientErrorMapper() {
    }

    public static RuntimeException map(String serviceName, WebClientResponseException ex) {
        String message = extractMessage(ex.getResponseBodyAsString());
        if (message == null || message.isBlank()) {
            message = serviceName + " returned " + ex.getStatusCode().value() + ".";
        }

        return switch (ex.getStatusCode().value()) {
            case 400 -> new InvalidInputException(message);
            case 404 -> new ResourceNotFoundException(message);
            case 409, 422 -> new ConflictException(message);
            default -> new DownstreamServiceException(message);
        };
    }

    private static String extractMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }

        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(responseBody);
            JsonNode messageNode = jsonNode.get("message");
            return messageNode != null && !messageNode.isNull() ? messageNode.asText() : responseBody;
        } catch (Exception ignored) {
            return responseBody;
        }
    }
}
