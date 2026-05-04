package com.champ.healthcare.ApiGateway.Doctor.PresentationLayer;

import com.champ.healthcare.ApiGateway.Doctor.BusinessLogicLayer.DoctorService;
import com.champ.healthcare.ApiGateway.utilities.ResourceNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;

    private DoctorModelAssembler doctorModelAssembler;
    private DoctorController doctorController;

    @BeforeEach
    void setUp() {
        doctorModelAssembler = new DoctorModelAssembler();
        doctorController = new DoctorController(doctorService, doctorModelAssembler);
    }

    @Test
    void getAllDoctorsReturnsOk() {
        when(doctorService.getAllDoctors()).thenReturn(List.of(doctorResponse()));

        assertThat(doctorController.getAllDoctors().getBody()).hasSize(1);
        assertThat(doctorController.getAllDoctors().getBody().get(0).getRequiredLink("self").getHref())
                .contains("/api/v1/doctors/doctor-1");
    }

    @Test
    void getDoctorByIdReturnsOk() {
        when(doctorService.getDoctorById("doctor-1")).thenReturn(doctorResponse());

        assertThat(doctorController.getDoctorById("doctor-1").getBody().getDoctorFirstName()).isEqualTo("Avery");
    }

    @Test
    void createDoctorReturnsCreated() {
        DoctorRequestDTO requestDTO = doctorRequest();
        when(doctorService.createDoctor(requestDTO)).thenReturn(doctorResponse());

        ResponseEntity<DoctorResponseDTO> response = doctorController.createDoctor(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/doctors/doctor-1");
        assertThat(response.getBody().getRequiredLink("activate").getHref()).contains("/api/v1/doctors/doctor-1/activate");
    }

    @Test
    void updateDoctorReturnsOk() {
        DoctorRequestDTO requestDTO = doctorRequest();
        when(doctorService.updateDoctor("doctor-1", requestDTO)).thenReturn(doctorResponse());

        assertThat(doctorController.updateDoctor("doctor-1", requestDTO).getBody().getDoctorLastName())
                .isEqualTo("Stone");
    }

    @Test
    void activeEndpointsReturnOk() {
        when(doctorService.getActiveDoctors()).thenReturn(List.of(doctorResponse()));
        when(doctorService.getActiveDoctorBySpeciality("Cardiology")).thenReturn(List.of(doctorResponse()));

        assertThat(doctorController.getActiveDoctors().getBody()).hasSize(1);
        assertThat(doctorController.getActiveDoctorBySpeciality("Cardiology").getBody()).hasSize(1);
    }

    @Test
    void activationEndpointsReturnOk() {
        when(doctorService.activateDoctor("doctor-1")).thenReturn(doctorResponse());
        when(doctorService.deactivateDoctor("doctor-1")).thenReturn(doctorResponse());
        when(doctorService.updateDoctorActivation("doctor-1", true)).thenReturn(doctorResponse());

        assertThat(doctorController.activateDoctor("doctor-1").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(doctorController.deactivateDoctor("doctor-1").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(doctorController.updateDoctorActivation("doctor-1", new DoctorActivationPatchDTO(true))
                .getBody()
                .getDoctorId()).isEqualTo("doctor-1");
    }

    @Test
    void specialityAndLicenseEndpointsReturnOk() {
        SpecialityRequestDTO specialityRequestDTO = new SpecialityRequestDTO("Cardiology", "Advanced");
        LicenseRequestDTO licenseRequestDTO = new LicenseRequestDTO();
        licenseRequestDTO.setLicenseName("LIC-1");
        licenseRequestDTO.setStatus("ACTIVE");
        licenseRequestDTO.setPerformedDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        when(doctorService.addSpeciality("doctor-1", specialityRequestDTO)).thenReturn(doctorResponse());
        when(doctorService.removeSpeciality("doctor-1", "Cardiology")).thenReturn(doctorResponse());
        when(doctorService.addLicense("doctor-1", licenseRequestDTO)).thenReturn(doctorResponse());

        assertThat(doctorController.addSpeciality("doctor-1", specialityRequestDTO).getBody().getDoctorId())
                .isEqualTo("doctor-1");
        assertThat(doctorController.removeSpeciality("doctor-1", "Cardiology").getBody().getDoctorId())
                .isEqualTo("doctor-1");
        assertThat(doctorController.addLicense("doctor-1", licenseRequestDTO).getBody().getLicenseName())
                .isEqualTo("College");
    }

    @Test
    void deleteDoctorReturnsFetchedDoctor() {
        when(doctorService.getDoctorById("doctor-1")).thenReturn(doctorResponse());

        assertThat(doctorController.deleteDoctor("doctor-1").getBody().getDoctorId()).isEqualTo("doctor-1");
    }

    @Test
    void getDoctorByIdPropagatesNegativePath() {
        when(doctorService.getDoctorById("doctor-404")).thenThrow(new ResourceNotFoundException("missing doctor"));

        assertThatThrownBy(() -> doctorController.getDoctorById("doctor-404"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("missing doctor");
    }

    private DoctorRequestDTO doctorRequest() {
        return new DoctorRequestDTO(
                "Avery",
                "Stone",
                "Montreal",
                "QC",
                new SpecialityRequestDTO("Cardiology", "Advanced")
        );
    }

    private DoctorResponseDTO doctorResponse() {
        return new DoctorResponseDTO(
                "doctor-1",
                "Avery",
                "Stone",
                true,
                true,
                new SpecialityRequestDTO("Cardiology", "Advanced"),
                "Montreal",
                "QC",
                10L,
                "College",
                "ACTIVE",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2027, 1, 1, 10, 0)
        );
    }
}
