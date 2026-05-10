package com.champ.healthcare.Doctor.DataAccessLayer;

import com.champ.healthcare.Doctor.Domain.Doctor;
import com.champ.healthcare.Doctor.Domain.DoctorIdentifier;
import com.champ.healthcare.Doctor.Domain.License;
import com.champ.healthcare.Doctor.Domain.LicenseStatus;
import com.champ.healthcare.Doctor.Domain.ProficiencyLevel;
import com.champ.healthcare.Doctor.Domain.Speciality;
import com.champ.healthcare.Doctor.Domain.WorkZone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Test
    void findByIsActiveTrueAndSpecialityNameReturnsMatchingDoctor() {
        Doctor activeDoctor = Doctor.builder()
                .doctorId(new DoctorIdentifier("active-doctor-1"))
                .doctorFirstName("Taylor")
                .doctorLastName("Brooks")
                .isActive(true)
                .isValid(true)
                .workZone(new WorkZone("Montreal", "Quebec"))
                .speciality(new ArrayList<>(List.of(new Speciality("Cardiology", ProficiencyLevel.EXPERT))))
                .license(new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1)))
                .build();

        doctorRepository.save(activeDoctor);

        assertThat(doctorRepository.findByIsActiveTrueAndSpeciality_SpecialityIgnoreCase("cardiology"))
                .extracting(doctor -> doctor.getDoctorId().getDoctorId())
                .contains("active-doctor-1");
    }
}
