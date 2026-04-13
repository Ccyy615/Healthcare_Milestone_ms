package com.champ.healthcare.Doctor.PresentationLayer;

import com.champ.healthcare.Doctor.BusinessLogicLayer.DoctorService;
import com.champ.healthcare.Doctor.Domain.LicenseStatus;
import com.champ.healthcare.Doctor.Domain.ProficiencyLevel;
import com.champ.healthcare.Doctor.Domain.Speciality;
import com.champ.healthcare.Doctor.Domain.WorkZone;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;

    @Mock
    private DoctorModelAssembler doctorModelAssembler;

    private DoctorController doctorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doctorController = new DoctorController(doctorService, doctorModelAssembler);
    }

    @Test
    void getAllDoctorsReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        CollectionModel<EntityModel<DoctorResponseDTO>> model = CollectionModel.of(List.of(EntityModel.of(doctor)));

        when(doctorService.getAllDoctors()).thenReturn(List.of(doctor));
        when(doctorModelAssembler.toCollectionModel(List.of(doctor))).thenReturn(model);

        ResponseEntity<CollectionModel<EntityModel<DoctorResponseDTO>>> response = doctorController.getAllDoctors();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void getDoctorByIdReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        EntityModel<DoctorResponseDTO> model = EntityModel.of(doctor);

        when(doctorService.getDoctorById("doctor-1")).thenReturn(doctor);
        when(doctorModelAssembler.toModel(doctor)).thenReturn(model);

        ResponseEntity<EntityModel<DoctorResponseDTO>> response = doctorController.getDoctorById("doctor-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void createDoctorReturnsCreatedResponse() {
        DoctorRequestDTO request = doctorRequest();
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        EntityModel<DoctorResponseDTO> model = EntityModel.of(doctor);

        when(doctorService.createDoctor(request)).thenReturn(doctor);
        when(doctorModelAssembler.toModel(doctor)).thenReturn(model);

        ResponseEntity<EntityModel<DoctorResponseDTO>> response = doctorController.createDoctor(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(model);
        assertThat(response.getHeaders().getLocation()).isNotNull();
    }

    @Test
    void updateDoctorReturnsOkResponse() {
        DoctorRequestDTO request = doctorRequest();
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        EntityModel<DoctorResponseDTO> model = EntityModel.of(doctor);

        when(doctorService.updateDoctor("doctor-1", request)).thenReturn(doctor);
        when(doctorModelAssembler.toModel(doctor)).thenReturn(model);

        ResponseEntity<EntityModel<DoctorResponseDTO>> response = doctorController.updateDoctor("doctor-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void getActiveDoctorsReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        CollectionModel<EntityModel<DoctorResponseDTO>> model = CollectionModel.of(List.of(EntityModel.of(doctor)));

        when(doctorService.getActiveDoctors()).thenReturn(List.of(doctor));
        when(doctorModelAssembler.toCollectionModel(List.of(doctor))).thenReturn(model);

        ResponseEntity<CollectionModel<EntityModel<DoctorResponseDTO>>> response = doctorController.getActiveDoctors();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void getActiveDoctorBySpecialityReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        CollectionModel<EntityModel<DoctorResponseDTO>> model = CollectionModel.of(List.of(EntityModel.of(doctor)));

        when(doctorService.getActiveDoctorBySpeciality("Cardiology")).thenReturn(List.of(doctor));
        when(doctorModelAssembler.toCollectionModel(List.of(doctor))).thenReturn(model);

        ResponseEntity<CollectionModel<EntityModel<DoctorResponseDTO>>> response =
                doctorController.getActiveDoctorBySpeciality("Cardiology");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void activateDoctorReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        EntityModel<DoctorResponseDTO> model = EntityModel.of(doctor);

        when(doctorService.activateDoctor("doctor-1")).thenReturn(doctor);
        when(doctorModelAssembler.toModel(doctor)).thenReturn(model);

        ResponseEntity<EntityModel<DoctorResponseDTO>> response = doctorController.activateDoctor("doctor-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void deactivateDoctorReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        EntityModel<DoctorResponseDTO> model = EntityModel.of(doctor);

        when(doctorService.deactivateDoctor("doctor-1")).thenReturn(doctor);
        when(doctorModelAssembler.toModel(doctor)).thenReturn(model);

        ResponseEntity<EntityModel<DoctorResponseDTO>> response = doctorController.deactivateDoctor("doctor-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void addSpecialityReturnsOkResponse() {
        Speciality speciality = new Speciality("Cardiology", ProficiencyLevel.EXPERT);
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        EntityModel<DoctorResponseDTO> model = EntityModel.of(doctor);

        when(doctorService.addSpeciality("doctor-1", speciality)).thenReturn(doctor);
        when(doctorModelAssembler.toModel(doctor)).thenReturn(model);

        ResponseEntity<EntityModel<DoctorResponseDTO>> response = doctorController.addSpeciality("doctor-1", speciality);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void removeSpecialityReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        EntityModel<DoctorResponseDTO> model = EntityModel.of(doctor);

        when(doctorService.removeSpeciality("doctor-1", "Cardiology")).thenReturn(doctor);
        when(doctorModelAssembler.toModel(doctor)).thenReturn(model);

        ResponseEntity<EntityModel<DoctorResponseDTO>> response =
                doctorController.removeSpeciality("doctor-1", "Cardiology");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void addLicenseReturnsOkResponse() {
        LicenseRequestDTO request = new LicenseRequestDTO();
        request.setLicenseName("Practice License");
        request.setStatus(LicenseStatus.VALID);
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        EntityModel<DoctorResponseDTO> model = EntityModel.of(doctor);

        when(doctorService.addLicense("doctor-1", request)).thenReturn(doctor);
        when(doctorModelAssembler.toModel(doctor)).thenReturn(model);

        ResponseEntity<EntityModel<DoctorResponseDTO>> response = doctorController.addLicense("doctor-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    @Test
    void deleteDoctorReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse("doctor-1");
        EntityModel<DoctorResponseDTO> model = EntityModel.of(doctor);

        when(doctorService.getDoctorById("doctor-1")).thenReturn(doctor);
        doNothing().when(doctorService).deleteDoctor("doctor-1");
        when(doctorModelAssembler.toModel(doctor)).thenReturn(model);

        ResponseEntity<EntityModel<DoctorResponseDTO>> response = doctorController.deleteDoctor("doctor-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(model);
    }

    private DoctorRequestDTO doctorRequest() {
        return new DoctorRequestDTO(
                "John",
                "Smith",
                new WorkZone("Montreal", "Quebec"),
                List.of(new Speciality("Cardiology", ProficiencyLevel.ADVANCED))
        );
    }

    private DoctorResponseDTO doctorResponse(String doctorId) {
        DoctorResponseDTO response = new DoctorResponseDTO();
        response.setDoctorId(doctorId);
        response.setDoctorFirstName("John");
        response.setDoctorLastName("Smith");
        response.setIsActive(false);
        response.setIsValid(false);
        return response;
    }
}
