package com.champ.healthcare.ClinicRoom.DataAccessLayer;

import com.champ.healthcare.ClinicRoom.Domain.ClinicRoom;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomIdentifier;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("testing")
class ClinicRoomRepositoryIntegrationTest {

    @Autowired
    private ClinicRoomRepository clinicRoomRepository;

    @BeforeEach
    void clearRepository() {
        clinicRoomRepository.deleteAll();
    }

    @Test
    void savePersistsRoomsWithNumericIds() {
        clinicRoomRepository.saveAll(List.of(
                room(1L, "room-1", "101"),
                room(2L, "room-2", "102")
        ));

        assertThat(clinicRoomRepository.findById(2L))
                .get()
                .extracting(ClinicRoom::getRoomNumber)
                .isEqualTo("102");
    }

    @Test
    void existsByRoomNumberReturnsTrueForSeededRoom() {
        clinicRoomRepository.save(room(1L, "room-1", "101"));

        assertThat(clinicRoomRepository.existsByRoomNumber("101")).isTrue();
    }

    @Test
    void findByRoomIdReturnsEmptyForUnknownRoom() {
        assertThat(clinicRoomRepository.findByRoomId_RoomId("missing-room")).isEmpty();
    }

    private ClinicRoom room(Long id, String roomId, String roomNumber) {
        return ClinicRoom.builder()
                .id(id)
                .roomId(new ClinicRoomIdentifier(roomId))
                .roomName("Consultation Room")
                .roomNumber(roomNumber)
                .roomStatus(ClinicRoomStatus.AVAILABLE)
                .build();
    }
}
