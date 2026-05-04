package com.champ.healthcare.ApiGateway.Patient.PresentationLayer;

import com.champ.healthcare.ApiGateway.Patient.BusinessLogicLayer.PatientService;
import com.champ.healthcare.ApiGateway.utilities.ResourceNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    private PatientModelAssembler patientModelAssembler;
    private PatientController patientController;

    @BeforeEach
    void setUp() {
        patientModelAssembler = new PatientModelAssembler();
        patientController = new PatientController(patientService, patientModelAssembler);
    }

    @Test
    void getAllPatientsReturnsOk() {
        when(patientService.getAllPatients()).thenReturn(List.of(patientResponse()));

        ResponseEntity<List<PatientResponseDTO>> response = patientController.getAllPatients();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getRequiredLink("self").getHref()).contains("/api/v1/patients/1");
    }

    @Test
    void getPatientByIdReturnsOk() {
        when(patientService.getPatientById(1L)).thenReturn(patientResponse());

        assertThat(patientController.getPatientById(1L).getBody().getRequiredLink("delete").getHref())
                .contains("/api/v1/patients/1");
    }

    @Test
    void getPatientByPatientIdentifierReturnsOk() {
        when(patientService.getPatientByPatientIdentifier("patient-1")).thenReturn(patientResponse());

        assertThat(patientController.getPatientByPatientIdentifier("patient-1").getBody().getFullName())
                .isEqualTo("Jordan Miles");
    }

    @Test
    void createPatientReturnsCreated() {
        PatientRequestDTO request = patientRequest();
        when(patientService.createPatient(request)).thenReturn(patientResponse());

        ResponseEntity<PatientResponseDTO> response = patientController.createPatient(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/patients/1");
        assertThat(response.getBody().getRequiredLink("status").getHref()).contains("/api/v1/patients/1/status");
    }

    @Test
    void updatePatientReturnsOk() {
        PatientRequestDTO request = patientRequest();
        when(patientService.updatePatient(1L, request)).thenReturn(patientResponse());

        assertThat(patientController.updatePatient(1L, request).getBody().getEmail()).isEqualTo("jordan@example.com");
    }

    @Test
    void updatePatientStatusReturnsOk() {
        PatientStatusPatchDTO patchDTO = new PatientStatusPatchDTO("ACTIVE");
        when(patientService.updatePatientStatus(1L, patchDTO)).thenReturn(patientResponse());

        assertThat(patientController.updatePatientStatus(1L, patchDTO).getBody().getStatus().getStatus())
                .isEqualTo("ACTIVE");
    }

    @Test
    void deletePatientReturnsOk() {
        when(patientService.deletePatientById(1L)).thenReturn(patientResponse());

        assertThat(patientController.deletePatientById(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getPatientByIdPropagatesNegativePath() {
        when(patientService.getPatientById(404L)).thenThrow(new ResourceNotFoundException("missing patient"));

        assertThatThrownBy(() -> patientController.getPatientById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("missing patient");
    }

    private PatientRequestDTO patientRequest() {
        return new PatientRequestDTO(
                "Jordan Miles",
                LocalDate.of(1990, 5, 1),
                "F",
                "jordan@example.com",
                "514-555-0100",
                "1 Main",
                "Montreal",
                "QC",
                "H1H1H1",
                "Canada",
                "INS-1",
                "Pollen",
                "Sneezing",
                "O+",
                new PatientStatusPatchDTO("ACTIVE")
        );
    }

    private PatientResponseDTO patientResponse() {
        return new PatientResponseDTO(
                1L,
                "patient-1",
                "Jordan Miles",
                LocalDate.of(1990, 5, 1),
                "F",
                "jordan@example.com",
                "514-555-0100",
                "1 Main",
                "Montreal",
                "QC",
                "H1H1H1",
                "Canada",
                "INS-1",
                "Pollen",
                "Sneezing",
                "O+",
                new PatientStatusPatchDTO("ACTIVE")
        );
    }
}
