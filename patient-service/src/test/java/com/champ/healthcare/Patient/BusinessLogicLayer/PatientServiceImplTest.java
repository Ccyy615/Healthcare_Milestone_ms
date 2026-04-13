package com.champ.healthcare.Patient.BusinessLogicLayer;

import com.champ.healthcare.Patient.DataAccessLayer.PatientRepository;
import com.champ.healthcare.Patient.Domain.*;
import com.champ.healthcare.Patient.Mapper.PatientMapper;
import com.champ.healthcare.Patient.PresentationLayer.PatientRequestDTO;
import com.champ.healthcare.Patient.PresentationLayer.PatientResponseDTO;
import com.champ.healthcare.Patient.utilities.DuplicateEmailException;
import com.champ.healthcare.Patient.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    void getAllPatientsReturnsMappedPatients() {
        Patient firstPatient = patient(1L, "first@example.com");
        Patient secondPatient = patient(2L, "second@example.com");
        PatientResponseDTO firstResponse = response(1L, "first@example.com");
        PatientResponseDTO secondResponse = response(2L, "second@example.com");

        when(patientRepository.findAll()).thenReturn(List.of(firstPatient, secondPatient));
        when(patientMapper.toResponseDTO(firstPatient)).thenReturn(firstResponse);
        when(patientMapper.toResponseDTO(secondPatient)).thenReturn(secondResponse);

        List<PatientResponseDTO> result = patientService.getAllPatients();

        assertThat(result).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void getPatientByIdThrowsWhenMissing() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient not found with id: 99");
    }

    @Test
    void getPatientByIdReturnsMappedPatient() {
        Patient patient = patient(4L, "john@example.com");
        PatientResponseDTO response = response(4L, "john@example.com");

        when(patientRepository.findById(4L)).thenReturn(Optional.of(patient));
        when(patientMapper.toResponseDTO(patient)).thenReturn(response);

        PatientResponseDTO result = patientService.getPatientById(4L);

        assertThat(result).isSameAs(response);
    }

    @Test
    void createPatientThrowsWhenContactInfoIsMissing() {
        PatientRequestDTO request = validRequest();
        request.setContactInfo(null);

        assertThatThrownBy(() -> patientService.createPatient(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patient must have at least one valid contact method: email or phone.");
    }

    @Test
    void createPatientThrowsWhenContactMethodsAreBlank() {
        PatientRequestDTO request = validRequest();
        request.setContactInfo(new ContactInfo(" ", " "));

        assertThatThrownBy(() -> patientService.createPatient(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Patient must have at least one valid contact method: email or phone.");
    }

    @Test
    void createPatientThrowsWhenEmailAlreadyExists() {
        PatientRequestDTO request = validRequest();
        when(patientRepository.existsByContactInfoEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> patientService.createPatient(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email already exists: john@example.com");
    }

    @Test
    void createPatientAllowsPhoneOnlyContactWithoutDuplicateEmailCheck() {
        PatientRequestDTO request = validRequest();
        request.setContactInfo(new ContactInfo(null, "514-555-0101"));

        Patient mapped = patient(null, null);
        mapped.setContactInfo(new ContactInfo(null, "514-555-0101"));
        Patient saved = patient(1L, null);
        saved.setContactInfo(new ContactInfo(null, "514-555-0101"));
        PatientResponseDTO response = response(1L, null);
        response.setContactInfo(new ContactInfo(null, "514-555-0101"));

        when(patientMapper.toEntity(request)).thenReturn(mapped);
        when(patientRepository.save(mapped)).thenReturn(saved);
        when(patientMapper.toResponseDTO(saved)).thenReturn(response);

        PatientResponseDTO result = patientService.createPatient(request);

        assertThat(result).isSameAs(response);
        verify(patientRepository, never()).existsByContactInfoEmail(any());
    }

    @Test
    void createPatientSavesAndReturnsMappedResponse() {
        PatientRequestDTO request = validRequest();
        Patient mapped = patient(null, "john@example.com");
        Patient saved = patient(1L, "john@example.com");
        PatientResponseDTO response = response(1L, "john@example.com");

        when(patientMapper.toEntity(request)).thenReturn(mapped);
        when(patientRepository.save(mapped)).thenReturn(saved);
        when(patientMapper.toResponseDTO(saved)).thenReturn(response);

        PatientResponseDTO result = patientService.createPatient(request);

        assertThat(result).isSameAs(response);
        verify(patientRepository).save(mapped);
    }

    @Test
    void updatePatientThrowsWhenNewEmailAlreadyExists() {
        Patient existing = patient(10L, "old@example.com");
        PatientRequestDTO request = validRequest();
        request.setContactInfo(new ContactInfo("new@example.com", "514-555-0101"));

        when(patientRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(patientRepository.existsByContactInfoEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> patientService.updatePatient(10L, request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email already exists: new@example.com");
    }

    @Test
    void updatePatientThrowsWhenPatientDoesNotExist() {
        when(patientRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(10L, validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient not found with id: 10");
    }

    @Test
    void updatePatientPreservesIdentifiersAndReturnsSavedPatient() {
        Patient existing = patient(10L, "old@example.com");
        PatientRequestDTO request = validRequest();
        Patient mapped = patient(null, "john@example.com");
        Patient saved = patient(10L, "john@example.com");
        PatientResponseDTO response = response(10L, "john@example.com");

        when(patientRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(patientMapper.toEntity(request)).thenReturn(mapped);
        when(patientRepository.save(mapped)).thenReturn(saved);
        when(patientMapper.toResponseDTO(saved)).thenReturn(response);

        PatientResponseDTO result = patientService.updatePatient(10L, request);

        assertThat(result).isSameAs(response);
        assertThat(mapped.getId()).isEqualTo(existing.getId());
        assertThat(mapped.getPatientId().getPatientId()).isEqualTo(existing.getPatientId().getPatientId());
    }

    @Test
    void updatePatientAllowsSameEmailWithoutDuplicateLookup() {
        Patient existing = patient(10L, "john@example.com");
        PatientRequestDTO request = validRequest();
        request.setContactInfo(new ContactInfo("john@example.com", "514-555-0101"));
        Patient mapped = patient(null, "john@example.com");
        Patient saved = patient(10L, "john@example.com");
        PatientResponseDTO response = response(10L, "john@example.com");

        when(patientRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(patientMapper.toEntity(request)).thenReturn(mapped);
        when(patientRepository.save(mapped)).thenReturn(saved);
        when(patientMapper.toResponseDTO(saved)).thenReturn(response);

        PatientResponseDTO result = patientService.updatePatient(10L, request);

        assertThat(result).isSameAs(response);
        verify(patientRepository, never()).existsByContactInfoEmail("john@example.com");
    }

    @Test
    void updatePatientAllowsPhoneOnlyWithoutDuplicateLookup() {
        Patient existing = patient(10L, "john@example.com");
        PatientRequestDTO request = validRequest();
        request.setContactInfo(new ContactInfo(null, "514-555-0101"));
        Patient mapped = patient(null, null);
        mapped.setContactInfo(new ContactInfo(null, "514-555-0101"));
        Patient saved = patient(10L, null);
        saved.setContactInfo(new ContactInfo(null, "514-555-0101"));
        PatientResponseDTO response = response(10L, null);
        response.setContactInfo(new ContactInfo(null, "514-555-0101"));

        when(patientRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(patientMapper.toEntity(request)).thenReturn(mapped);
        when(patientRepository.save(mapped)).thenReturn(saved);
        when(patientMapper.toResponseDTO(saved)).thenReturn(response);

        PatientResponseDTO result = patientService.updatePatient(10L, request);

        assertThat(result).isSameAs(response);
        verify(patientRepository, never()).existsByContactInfoEmail(any());
    }

    @Test
    void deletePatientThrowsWhenMissing() {
        when(patientRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.deletePatientById(7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient not found with id: 7");
    }

    @Test
    void deletePatientDeletesEntityAndReturnsMappedResponse() {
        Patient existing = patient(7L, "john@example.com");
        PatientResponseDTO response = response(7L, "john@example.com");

        when(patientRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(patientMapper.toResponseDTO(existing)).thenReturn(response);

        PatientResponseDTO result = patientService.deletePatientById(7L);

        assertThat(result).isSameAs(response);
        verify(patientRepository).delete(existing);
    }

    private PatientRequestDTO validRequest() {
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

    private Patient patient(Long id, String email) {
        return Patient.builder()
                .id(id)
                .patientId(new PatientIdentifier("patient-" + (id == null ? "new" : id)))
                .fullName("John Smith")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("M")
                .contactInfo(new ContactInfo(email, "514-555-0101"))
                .address(new Address("123 Main", "Montreal", "Quebec", "H1H1H1", "Canada"))
                .insuranceNumber("INS-123")
                .allergy(new Allergy("Peanuts", "Rash"))
                .bloodType(BloodType.A)
                .status(PatientStatus.ACTIVE)
                .build();
    }

    private PatientResponseDTO response(Long id, String email) {
        return new PatientResponseDTO(
                id,
                new PatientIdentifier("patient-" + id),
                "John Smith",
                LocalDate.of(1990, 1, 1),
                "M",
                new ContactInfo(email, "514-555-0101"),
                new Address("123 Main", "Montreal", "Quebec", "H1H1H1", "Canada"),
                "INS-123",
                new Allergy("Peanuts", "Rash"),
                BloodType.A,
                PatientStatus.ACTIVE
        );
    }
}
