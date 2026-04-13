package com.champ.healthcare.ClinicRoom.Domain;

import com.champ.healthcare.ClinicRoom.Mapper.ClinicRoomMapper;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClinicRoomDomainAndMapperTest {

    private final ClinicRoomMapper clinicRoomMapper = new ClinicRoomMapper();

    @Test
    void mapperToEntityCreatesRoomWithIdentifier() {
        ClinicRoomRequestDTO request = ClinicRoomRequestDTO.builder()
                .roomName("Consultation Room")
                .roomNumber("101")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();

        ClinicRoom result = clinicRoomMapper.toEntity(request);

        assertThat(result.getRoomId()).isNotNull();
        assertThat(result.getRoomName()).isEqualTo("Consultation Room");
        assertThat(result.getRoomNumber()).isEqualTo("101");
    }

    @Test
    void mapperToResponseDtoCopiesRoomValues() {
        ClinicRoom room = ClinicRoom.builder()
                .id(3L)
                .roomId(new ClinicRoomIdentifier("room-3"))
                .roomName("Treatment Room")
                .roomNumber("201")
                .roomStatus(ClinicRoomStatus.OUT_OF_SERVICE)
                .build();

        ClinicRoomResponseDTO result = clinicRoomMapper.toResponseDTO(room);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getRoomId()).isEqualTo("room-3");
        assertThat(result.getRoomStatus()).isEqualTo(ClinicRoomStatus.OUT_OF_SERVICE);
    }

    @Test
    void mapperToResponseDtoAllowsMissingRoomIdentifier() {
        ClinicRoom room = ClinicRoom.builder()
                .id(4L)
                .roomId(null)
                .roomName("Treatment Room")
                .roomNumber("202")
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();

        ClinicRoomResponseDTO result = clinicRoomMapper.toResponseDTO(room);

        assertThat(result.getId()).isEqualTo(4L);
        assertThat(result.getRoomId()).isNull();
    }

    @Test
    void mapperToResponseDtoListMapsEveryRoom() {
        List<ClinicRoomResponseDTO> result = clinicRoomMapper.toResponseDTOList(List.of(
                ClinicRoom.builder()
                        .id(1L)
                        .roomId(new ClinicRoomIdentifier("room-1"))
                        .roomName("A")
                        .roomNumber("101")
                        .roomStatus(ClinicRoomStatus.AVAILABLE)
                        .build(),
                ClinicRoom.builder()
                        .id(2L)
                        .roomId(new ClinicRoomIdentifier("room-2"))
                        .roomName("B")
                        .roomNumber("102")
                        .roomStatus(ClinicRoomStatus.OUT_OF_SERVICE)
                        .build()
        ));

        assertThat(result).hasSize(2);
        assertThat(result.get(1).getRoomNumber()).isEqualTo("102");
    }

    @Test
    void isAvailableForBookingReflectsRoomStatus() {
        ClinicRoom available = ClinicRoom.builder().roomStatus(ClinicRoomStatus.AVAILABLE).build();
        ClinicRoom unavailable = ClinicRoom.builder().roomStatus(ClinicRoomStatus.OUT_OF_SERVICE).build();

        assertThat(available.isAvailableForBooking()).isTrue();
        assertThat(unavailable.isAvailableForBooking()).isFalse();
    }

    @Test
    void clinicRoomIdentifierSupportsDefaultAndUuidConstructors() {
        ClinicRoomIdentifier defaultIdentifier = new ClinicRoomIdentifier();
        ClinicRoomIdentifier uuidIdentifier = new ClinicRoomIdentifier(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        );

        assertThat(defaultIdentifier.getRoomId()).isNotBlank();
        assertThat(uuidIdentifier.getRoomId()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
    }
}
