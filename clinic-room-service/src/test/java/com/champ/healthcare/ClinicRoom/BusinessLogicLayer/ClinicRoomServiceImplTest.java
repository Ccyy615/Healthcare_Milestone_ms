package com.champ.healthcare.ClinicRoom.BusinessLogicLayer;

import com.champ.healthcare.ClinicRoom.DataAccessLayer.ClinicRoomRepository;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoom;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomIdentifier;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomStatus;
import com.champ.healthcare.ClinicRoom.Mapper.ClinicRoomMapper;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomResponseDTO;
import com.champ.healthcare.ClinicRoom.utilities.DuplicateRoomNumberException;
import com.champ.healthcare.ClinicRoom.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicRoomServiceImplTest {

    @Mock
    private ClinicRoomRepository clinicRoomRepository;

    @Mock
    private ClinicRoomMapper clinicRoomMapper;

    @InjectMocks
    private ClinicRoomServiceImpl clinicRoomService;

    @Test
    void getAllRoomsReturnsMappedList() {
        List<ClinicRoom> rooms = List.of(room(1L, "101"), room(2L, "102"));
        List<ClinicRoomResponseDTO> responses = List.of(response(1L, "101"), response(2L, "102"));

        when(clinicRoomRepository.findAll()).thenReturn(rooms);
        when(clinicRoomMapper.toResponseDTOList(rooms)).thenReturn(responses);

        List<ClinicRoomResponseDTO> result = clinicRoomService.getAllRooms();

        assertThat(result).containsExactlyElementsOf(responses);
    }

    @Test
    void getRoomByIdThrowsWhenRoomDoesNotExist() {
        when(clinicRoomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clinicRoomService.getRoomById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Clinic room not found with id: 99");
    }

    @Test
    void getRoomByIdReturnsMappedRoom() {
        ClinicRoom room = room(3L, "101");
        ClinicRoomResponseDTO response = response(3L, "101");

        when(clinicRoomRepository.findById(3L)).thenReturn(Optional.of(room));
        when(clinicRoomMapper.toResponseDTO(room)).thenReturn(response);

        ClinicRoomResponseDTO result = clinicRoomService.getRoomById(3L);

        assertThat(result).isSameAs(response);
    }

    @Test
    void createRoomThrowsWhenRequestIsInvalid() {
        ClinicRoomRequestDTO request = ClinicRoomRequestDTO.builder()
                .roomName(" ")
                .roomNumber("101")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();

        assertThatThrownBy(() -> clinicRoomService.createRoom(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room name is required.");
    }

    @Test
    void createRoomThrowsWhenRoomNameIsNull() {
        ClinicRoomRequestDTO request = ClinicRoomRequestDTO.builder()
                .roomName(null)
                .roomNumber("101")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();

        assertThatThrownBy(() -> clinicRoomService.createRoom(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room name is required.");
    }

    @Test
    void createRoomThrowsWhenRoomNumberIsMissing() {
        ClinicRoomRequestDTO request = ClinicRoomRequestDTO.builder()
                .roomName("Consultation Room")
                .roomNumber(" ")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();

        assertThatThrownBy(() -> clinicRoomService.createRoom(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room number is required.");
    }

    @Test
    void createRoomThrowsWhenRoomNumberIsNull() {
        ClinicRoomRequestDTO request = ClinicRoomRequestDTO.builder()
                .roomName("Consultation Room")
                .roomNumber(null)
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();

        assertThatThrownBy(() -> clinicRoomService.createRoom(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room number is required.");
    }

    @Test
    void createRoomThrowsWhenRoomStatusIsMissing() {
        ClinicRoomRequestDTO request = ClinicRoomRequestDTO.builder()
                .roomName("Consultation Room")
                .roomNumber("101")
                .roomStatus(null)
                .build();

        assertThatThrownBy(() -> clinicRoomService.createRoom(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room status is required.");
    }

    @Test
    void createRoomThrowsWhenRoomNumberAlreadyExists() {
        ClinicRoomRequestDTO request = validRequest();
        when(clinicRoomRepository.existsByRoomNumber("101")).thenReturn(true);

        assertThatThrownBy(() -> clinicRoomService.createRoom(request))
                .isInstanceOf(DuplicateRoomNumberException.class)
                .hasMessage("A clinic room with this room number already exists.");
    }

    @Test
    void createRoomSavesAndReturnsMappedResponse() {
        ClinicRoomRequestDTO request = validRequest();
        ClinicRoom mapped = room(null, "101");
        ClinicRoom saved = room(1L, "101");
        ClinicRoomResponseDTO response = response(1L, "101");

        when(clinicRoomMapper.toEntity(request)).thenReturn(mapped);
        when(clinicRoomRepository.save(mapped)).thenReturn(saved);
        when(clinicRoomMapper.toResponseDTO(saved)).thenReturn(response);

        ClinicRoomResponseDTO result = clinicRoomService.createRoom(request);

        assertThat(result).isSameAs(response);
    }

    @Test
    void updateRoomThrowsWhenRoomDoesNotExist() {
        when(clinicRoomRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clinicRoomService.updateRoom(5L, validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Clinic room not found with id: 5");
    }

    @Test
    void updateRoomThrowsWhenRequestIsInvalid() {
        ClinicRoomRequestDTO request = ClinicRoomRequestDTO.builder()
                .roomName(" ")
                .roomNumber("101")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();

        assertThatThrownBy(() -> clinicRoomService.updateRoom(5L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room name is required.");
    }

    @Test
    void updateRoomThrowsWhenAnotherRoomUsesSameNumber() {
        ClinicRoomRequestDTO request = validRequest();
        when(clinicRoomRepository.findById(5L)).thenReturn(Optional.of(room(5L, "101")));
        when(clinicRoomRepository.existsByRoomNumberAndIdNot("101", 5L)).thenReturn(true);

        assertThatThrownBy(() -> clinicRoomService.updateRoom(5L, request))
                .isInstanceOf(DuplicateRoomNumberException.class)
                .hasMessage("A clinic room with this room number already exists.");
    }

    @Test
    void updateRoomUpdatesExistingEntity() {
        ClinicRoom existing = room(5L, "100");
        ClinicRoomRequestDTO request = validRequest();
        ClinicRoom saved = room(5L, "101");
        ClinicRoomResponseDTO response = response(5L, "101");

        when(clinicRoomRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(clinicRoomRepository.save(existing)).thenReturn(saved);
        when(clinicRoomMapper.toResponseDTO(saved)).thenReturn(response);

        ClinicRoomResponseDTO result = clinicRoomService.updateRoom(5L, request);

        assertThat(result).isSameAs(response);
        assertThat(existing.getRoomName()).isEqualTo("Consultation Room");
        assertThat(existing.getRoomNumber()).isEqualTo("101");
        assertThat(existing.getRoomStatus()).isEqualTo(ClinicRoomStatus.AVAILABLE);
    }

    @Test
    void updateRoomStatusUpdatesExistingEntity() {
        ClinicRoom existing = room(5L, "100");
        ClinicRoomResponseDTO response = response(5L, "100");
        response.setRoomStatus(ClinicRoomStatus.OUT_OF_SERVICE);

        when(clinicRoomRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(clinicRoomRepository.save(existing)).thenReturn(existing);
        when(clinicRoomMapper.toResponseDTO(existing)).thenReturn(response);

        ClinicRoomResponseDTO result =
                clinicRoomService.updateRoomStatus(5L, ClinicRoomStatus.OUT_OF_SERVICE);

        assertThat(result).isSameAs(response);
        assertThat(existing.getRoomStatus()).isEqualTo(ClinicRoomStatus.OUT_OF_SERVICE);
    }

    @Test
    void updateRoomStatusThrowsWhenStatusIsMissing() {
        assertThatThrownBy(() -> clinicRoomService.updateRoomStatus(5L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room status is required.");
    }

    @Test
    void updateRoomStatusThrowsWhenRoomDoesNotExist() {
        when(clinicRoomRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clinicRoomService.updateRoomStatus(5L, ClinicRoomStatus.AVAILABLE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Clinic room not found with id: 5");
    }

    @Test
    void deleteRoomThrowsWhenMissing() {
        when(clinicRoomRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clinicRoomService.deleteRoom(7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Clinic room not found with id: 7");
    }

    @Test
    void deleteRoomDeletesEntityAndReturnsResponse() {
        ClinicRoom existing = room(7L, "201");
        ClinicRoomResponseDTO response = response(7L, "201");

        when(clinicRoomRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(clinicRoomMapper.toResponseDTO(existing)).thenReturn(response);

        ClinicRoomResponseDTO result = clinicRoomService.deleteRoom(7L);

        assertThat(result).isSameAs(response);
        verify(clinicRoomRepository).delete(existing);
    }

    private ClinicRoomRequestDTO validRequest() {
        return ClinicRoomRequestDTO.builder()
                .roomName("Consultation Room")
                .roomNumber("101")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();
    }

    private ClinicRoom room(Long id, String roomNumber) {
        return ClinicRoom.builder()
                .id(id)
                .roomId(new ClinicRoomIdentifier("room-" + (id == null ? "new" : id)))
                .roomName("Consultation Room")
                .roomNumber(roomNumber)
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();
    }

    private ClinicRoomResponseDTO response(Long id, String roomNumber) {
        return ClinicRoomResponseDTO.builder()
                .id(id)
                .roomId("room-" + id)
                .roomName("Consultation Room")
                .roomNumber(roomNumber)
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();
    }
}
