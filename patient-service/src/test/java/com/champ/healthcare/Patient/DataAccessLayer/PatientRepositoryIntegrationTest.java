package com.champ.healthcare.Patient.DataAccessLayer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PatientRepositoryIntegrationTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void existsByContactInfoEmailReturnsTrueForSeededPatient() {
        assertThat(patientRepository.existsByContactInfoEmail("john.smith@example.com")).isTrue();
    }

    @Test
    void findByPatientIdReturnsEmptyForUnknownPatient() {
        assertThat(patientRepository.findByPatientId_PatientId("missing-patient")).isEmpty();
    }
}
