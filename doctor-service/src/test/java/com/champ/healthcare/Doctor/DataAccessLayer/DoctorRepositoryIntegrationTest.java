package com.champ.healthcare.Doctor.DataAccessLayer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DoctorRepositoryIntegrationTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Test
    void findByDoctorIdReturnsSeededDoctor() {
        assertThat(doctorRepository.findByDoctorId_DoctorId("e1f2a3b4-c5d6-47e8-9f01-23456789abcd"))
                .isPresent()
                .get()
                .extracting(doctor -> doctor.getDoctorId().getDoctorId())
                .isEqualTo("e1f2a3b4-c5d6-47e8-9f01-23456789abcd");
    }

    @Test
    void findByDoctorIdReturnsEmptyForUnknownDoctor() {
        assertThat(doctorRepository.findByDoctorId_DoctorId("missing-doctor")).isEmpty();
    }
}
