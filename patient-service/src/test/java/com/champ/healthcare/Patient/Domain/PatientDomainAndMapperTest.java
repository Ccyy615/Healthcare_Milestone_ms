package com.champ.healthcare.Patient.Domain;

import com.champ.healthcare.Patient.Mapper.PatientMapper;
import com.champ.healthcare.Patient.PresentationLayer.PatientRequestDTO;
import com.champ.healthcare.Patient.PresentationLayer.PatientResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatientDomainAndMapperTest {

    private final PatientMapper patientMapper = new PatientMapper();

    @Test
    void mapperToEntityCopiesNestedFields() {
        PatientRequestDTO request = request();

        Patient result = patientMapper.toEntity(request);

        assertThat(result.getPatientId()).isNotNull();
        assertThat(result.getAddress().getCity()).isEqualTo("Montreal");
        assertThat(result.getAllergy().getSubstance()).isEqualTo("Peanuts");
        assertThat(result.getContactInfo().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void mapperToResponseDtoCopiesFields() {
        Patient patient = patient();

        PatientResponseDTO result = patientMapper.toResponseDTO(patient);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getPatientId().getPatientId()).isEqualTo("patient-5");
        assertThat(result.getStatus()).isEqualTo(PatientStatus.ACTIVE);
    }

    @Test
    void constructorRejectsMissingContactInfo() {
        assertThatThrownBy(() -> new Patient(
                "John Smith",
                LocalDate.of(1990, 1, 1),
                "M",
                null,
                "INS-123",
                new Allergy("Peanuts", "Rash"),
                BloodType.A,
                PatientStatus.ACTIVE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patient must have at least one valid contact method: email or phone.");
    }

    @Test
    void constructorAcceptsPatientWhenPhoneIsPresent() {
        Patient patient = new Patient(
                "John Smith",
                LocalDate.of(1990, 1, 1),
                "M",
                new ContactInfo(null, "514-555-0101"),
                "INS-123",
                new Allergy("Peanuts", "Rash"),
                BloodType.A,
                PatientStatus.ACTIVE
        );

        assertThat(patient.getPatientId()).isNotNull();
        assertThat(patient.getContactInfo().getPhone()).isEqualTo("514-555-0101");
        assertThat(patient.getStatus()).isEqualTo(PatientStatus.ACTIVE);
    }

    @Test
    void constructorAcceptsPatientWhenEmailIsPresent() {
        Patient patient = new Patient(
                "John Smith",
                LocalDate.of(1990, 1, 1),
                "M",
                new ContactInfo("john@example.com", null),
                "INS-123",
                new Allergy("Peanuts", "Rash"),
                BloodType.A,
                PatientStatus.ACTIVE
        );

        assertThat(patient.getPatientId()).isNotNull();
        assertThat(patient.getContactInfo().getEmail()).isEqualTo("john@example.com");
        assertThat(patient.getContactInfo().getPhone()).isNull();
    }

    @Test
    void constructorRejectsBlankContactMethods() {
        assertThatThrownBy(() -> new Patient(
                "John Smith",
                LocalDate.of(1990, 1, 1),
                "M",
                new ContactInfo(" ", " "),
                "INS-123",
                new Allergy("Peanuts", "Rash"),
                BloodType.A,
                PatientStatus.ACTIVE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patient must have at least one valid contact method: email or phone.");
    }

    @Test
    void activateDeactivateAndUpdateEmailChangePatientState() {
        Patient patient = patient();
        patient.setStatus(PatientStatus.ACTIVE);

        patient.deactivate();
        assertThat(patient.getStatus()).isEqualTo(PatientStatus.INACTIVE);

        patient.activate();
        assertThat(patient.getStatus()).isEqualTo(PatientStatus.ACTIVE);

        patient.updateEmail("updated@example.com");
        assertThat(patient.getContactInfo().getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void updateContactInfoAcceptsValidPhoneOnlyContact() {
        Patient patient = patient();

        patient.updateContactInfo(new ContactInfo(null, "438-555-1212"));

        assertThat(patient.getContactInfo().getPhone()).isEqualTo("438-555-1212");
        assertThat(patient.getContactInfo().getEmail()).isNull();
    }

    @Test
    void updateContactInfoRejectsBlankContactMethods() {
        Patient patient = patient();

        assertThatThrownBy(() -> patient.updateContactInfo(new ContactInfo(" ", " ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patient must have at least one valid contact method: email or phone.");
    }

    @Test
    void updateEmailCreatesContactInfoWhenMissing() {
        Patient patient = patient();
        patient.setContactInfo(null);

        patient.updateEmail("new@example.com");

        assertThat(patient.getContactInfo()).isNotNull();
        assertThat(patient.getContactInfo().getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateEmailRejectsBlankEmail() {
        Patient patient = patient();

        assertThatThrownBy(() -> patient.updateEmail(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }

    @Test
    void updateEmailRejectsNullEmail() {
        Patient patient = patient();

        assertThatThrownBy(() -> patient.updateEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }

    @Test
    void updateEmailRejectsInvalidEmail() {
        Patient patient = patient();

        assertThatThrownBy(() -> patient.updateEmail("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }

    private PatientRequestDTO request() {
        return new PatientRequestDTO(
                "John Smith",
                LocalDate.of(1990, 1, 1),
                "M",
                new ContactInfo("john@example.com", "514-555-0101"),
                new Address("123 Main", "Montreal", "Quebec", "H1H1H1", "Canada"),
                "INS-123",
                new Allergy("Peanuts", "Rash"),
                BloodType.A,
                PatientStatus.ACTIVE
        );
    }

    private Patient patient() {
        return Patient.builder()
                .id(5L)
                .patientId(new PatientIdentifier("patient-5"))
                .fullName("John Smith")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("M")
                .contactInfo(new ContactInfo("john@example.com", "514-555-0101"))
                .address(new Address("123 Main", "Montreal", "Quebec", "H1H1H1", "Canada"))
                .insuranceNumber("INS-123")
                .allergy(new Allergy("Peanuts", "Rash"))
                .bloodType(BloodType.A)
                .status(PatientStatus.ACTIVE)
                .build();
    }
}
