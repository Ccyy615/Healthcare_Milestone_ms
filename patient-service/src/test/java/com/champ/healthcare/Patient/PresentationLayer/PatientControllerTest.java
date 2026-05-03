package com.champ.healthcare.Patient.PresentationLayer;

import com.champ.healthcare.Patient.BusinessLogicLayer.PatientService;
import com.champ.healthcare.Patient.Domain.Address;
import com.champ.healthcare.Patient.Domain.Allergy;
import com.champ.healthcare.Patient.Domain.BloodType;
import com.champ.healthcare.Patient.Domain.ContactInfo;
import com.champ.healthcare.Patient.Domain.PatientIdentifier;
import com.champ.healthcare.Patient.Domain.PatientStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    private PatientController patientController;

    @BeforeEach
    void setUp() {
        patientController = new PatientController(patientService);
    }

    @Test
    void getAllPatientsReturnsOkResponse() {
        PatientResponseDTO patient = patientResponse();
        when(patientService.getAllPatients()).thenReturn(List.of(patient));

        ResponseEntity<List<PatientResponseDTO>> response = patientController.getAllPatients();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(patient);
    }

    @Test
    void getPatientByIdReturnsOkResponse() {
        PatientResponseDTO patient = patientResponse();
        when(patientService.getPatientById(3L)).thenReturn(patient);

        ResponseEntity<PatientResponseDTO> response = patientController.getPatientById(3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(patient);
    }

    @Test
    void getPatientByPatientIdentifierReturnsOkResponse() {
        PatientResponseDTO patient = patientResponse();
        when(patientService.getPatientByPatientIdentifier("patient-1")).thenReturn(patient);

        ResponseEntity<PatientResponseDTO> response =
                patientController.getPatientByPatientIdentifier("patient-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(patient);
    }

    @Test
    void createPatientReturnsCreatedResponse() {
        PatientRequestDTO request = patientRequest();
        PatientResponseDTO created = patientResponse();
        when(patientService.createPatient(request)).thenReturn(created);

        ResponseEntity<PatientResponseDTO> response = patientController.createPatient(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/patients/3");
        assertThat(response.getBody()).isEqualTo(created);
    }

    @Test
    void updatePatientReturnsOkResponse() {
        PatientRequestDTO request = patientRequest();
        PatientResponseDTO updated = patientResponse();
        when(patientService.updatePatient(4L, request)).thenReturn(updated);

        ResponseEntity<PatientResponseDTO> response = patientController.updatePatient(4L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(updated);
    }

    @Test
    void updatePatientStatusReturnsOkResponse() {
        PatientResponseDTO updated = patientResponse();
        when(patientService.updatePatientStatus(4L, PatientStatus.INACTIVE)).thenReturn(updated);

        ResponseEntity<PatientResponseDTO> response =
                patientController.updatePatientStatus(4L, new PatientStatusPatchDTO(PatientStatus.INACTIVE));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(updated);
    }

    @Test
    void deletePatientReturnsOkResponse() {
        PatientResponseDTO deleted = patientResponse();
        when(patientService.deletePatientById(5L)).thenReturn(deleted);

        ResponseEntity<PatientResponseDTO> response = patientController.deletePatientById(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(deleted);
    }

    private PatientRequestDTO patientRequest() {
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

    private PatientResponseDTO patientResponse() {
        return new PatientResponseDTO(
                3L,
                new PatientIdentifier("patient-1"),
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
}
