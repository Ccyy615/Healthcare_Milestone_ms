package com.champ.healthcare.Doctor.Domain;

import com.champ.healthcare.Doctor.Mapper.DoctorMapper;
import com.champ.healthcare.Doctor.PresentationLayer.DoctorRequestDTO;
import com.champ.healthcare.Doctor.PresentationLayer.DoctorResponseDTO;
import com.champ.healthcare.Doctor.utilities.DoctorNotEligibleException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoctorDomainAndMapperTest {

    private final DoctorMapper doctorMapper = new DoctorMapper();

    @Test
    void mapperToEntityCopiesFieldsAndDefaultsToInactiveDoctor() {
        DoctorRequestDTO request = new DoctorRequestDTO(
                "John",
                "Smith",
                new WorkZone("Montreal", "Quebec"),
                List.of(new Speciality("Cardiology", ProficiencyLevel.ADVANCED))
        );

        Doctor result = doctorMapper.toEntity(request);

        assertThat(result.getDoctorId()).isNotNull();
        assertThat(result.getDoctorFirstName()).isEqualTo("John");
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getSpeciality()).hasSize(1);
    }

    @Test
    void mapperToEntityDefaultsToEmptySpecialityListWhenRequestHasNone() {
        DoctorRequestDTO request = new DoctorRequestDTO(
                "Jane",
                "Doe",
                new WorkZone("Montreal", "Quebec"),
                null
        );

        Doctor result = doctorMapper.toEntity(request);

        assertThat(result.getSpeciality()).isEmpty();
    }

    @Test
    void mapperToResponseDtoCopiesNestedValues() {
        Doctor doctor = doctor();
        doctor.setLicense(new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1)));

        DoctorResponseDTO result = doctorMapper.toResponseDTO(doctor);

        assertThat(result.getDoctorId()).isEqualTo("doctor-1");
        assertThat(result.getWorkZone().getCity()).isEqualTo("Montreal");
        assertThat(result.getSpeciality()).hasSize(1);
        assertThat(result.getLicense().getLicenseName()).isEqualTo("Practice License");
    }

    @Test
    void mapperToResponseDtoLeavesOptionalFieldsNullWhenMissing() {
        Doctor doctor = doctor();
        doctor.setWorkZone(null);
        doctor.setSpeciality(null);
        doctor.setLicense(null);

        DoctorResponseDTO result = doctorMapper.toResponseDTO(doctor);

        assertThat(result.getWorkZone()).isNull();
        assertThat(result.getSpeciality()).isNull();
        assertThat(result.getLicense()).isNull();
    }

    @Test
    void activateRequiresVerifiedSpeciality() {
        Doctor doctor = doctor();
        doctor.setSpeciality(new ArrayList<>(List.of(new Speciality("Cardiology", ProficiencyLevel.BEGINNER))));
        doctor.setLicense(new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1)));

        assertThatThrownBy(doctor::activate)
                .isInstanceOf(DoctorNotEligibleException.class)
                .hasMessage("Cannot activate Doctor: No verified speciality found.");
    }

    @Test
    void activateRequiresValidLicense() {
        Doctor doctor = doctor();
        doctor.setSpeciality(new ArrayList<>(List.of(new Speciality("Cardiology", ProficiencyLevel.EXPERT))));
        doctor.setLicense(new License("Practice License", LicenseStatus.SUSPENDED, LocalDateTime.now().minusDays(1)));

        assertThatThrownBy(doctor::activate)
                .isInstanceOf(DoctorNotEligibleException.class)
                .hasMessage("Cannot activate handyman: No valid license found. ");
    }

    @Test
    void activateAndRemoveSpecialityUpdateDoctorState() {
        Doctor doctor = doctor();
        doctor.setSpeciality(new ArrayList<>(List.of(new Speciality("Cardiology", ProficiencyLevel.EXPERT))));
        doctor.setLicense(new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1)));

        doctor.activate();
        assertThat(doctor.getIsActive()).isTrue();

        doctor.removeSpeciality("Cardiology");
        assertThat(doctor.getIsActive()).isFalse();
    }

    @Test
    void removeSpecialityKeepsDoctorActiveWhenVerifiedSpecialityStillExists() {
        Doctor doctor = doctor();
        doctor.setIsActive(true);
        doctor.setSpeciality(new ArrayList<>(List.of(
                new Speciality("Cardiology", ProficiencyLevel.EXPERT),
                new Speciality("Neurology", ProficiencyLevel.ADVANCED)
        )));

        doctor.removeSpeciality("Cardiology");

        assertThat(doctor.getIsActive()).isTrue();
        assertThat(doctor.getSpeciality()).hasSize(1);
    }

    @Test
    void verifyAndUnverifyToggleDoctorFlags() {
        Doctor doctor = doctor();
        doctor.setIsActive(true);

        doctor.verify();
        assertThat(doctor.getIsValid()).isTrue();

        doctor.unverify();
        assertThat(doctor.getIsValid()).isFalse();
        assertThat(doctor.getIsActive()).isFalse();
    }

    @Test
    void updateWorkZoneAndSetLicenseLinkEntities() {
        Doctor doctor = doctor();
        License license = new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1));

        doctor.updateWorkZone(new WorkZone("Toronto", "Ontario"));
        doctor.setLicense(license);

        assertThat(doctor.getWorkZone().getCity()).isEqualTo("Toronto");
        assertThat(license.getDoctor()).isSameAs(doctor);
        assertThat(doctor.hasValidLicense()).isTrue();
    }

    @Test
    void setLicenseAcceptsNullAndMarksDoctorAsWithoutValidLicense() {
        Doctor doctor = doctor();

        doctor.setLicense(null);

        assertThat(doctor.getLicense()).isNull();
        assertThat(doctor.hasValidLicense()).isFalse();
    }

    @Test
    void specialityAndWorkZoneValidationRejectInvalidValues() {
        Speciality speciality = new Speciality(" ", ProficiencyLevel.ADVANCED);
        WorkZone workZone = new WorkZone(" ", "Quebec");

        assertThatThrownBy(speciality::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("speciality type is required.");

        assertThatThrownBy(workZone::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("City is required");
    }

    @Test
    void specialityAndWorkZoneValidationRejectNullAndBlankVariants() {
        Speciality nullSpeciality = new Speciality(null, ProficiencyLevel.ADVANCED);
        Speciality missingLevel = new Speciality("Cardiology", null);
        WorkZone nullCity = new WorkZone(null, "Quebec");
        WorkZone blankProvince = new WorkZone("Montreal", " ");
        WorkZone nullProvince = new WorkZone("Montreal", null);

        assertThatThrownBy(nullSpeciality::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("speciality type is required.");

        assertThatThrownBy(missingLevel::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Proficiency level is required.");

        assertThatThrownBy(nullCity::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("City is required");

        assertThatThrownBy(blankProvince::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Province is required");

        assertThatThrownBy(nullProvince::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Province is required");
    }

    @Test
    void specialityVerificationReflectsSupportedLevels() {
        assertThat(new Speciality("Cardiology", ProficiencyLevel.INTERMEDIATE).isVerified()).isTrue();
        assertThat(new Speciality("Cardiology", ProficiencyLevel.ADVANCED).isVerified()).isTrue();
        assertThat(new Speciality("Cardiology", ProficiencyLevel.EXPERT).isVerified()).isTrue();
        assertThat(new Speciality("Cardiology", ProficiencyLevel.BEGINNER).isVerified()).isFalse();
    }

    @Test
    void licenseHelpersReflectValidityAndExpiry() {
        License validLicense = new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1));
        License expiredLicense = new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusYears(6));

        expiredLicense.setExpiryDate(LocalDateTime.now().minusDays(1));

        assertThat(validLicense.isValid()).isTrue();
        assertThat(expiredLicense.isExpired()).isTrue();

        validLicense.updateStatus("Practice License", LicenseStatus.SUSPENDED);
        assertThat(validLicense.isValid()).isFalse();
    }

    @Test
    void licenseHelpersHandleNullFutureAndExpiredDates() {
        License noExpiryLicense = new License("Practice License", LicenseStatus.SUSPENDED, LocalDateTime.now().minusDays(1));
        noExpiryLicense.setStatus(LicenseStatus.VALID);
        noExpiryLicense.setExpiryDate(null);

        License futureExpiryLicense = new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1));
        futureExpiryLicense.setExpiryDate(LocalDateTime.now().plusDays(2));

        License expiredLicense = new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1));
        expiredLicense.setExpiryDate(LocalDateTime.now().minusDays(2));

        assertThat(noExpiryLicense.isValid()).isTrue();
        assertThat(noExpiryLicense.isExpired()).isFalse();
        assertThat(futureExpiryLicense.isExpired()).isFalse();
        assertThat(expiredLicense.isValid()).isFalse();
    }

    @Test
    void updateStatusToValidSetsExpiryDate() {
        License license = new License("Practice License", LicenseStatus.SUSPENDED, LocalDateTime.now().minusDays(1));

        license.updateStatus("Updated License", LicenseStatus.VALID);

        assertThat(license.getLicenseName()).isEqualTo("Updated License");
        assertThat(license.getStatus()).isEqualTo(LicenseStatus.VALID);
        assertThat(license.getExpiryDate()).isAfter(LocalDateTime.now().plusYears(4));
    }

    private Doctor doctor() {
        return Doctor.builder()
                .id(1L)
                .doctorId(new DoctorIdentifier("doctor-1"))
                .doctorFirstName("John")
                .doctorLastName("Smith")
                .isActive(false)
                .isValid(false)
                .workZone(new WorkZone("Montreal", "Quebec"))
                .speciality(new ArrayList<>(List.of(new Speciality("Cardiology", ProficiencyLevel.ADVANCED))))
                .build();
    }
}
