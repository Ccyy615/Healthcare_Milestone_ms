package com.champ.healthcare.ClinicRoom.DataAccessLayer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClinicRoomRepositoryIntegrationTest {

    @Autowired
    private ClinicRoomRepository clinicRoomRepository;

    @Test
    void existsByRoomNumberReturnsTrueForSeededRoom() {
        assertThat(clinicRoomRepository.existsByRoomNumber("101")).isTrue();
    }

    @Test
    void findByRoomIdReturnsEmptyForUnknownRoom() {
        assertThat(clinicRoomRepository.findByRoomId_RoomId("missing-room")).isEmpty();
    }
}
