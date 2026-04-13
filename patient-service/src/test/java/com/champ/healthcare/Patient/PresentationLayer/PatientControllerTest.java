package com.champ.healthcare.Patient.PresentationLayer;

import com.champ.healthcare.Patient.BusinessLogicLayer.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @Mock
    private PatientModelAssembler patientModelAssembler;

    private PatientController patientController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientController = new PatientController(patientService, patientModelAssembler);
    }

    @Test
    void getAllPatientsReturnsOkResponse() {
        PatientResponseDTO patient = new PatientResponseDTO();
        CollectionModel<EntityModel<PatientResponseDTO>> model = CollectionModel.of(List.of(EntityModel.of(patient)));

        when(patientService.getAllPatients()).thenReturn(List.of(patient));
        when(patientModelAssembler.toCollectionModel(List.of(patient))).thenReturn(model);

        ResponseEntity<CollectionModel<EntityModel<PatientResponseDTO>>> response = patientController.getAllPatients();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void getPatientByIdReturnsOkResponse() {
        PatientResponseDTO patient = new PatientResponseDTO();
        EntityModel<PatientResponseDTO> model = EntityModel.of(patient);

        when(patientService.getPatientById(3L)).thenReturn(patient);
        when(patientModelAssembler.toModel(patient)).thenReturn(model);

        ResponseEntity<EntityModel<PatientResponseDTO>> response = patientController.getPatientById(3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void createPatientReturnsCreatedResponse() {
        PatientRequestDTO request = new PatientRequestDTO();
        PatientResponseDTO created = new PatientResponseDTO();
        created.setId(12L);
        EntityModel<PatientResponseDTO> model = EntityModel.of(created);

        when(patientService.createPatient(request)).thenReturn(created);
        when(patientModelAssembler.toModel(created)).thenReturn(model);

        ResponseEntity<EntityModel<PatientResponseDTO>> response = patientController.createPatient(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(model);
        assertThat(response.getHeaders().getLocation()).isNotNull();
    }

    @Test
    void updatePatientReturnsOkResponse() {
        PatientRequestDTO request = new PatientRequestDTO();
        PatientResponseDTO updated = new PatientResponseDTO();
        EntityModel<PatientResponseDTO> model = EntityModel.of(updated);

        when(patientService.updatePatient(4L, request)).thenReturn(updated);
        when(patientModelAssembler.toModel(updated)).thenReturn(model);

        ResponseEntity<EntityModel<PatientResponseDTO>> response = patientController.updatePatient(4L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void deletePatientReturnsOkResponse() {
        PatientResponseDTO deleted = new PatientResponseDTO();
        EntityModel<PatientResponseDTO> model = EntityModel.of(deleted);

        when(patientService.deletePatientById(5L)).thenReturn(deleted);
        when(patientModelAssembler.toModel(deleted)).thenReturn(model);

        ResponseEntity<EntityModel<PatientResponseDTO>> response = patientController.deletePatientById(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }
}
