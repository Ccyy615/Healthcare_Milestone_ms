package com.champ.healthcare.ApiGateway;

import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentResponseDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomResponseDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomStatusPatchDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.DoctorActivationPatchDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.DoctorRequestDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.DoctorResponseDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.LicenseRequestDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.SpecialityRequestDTO;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientRequestDTO;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientResponseDTO;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientStatusPatchDTO;
import com.champ.healthcare.ApiGateway.utilities.ApiErrorResponse;
import com.champ.healthcare.ApiGateway.utilities.ConflictException;
import com.champ.healthcare.ApiGateway.utilities.DownstreamServiceException;
import com.champ.healthcare.ApiGateway.utilities.GlobalExceptionHandler;
import com.champ.healthcare.ApiGateway.utilities.InvalidInputException;
import com.champ.healthcare.ApiGateway.utilities.ResourceNotFoundException;
import com.champ.healthcare.ApiGateway.utilities.WebClientErrorMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class GatewaySupportCoverageTest {

    @Test
    void mainDelegatesToSpringApplication() {
        String[] args = {"--spring.profiles.active=test"};

        try (var springApplication = mockStatic(SpringApplication.class)) {
            ApigatewayServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(ApigatewayServiceApplication.class, args));
        }
    }

    @Test
    void webClientConfigCreatesBuilder() {
        WebClient.Builder builder = new WebClientConfig().webClientBuilder();

        assertThat(builder).isNotNull();
    }

    @Test
    void webClientErrorMapperMapsStatusesAndMessages() {
        RuntimeException notFound = WebClientErrorMapper.map(
                "Patient service",
                exception(HttpStatus.NOT_FOUND, "{\"message\":\"patient missing\"}")
        );
        RuntimeException invalidInput = WebClientErrorMapper.map(
                "Patient service",
                exception(HttpStatus.BAD_REQUEST, "{\"message\":\"bad patient\"}")
        );
        RuntimeException conflict = WebClientErrorMapper.map(
                "Doctor service",
                exception(HttpStatus.CONFLICT, "{\"message\":\"duplicate doctor\"}")
        );
        RuntimeException downstream = WebClientErrorMapper.map(
                "Clinic room service",
                exception(HttpStatus.INTERNAL_SERVER_ERROR, "")
        );

        assertThat(notFound).isInstanceOf(ResourceNotFoundException.class).hasMessage("patient missing");
        assertThat(invalidInput).isInstanceOf(InvalidInputException.class).hasMessage("bad patient");
        assertThat(conflict).isInstanceOf(ConflictException.class).hasMessage("duplicate doctor");
        assertThat(downstream).isInstanceOf(DownstreamServiceException.class)
                .hasMessage("Clinic room service returned 500.");
    }

    @Test
    void globalExceptionHandlerBuildsExpectedResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/patients/1");

        ApiErrorResponse notFound = handler.handleResourceNotFound(
                new ResourceNotFoundException("missing"),
                request
        ).getBody();
        ApiErrorResponse invalidInput = handler.handleInvalidInput(
                new InvalidInputException("bad request"),
                request
        ).getBody();
        ApiErrorResponse conflict = handler.handleConflict(
                new ConflictException("already exists"),
                request
        ).getBody();
        ApiErrorResponse downstream = handler.handleDownstreamService(
                new DownstreamServiceException("downstream failed"),
                request
        ).getBody();

        assertThat(notFound.getStatus()).isEqualTo(404);
        assertThat(invalidInput.getStatus()).isEqualTo(400);
        assertThat(conflict.getStatus()).isEqualTo(409);
        assertThat(downstream.getStatus()).isEqualTo(502);
        assertThat(notFound.getPath()).isEqualTo("/api/v1/patients/1");
    }

    @Test
    void dtoTypesRetainAssignedValues() {
        PatientStatusPatchDTO patientStatus = new PatientStatusPatchDTO("ACTIVE");
        PatientRequestDTO patientRequest = new PatientRequestDTO(
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
                patientStatus
        );
        PatientResponseDTO patientResponse = new PatientResponseDTO(
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
                patientStatus
        );
        DoctorActivationPatchDTO doctorActivationPatchDTO = new DoctorActivationPatchDTO(true);
        SpecialityRequestDTO specialityRequestDTO = new SpecialityRequestDTO("Cardiology", "Advanced");
        LicenseRequestDTO licenseRequestDTO = new LicenseRequestDTO();
        licenseRequestDTO.setLicenseName("LIC-1");
        licenseRequestDTO.setStatus("ACTIVE");
        licenseRequestDTO.setPerformedDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        DoctorRequestDTO doctorRequestDTO = new DoctorRequestDTO(
                "Avery",
                "Stone",
                "Montreal",
                "QC",
                specialityRequestDTO
        );
        DoctorResponseDTO doctorResponseDTO = new DoctorResponseDTO(
                "doctor-1",
                "Avery",
                "Stone",
                true,
                true,
                specialityRequestDTO,
                "Montreal",
                "QC",
                10L,
                "College",
                "ACTIVE",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2027, 1, 1, 10, 0)
        );
        ClinicRoomStatusPatchDTO clinicRoomStatus = new ClinicRoomStatusPatchDTO("AVAILABLE");
        ClinicRoomRequestDTO clinicRoomRequestDTO = ClinicRoomRequestDTO.builder()
                .roomName("Consultation Room A")
                .roomNumber("101")
                .roomStatus(clinicRoomStatus)
                .build();
        ClinicRoomResponseDTO clinicRoomResponseDTO = ClinicRoomResponseDTO.builder()
                .id(1L)
                .roomId("room-1")
                .roomName("Consultation Room A")
                .roomNumber("101")
                .roomStatus(clinicRoomStatus)
                .build();
        AppointmentRequestDTO appointmentRequestDTO = AppointmentRequestDTO.builder()
                .patientId("patient-1")
                .doctorId("doctor-1")
                .roomId("room-1")
                .startTime(LocalDateTime.of(2026, 5, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 5, 10, 10, 0))
                .description("Annual checkup")
                .build();
        AppointmentResponseDTO appointmentResponseDTO = AppointmentResponseDTO.builder()
                .appointmentId(10L)
                .patientId("patient-1")
                .patientFullName("Jordan Miles")
                .patientEmail("jordan@example.com")
                .doctorId("doctor-1")
                .doctorFullName("Avery Stone")
                .roomId("room-1")
                .roomName("Consultation Room A")
                .roomNumber("101")
                .roomStatus("AVAILABLE")
                .status("CONFIRMED")
                .createdAt(LocalDateTime.of(2026, 5, 1, 9, 0))
                .startTime(LocalDateTime.of(2026, 5, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 5, 10, 10, 0))
                .description("Annual checkup")
                .build();

        assertThat(patientRequest.getFullName()).isEqualTo("Jordan Miles");
        assertThat(patientResponse.getPatientId()).isEqualTo("patient-1");
        assertThat(doctorActivationPatchDTO.getActive()).isTrue();
        assertThat(specialityRequestDTO.getSpeciality()).isEqualTo("Cardiology");
        assertThat(licenseRequestDTO.getLicenseName()).isEqualTo("LIC-1");
        assertThat(doctorRequestDTO.getDoctorFirstName()).isEqualTo("Avery");
        assertThat(doctorResponseDTO.getDoctorId()).isEqualTo("doctor-1");
        assertThat(clinicRoomRequestDTO.getRoomNumber()).isEqualTo("101");
        assertThat(clinicRoomResponseDTO.getRoomId()).isEqualTo("room-1");
        assertThat(appointmentRequestDTO.getDescription()).isEqualTo("Annual checkup");
        assertThat(appointmentResponseDTO.getAppointmentId()).isEqualTo(10L);
    }

    private WebClientResponseException exception(HttpStatus status, String body) {
        return WebClientResponseException.create(
                status.value(),
                status.getReasonPhrase(),
                HttpHeaders.EMPTY,
                body.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
    }
}
