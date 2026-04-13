package com.champ.healthcare.ClinicRoom.PresentationLayer;

import com.champ.healthcare.ClinicRoom.BusinessLogicLayer.ClinicRoomService;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicRoomControllerTest {

    @Mock
    private ClinicRoomService clinicRoomService;

    private ClinicRoomController clinicRoomController;

    @BeforeEach
    void setUp() {
        clinicRoomController = new ClinicRoomController(clinicRoomService);
    }

    @Test
    void getAllRoomsReturnsOkResponse() {
        ClinicRoomResponseDTO room = roomResponse();
        when(clinicRoomService.getAllRooms()).thenReturn(List.of(room));

        ResponseEntity<List<ClinicRoomResponseDTO>> response = clinicRoomController.getAllRooms();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(room);
    }

    @Test
    void getRoomByIdReturnsOkResponse() {
        ClinicRoomResponseDTO room = roomResponse();
        when(clinicRoomService.getRoomById(1L)).thenReturn(room);

        ResponseEntity<ClinicRoomResponseDTO> response = clinicRoomController.getRoomById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(room);
    }

    @Test
    void createRoomReturnsCreatedResponse() {
        ClinicRoomRequestDTO request = roomRequest();
        ClinicRoomResponseDTO room = roomResponse();
        when(clinicRoomService.createRoom(request)).thenReturn(room);

        ResponseEntity<ClinicRoomResponseDTO> response = clinicRoomController.createRoom(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/clinic-rooms/1");
        assertThat(response.getBody()).isEqualTo(room);
    }

    @Test
    void updateRoomReturnsOkResponse() {
        ClinicRoomRequestDTO request = roomRequest();
        ClinicRoomResponseDTO room = roomResponse();
        when(clinicRoomService.updateRoom(2L, request)).thenReturn(room);

        ResponseEntity<ClinicRoomResponseDTO> response = clinicRoomController.updateRoom(2L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(room);
    }

    @Test
    void updateRoomStatusReturnsOkResponse() {
        ClinicRoomResponseDTO room = roomResponse();
        room.setRoomStatus(ClinicRoomStatus.OUT_OF_SERVICE);
        when(clinicRoomService.updateRoomStatus(2L, ClinicRoomStatus.OUT_OF_SERVICE)).thenReturn(room);

        ResponseEntity<ClinicRoomResponseDTO> response = clinicRoomController.updateRoomStatus(
                2L,
                new ClinicRoomStatusPatchDTO(ClinicRoomStatus.OUT_OF_SERVICE)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(room);
    }

    @Test
    void deleteRoomReturnsOkResponse() {
        ClinicRoomResponseDTO room = roomResponse();
        when(clinicRoomService.deleteRoom(3L)).thenReturn(room);

        ResponseEntity<ClinicRoomResponseDTO> response = clinicRoomController.deleteRoom(3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(room);
    }

    private ClinicRoomRequestDTO roomRequest() {
        return ClinicRoomRequestDTO.builder()
                .roomName("Consultation Room")
                .roomNumber("101")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();
    }

    private ClinicRoomResponseDTO roomResponse() {
        return ClinicRoomResponseDTO.builder()
                .id(1L)
                .roomId("room-1")
                .roomName("Consultation Room")
                .roomNumber("101")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();
    }
}
