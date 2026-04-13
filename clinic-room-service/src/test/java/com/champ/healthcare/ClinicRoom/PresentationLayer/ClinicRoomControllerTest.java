package com.champ.healthcare.ClinicRoom.PresentationLayer;

import com.champ.healthcare.ClinicRoom.BusinessLogicLayer.ClinicRoomService;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ClinicRoomControllerTest {

    @Mock
    private ClinicRoomService clinicRoomService;

    @Mock
    private ClinicRoomModelAssembler clinicRoomModelAssembler;

    private ClinicRoomController clinicRoomController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clinicRoomController = new ClinicRoomController(clinicRoomService, clinicRoomModelAssembler);
    }

    @Test
    void getAllRoomsReturnsOkResponse() {
        List<ClinicRoomResponseDTO> rooms = List.of(ClinicRoomResponseDTO.builder().roomNumber("101").build());

        when(clinicRoomService.getAllRooms()).thenReturn(rooms);

        ResponseEntity<List<ClinicRoomResponseDTO>> response = clinicRoomController.getAllRooms();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(rooms);
    }

    @Test
    void getRoomByIdReturnsOkResponse() {
        ClinicRoomResponseDTO room = ClinicRoomResponseDTO.builder().id(1L).roomNumber("101").build();
        EntityModel<ClinicRoomResponseDTO> model = EntityModel.of(room);

        when(clinicRoomService.getRoomById(1L)).thenReturn(room);
        when(clinicRoomModelAssembler.toModel(room)).thenReturn(model);

        ResponseEntity<EntityModel<ClinicRoomResponseDTO>> response = clinicRoomController.getRoomById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void createRoomReturnsCreatedResponse() {
        ClinicRoomRequestDTO request = ClinicRoomRequestDTO.builder()
                .roomName("Consultation Room")
                .roomNumber("101")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();
        ClinicRoomResponseDTO room = ClinicRoomResponseDTO.builder().id(1L).roomNumber("101").build();
        EntityModel<ClinicRoomResponseDTO> model = EntityModel.of(room);

        when(clinicRoomService.createRoom(request)).thenReturn(room);
        when(clinicRoomModelAssembler.toModel(room)).thenReturn(model);

        ResponseEntity<EntityModel<ClinicRoomResponseDTO>> response = clinicRoomController.createRoom(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void updateRoomReturnsOkResponse() {
        ClinicRoomRequestDTO request = ClinicRoomRequestDTO.builder().roomNumber("101").build();
        ClinicRoomResponseDTO room = ClinicRoomResponseDTO.builder().id(2L).roomNumber("101").build();
        EntityModel<ClinicRoomResponseDTO> model = EntityModel.of(room);

        when(clinicRoomService.updateRoom(2L, request)).thenReturn(room);
        when(clinicRoomModelAssembler.toModel(room)).thenReturn(model);

        ResponseEntity<EntityModel<ClinicRoomResponseDTO>> response = clinicRoomController.updateRoom(2L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void deleteRoomReturnsOkResponse() {
        ClinicRoomResponseDTO room = ClinicRoomResponseDTO.builder().id(3L).roomNumber("102").build();
        EntityModel<ClinicRoomResponseDTO> model = EntityModel.of(room);

        when(clinicRoomService.deleteRoom(3L)).thenReturn(room);
        when(clinicRoomModelAssembler.toModel(room)).thenReturn(model);

        ResponseEntity<EntityModel<ClinicRoomResponseDTO>> response = clinicRoomController.deleteRoom(3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }
}
