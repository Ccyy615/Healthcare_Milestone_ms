package com.champ.healthcare.Doctor.PresentationLayer;

import com.champ.healthcare.Doctor.BusinessLogicLayer.DoctorService;
import com.champ.healthcare.Doctor.Domain.License;
import com.champ.healthcare.Doctor.Domain.LicenseStatus;
import com.champ.healthcare.Doctor.Domain.ProficiencyLevel;
import com.champ.healthcare.Doctor.Domain.Speciality;
import com.champ.healthcare.Doctor.Domain.WorkZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;

    private DoctorController doctorController;

    @BeforeEach
    void setUp() {
        doctorController = new DoctorController(doctorService);
    }

    @Test
    void getAllDoctorsReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.getAllDoctors()).thenReturn(List.of(doctor));

        ResponseEntity<List<DoctorResponseDTO>> response = doctorController.getAllDoctors();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(doctor);
    }

    @Test
    void getDoctorByIdReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.getDoctorById("doctor-1")).thenReturn(doctor);

        ResponseEntity<DoctorResponseDTO> response = doctorController.getDoctorById("doctor-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(doctor);
    }

    @Test
    void createDoctorReturnsCreatedResponse() {
        DoctorRequestDTO request = doctorRequest();
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.createDoctor(request)).thenReturn(doctor);

        ResponseEntity<DoctorResponseDTO> response = doctorController.createDoctor(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/doctors/doctor-1");
        assertThat(response.getBody()).isEqualTo(doctor);
    }

    @Test
    void updateDoctorReturnsOkResponse() {
        DoctorRequestDTO request = doctorRequest();
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.updateDoctor("doctor-1", request)).thenReturn(doctor);

        ResponseEntity<DoctorResponseDTO> response = doctorController.updateDoctor("doctor-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(doctor);
    }

    @Test
    void getActiveDoctorsReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.getActiveDoctors()).thenReturn(List.of(doctor));

        ResponseEntity<List<DoctorResponseDTO>> response = doctorController.getActiveDoctors();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(doctor);
    }

    @Test
    void getActiveDoctorBySpecialityReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.getActiveDoctorBySpeciality("Cardiology")).thenReturn(List.of(doctor));

        ResponseEntity<List<DoctorResponseDTO>> response =
                doctorController.getActiveDoctorBySpeciality("Cardiology");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(doctor);
    }

    @Test
    void activateDoctorReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.activateDoctor("doctor-1")).thenReturn(doctor);

        ResponseEntity<DoctorResponseDTO> response = doctorController.activateDoctor("doctor-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(doctor);
    }

    @Test
    void deactivateDoctorReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.deactivateDoctor("doctor-1")).thenReturn(doctor);

        ResponseEntity<DoctorResponseDTO> response = doctorController.deactivateDoctor("doctor-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(doctor);
    }

    @Test
    void updateDoctorActivationReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.updateDoctorActivation("doctor-1", false)).thenReturn(doctor);

        ResponseEntity<DoctorResponseDTO> response = doctorController.updateDoctorActivation(
                "doctor-1",
                new DoctorActivationPatchDTO(false)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(doctor);
    }

    @Test
    void addSpecialityReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        Speciality speciality = new Speciality("Cardiology", ProficiencyLevel.EXPERT);
        when(doctorService.addSpeciality("doctor-1", speciality)).thenReturn(doctor);

        ResponseEntity<DoctorResponseDTO> response = doctorController.addSpeciality("doctor-1", speciality);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(doctor);
    }

    @Test
    void removeSpecialityReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.removeSpeciality("doctor-1", "Cardiology")).thenReturn(doctor);

        ResponseEntity<DoctorResponseDTO> response =
                doctorController.removeSpeciality("doctor-1", "Cardiology");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(doctor);
    }

    @Test
    void addLicenseReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        LicenseRequestDTO request = new LicenseRequestDTO();
        request.setLicenseName("Practice License");
        request.setStatus(LicenseStatus.VALID);
        when(doctorService.addLicense("doctor-1", request)).thenReturn(doctor);

        ResponseEntity<DoctorResponseDTO> response = doctorController.addLicense("doctor-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(doctor);
    }

    @Test
    void deleteDoctorReturnsOkResponse() {
        DoctorResponseDTO doctor = doctorResponse();
        when(doctorService.getDoctorById("doctor-1")).thenReturn(doctor);

        ResponseEntity<DoctorResponseDTO> response = doctorController.deleteDoctor("doctor-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(doctor);
        verify(doctorService).deleteDoctor("doctor-1");
    }

    private DoctorRequestDTO doctorRequest() {
        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setDoctorFirstName("John");
        request.setDoctorLastName("Smith");
        request.setWorkZone(new WorkZone("Montreal", "Quebec"));
        request.setSpeciality(List.of(new Speciality("Cardiology", ProficiencyLevel.EXPERT)));
        return request;
    }

    private DoctorResponseDTO doctorResponse() {
        DoctorResponseDTO response = new DoctorResponseDTO();
        response.setDoctorId("doctor-1");
        response.setDoctorFirstName("John");
        response.setDoctorLastName("Smith");
        response.setIsActive(true);
        response.setIsValid(true);
        response.setWorkZone(new WorkZone("Montreal", "Quebec"));
        response.setSpeciality(List.of(new Speciality("Cardiology", ProficiencyLevel.EXPERT)));
        response.setLicense(new License("Practice License", LicenseStatus.VALID, LocalDateTime.now().minusDays(1)));
        return response;
    }
}
