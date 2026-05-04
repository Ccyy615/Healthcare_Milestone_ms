package com.champ.healthcare.Appointment.DomainClientLayer;

import com.champ.healthcare.Appointment.utilities.DownstreamServiceException;
import com.champ.healthcare.Appointment.utilities.InvalidInputException;
import com.champ.healthcare.Appointment.utilities.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class ClinicRoomServiceClientImpl implements ClinicRoomServiceClient {

    private static final String BASE_PATH = "/api/v1/clinic-rooms/room-identifier/{roomId}";

    private final WebClient webClient;

    public ClinicRoomServiceClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${services.clinic-room.url}") String clinicRoomServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(clinicRoomServiceUrl).build();
    }

    @Override
    public ClinicRoomClientResponse getRoomByRoomId(String roomId) {
        try {
            return webClient.get()
                    .uri(BASE_PATH, roomId)
                    .retrieve()
                    .bodyToMono(ClinicRoomClientResponse.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException("Clinic room not found with roomId: " + roomId);
        } catch (WebClientResponseException.BadRequest ex) {
            throw new InvalidInputException("Invalid clinic room identifier: " + roomId);
        } catch (WebClientResponseException ex) {
            throw new DownstreamServiceException("Clinic room service returned " + ex.getStatusCode().value() + ".");
        }
    }
}
