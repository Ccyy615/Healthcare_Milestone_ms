package com.champ.healthcare.ApiGateway.Clinic.BusinessLogicLayer;

import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomResponseDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomStatusPatchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Slf4j
public class ClinicRoomServiceImpl implements ClinicRoomService {

    private final WebClient webClient;
    private static final String BASE_PATH = "/api/v1/clinic-rooms";

    public ClinicRoomServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://clinic-room-service")
                .build();
    }

    @Override
    public List<ClinicRoomResponseDTO> getAllRooms() {
        log.info("Fetching all clinic rooms");

        return webClient.get()
                .uri(BASE_PATH)
                .retrieve()
                .bodyToFlux(ClinicRoomResponseDTO.class)
                .collectList()
                .block();
    }

    @Override
    public ClinicRoomResponseDTO getRoomById(Long id) {
        log.info("Fetching clinic room with id: {}", id);

        return webClient.get()
                .uri(BASE_PATH + "/" + id)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block();
    }

    @Override
    public ClinicRoomResponseDTO createRoom(ClinicRoomRequestDTO requestDTO) {
        log.info("Creating clinic room");

        return webClient.post()
                .uri(BASE_PATH)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block();
    }

    @Override
    public ClinicRoomResponseDTO updateRoom(Long id, ClinicRoomRequestDTO requestDTO) {
        log.info("Updating clinic room with id: {}", id);

        return webClient.put()
                .uri(BASE_PATH + "/" + id)
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block();
    }

    @Override
    public ClinicRoomResponseDTO updateRoomStatus(Long id, ClinicRoomStatusPatchDTO roomStatus) {
        log.info("Updating clinic room status with id: {}", id);

        return webClient.patch()
                .uri(BASE_PATH + "/" + id + "/status")
                .bodyValue(roomStatus)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block();
    }

    @Override
    public ClinicRoomResponseDTO deleteRoom(Long id) {
        log.info("Deleting clinic room with id: {}", id);

        return webClient.delete()
                .uri(BASE_PATH + "/" + id)
                .retrieve()
                .bodyToMono(ClinicRoomResponseDTO.class)
                .block();
    }
}