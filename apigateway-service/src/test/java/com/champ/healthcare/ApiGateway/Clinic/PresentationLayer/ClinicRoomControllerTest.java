package com.champ.healthcare.ApiGateway.Clinic.PresentationLayer;

import com.champ.healthcare.ApiGateway.Clinic.BusinessLogicLayer.ClinicRoomService;
import com.champ.healthcare.ApiGateway.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicRoomControllerTest {

    @Mock
    private ClinicRoomService clinicRoomService;

    private ClinicRoomModelAssembler clinicRoomModelAssembler;
    private ClinicRoomController clinicRoomController;

    @BeforeEach
    void setUp() {
        clinicRoomModelAssembler = new ClinicRoomModelAssembler();
        clinicRoomController = new ClinicRoomController(clinicRoomService, clinicRoomModelAssembler);
    }

    @Test
    void getAllRoomsReturnsOk() {
        when(clinicRoomService.getAllRooms()).thenReturn(List.of(roomResponse()));

        ResponseEntity<List<ClinicRoomResponseDTO>> response = clinicRoomController.getAllRooms();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRequiredLink("self").getHref()).contains("/api/v1/clinic-rooms/1");
    }

    @Test
    void getRoomByIdReturnsOk() {
        when(clinicRoomService.getRoomById(1L)).thenReturn(roomResponse());

        assertThat(clinicRoomController.getRoomById(1L).getBody().getRequiredLink("roomIdentifier").getHref())
                .contains("/api/v1/clinic-rooms/room-identifier/room-1");
    }

    @Test
    void getRoomByRoomIdReturnsOk() {
        when(clinicRoomService.getRoomByRoomId("room-1")).thenReturn(roomResponse());

        assertThat(clinicRoomController.getRoomByRoomId("room-1").getBody().getRoomNumber()).isEqualTo("101");
    }

    @Test
    void createRoomReturnsCreated() {
        ClinicRoomRequestDTO requestDTO = roomRequest();
        when(clinicRoomService.createRoom(requestDTO)).thenReturn(roomResponse());

        ResponseEntity<ClinicRoomResponseDTO> response = clinicRoomController.createRoom(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/clinic-rooms/1");
        assertThat(response.getBody().getRequiredLink("status").getHref()).contains("/api/v1/clinic-rooms/1/status");
    }

    @Test
    void updateRoomReturnsOk() {
        ClinicRoomRequestDTO requestDTO = roomRequest();
        when(clinicRoomService.updateRoom(1L, requestDTO)).thenReturn(roomResponse());

        assertThat(clinicRoomController.updateRoom(1L, requestDTO).getBody().getRoomName())
                .isEqualTo("Consultation Room A");
    }

    @Test
    void updateRoomStatusReturnsOk() {
        ClinicRoomStatusPatchDTO patchDTO = new ClinicRoomStatusPatchDTO("AVAILABLE");
        when(clinicRoomService.updateRoomStatus(1L, patchDTO)).thenReturn(roomResponse());

        assertThat(clinicRoomController.updateRoomStatus(1L, patchDTO).getBody().getRoomStatus().getRoomStatus())
                .isEqualTo("AVAILABLE");
    }

    @Test
    void deleteRoomReturnsOk() {
        when(clinicRoomService.deleteRoom(1L)).thenReturn(roomResponse());

        assertThat(clinicRoomController.deleteRoom(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getRoomByIdPropagatesNegativePath() {
        when(clinicRoomService.getRoomById(404L)).thenThrow(new ResourceNotFoundException("missing room"));

        assertThatThrownBy(() -> clinicRoomController.getRoomById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("missing room");
    }

    private ClinicRoomRequestDTO roomRequest() {
        return ClinicRoomRequestDTO.builder()
                .roomName("Consultation Room A")
                .roomNumber("101")
                .roomStatus(new ClinicRoomStatusPatchDTO("AVAILABLE"))
                .build();
    }

    private ClinicRoomResponseDTO roomResponse() {
        return ClinicRoomResponseDTO.builder()
                .id(1L)
                .roomId("room-1")
                .roomName("Consultation Room A")
                .roomNumber("101")
                .roomStatus(new ClinicRoomStatusPatchDTO("AVAILABLE"))
                .build();
    }
}
