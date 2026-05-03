package com.champ.healthcare.ApiGateway.Clinic.BusinessLogicLayer;

import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomResponseDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomStatusPatchDTO;
import com.champ.healthcare.ApiGateway.utilities.WebClientErrorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.function.Supplier;

@Service
@Slf4j
public class ClinicRoomServiceImpl implements ClinicRoomService {

    private final WebClient webClient;
    private static final String BASE_PATH = "/api/v1/clinic-rooms";

    public ClinicRoomServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${services.clinic-room.url}") String clinicRoomServiceUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(clinicRoomServiceUrl)
                .build();
    }

    @Override
    public List<ClinicRoomResponseDTO> getAllRooms() {
        log.info("Fetching all clinic rooms");

        return execute(() -> webClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .bodyToFlux(ClinicRoomResponseDTO.class)
                .collectList()
                .block());
    }

    @Override
    public ClinicRoomResponseDTO getRoomById(Long id) {
        log.info("Fetching clinic room with id: {}", id);

        return execute(() -> webClient.get()
                .uri(BASE_PATH + "/" + id)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block());
    }

    @Override
    public ClinicRoomResponseDTO getRoomByRoomId(String roomId) {
        log.info("Fetching clinic room with room identifier: {}", roomId);

        return execute(() -> webClient.get()
                .uri(BASE_PATH + "/room-identifier/" + roomId)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block());
    }

    @Override
    public ClinicRoomResponseDTO createRoom(ClinicRoomRequestDTO requestDTO) {
        log.info("Creating clinic room");

        return execute(() -> webClient.post()
                .uri(BASE_PATH)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block());
    }

    @Override
    public ClinicRoomResponseDTO updateRoom(Long id, ClinicRoomRequestDTO requestDTO) {
        log.info("Updating clinic room with id: {}", id);

        return execute(() -> webClient.put()
                .uri(BASE_PATH + "/" + id)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block());
    }

    @Override
    public ClinicRoomResponseDTO updateRoomStatus(Long id, ClinicRoomStatusPatchDTO roomStatus) {
        log.info("Updating clinic room status with id: {}", id);

        return execute(() -> webClient.patch()
                .uri(BASE_PATH + "/" + id + "/status")
                .bodyValue(roomStatus)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block());
    }

    @Override
    public ClinicRoomResponseDTO deleteRoom(Long id) {
        log.info("Deleting clinic room with id: {}", id);

        return execute(() -> webClient.delete()
                .uri(BASE_PATH + "/" + id)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block());
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (WebClientResponseException ex) {
            throw WebClientErrorMapper.map("Clinic room service", ex);
        }
    }
}
