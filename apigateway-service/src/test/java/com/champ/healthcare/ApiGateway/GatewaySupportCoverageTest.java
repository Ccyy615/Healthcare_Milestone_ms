package com.champ.healthcare.ApiGateway;

import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentResponseDTO;
import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentModelAssembler;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomModelAssembler;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomResponseDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomStatusPatchDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.DoctorModelAssembler;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.DoctorActivationPatchDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.DoctorRequestDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.DoctorResponseDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.LicenseRequestDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.SpecialityRequestDTO;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientModelAssembler;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
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
    void webClientErrorMapperHandlesBlankMissingAndNonJsonMessages() {
        RuntimeException blankMessage = WebClientErrorMapper.map(
                "Patient service",
                exception(HttpStatus.BAD_REQUEST, "{\"message\":\"   \"}")
        );
        RuntimeException missingMessageField = WebClientErrorMapper.map(
                "Patient service",
                exception(HttpStatus.NOT_FOUND, "{\"error\":\"missing\"}")
        );
        RuntimeException plainTextBody = WebClientErrorMapper.map(
                "Doctor service",
                exception(HttpStatus.UNPROCESSABLE_ENTITY, "plain-text failure")
        );

        assertThat(blankMessage).isInstanceOf(InvalidInputException.class)
                .hasMessage("Patient service returned 400.");
        assertThat(missingMessageField).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("{\"error\":\"missing\"}");
        assertThat(plainTextBody).isInstanceOf(ConflictException.class)
                .hasMessage("plain-text failure");
    }

    @Test
    void webClientErrorMapperExtractMessageHandlesNullBodyAndNullMessageField() throws Exception {
        Method extractMessage = WebClientErrorMapper.class.getDeclaredMethod("extractMessage", String.class);
        extractMessage.setAccessible(true);

        assertThat(extractMessage.invoke(null, new Object[]{null})).isNull();
        assertThat(extractMessage.invoke(null, "{\"message\":null}")).isEqualTo("{\"message\":null}");
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

    @Test
    void modelAssemblersReturnNullWhenInputIsNull() {
        assertThat(new AppointmentModelAssembler().addLinks((AppointmentResponseDTO) null)).isNull();
        assertThat(new PatientModelAssembler().addLinks((PatientResponseDTO) null)).isNull();
        assertThat(new DoctorModelAssembler().addLinks((DoctorResponseDTO) null)).isNull();
        assertThat(new ClinicRoomModelAssembler().addLinks((ClinicRoomResponseDTO) null)).isNull();
    }

    @Test
    void patientResponseDtoUnpacksNestedAndScalarGatewayPayloads() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        PatientResponseDTO nested = objectMapper.readValue("""
                {
                  "id": 1,
                  "patientId": { "patientId": "patient-1" },
                  "fullName": "Jordan Miles",
                  "contactInfo": { "email": "jordan@example.com", "phone": "514-555-0100" },
                  "address": {
                    "street": "1 Main",
                    "city": "Montreal",
                    "province": "QC",
                    "postal_code": "H1H1H1",
                    "country": "Canada"
                  },
                  "allergy": { "substance": "Pollen", "reaction": "Sneezing" },
                  "bloodType": "O+",
                  "status": { "status": "ACTIVE" }
                }
                """, PatientResponseDTO.class);

        PatientResponseDTO scalar = objectMapper.readValue("""
                {
                  "id": 2,
                  "patientId": "patient-2",
                  "contactInfo": null,
                  "address": null,
                  "allergy": null,
                  "bloodType": null,
                  "status": "INACTIVE"
                }
                """, PatientResponseDTO.class);

        PatientResponseDTO nullFields = objectMapper.readValue("""
                {
                  "id": 3,
                  "patientId": { "patientId": null },
                  "contactInfo": { "email": null, "phone": null },
                  "address": {
                    "street": null,
                    "city": null,
                    "province": null,
                    "postal_code": null,
                    "country": null
                  },
                  "allergy": { "substance": null, "reaction": null },
                  "bloodType": null,
                  "status": { "status": null }
                }
                """, PatientResponseDTO.class);

        PatientResponseDTO explicitNullScalars = objectMapper.readValue("""
                {
                  "id": 4,
                  "patientId": null,
                  "status": null
                }
                """, PatientResponseDTO.class);

        assertThat(nested.getPatientId()).isEqualTo("patient-1");
        assertThat(nested.getEmail()).isEqualTo("jordan@example.com");
        assertThat(nested.getPhone()).isEqualTo("514-555-0100");
        assertThat(nested.getStreet()).isEqualTo("1 Main");
        assertThat(nested.getCity()).isEqualTo("Montreal");
        assertThat(nested.getProvince()).isEqualTo("QC");
        assertThat(nested.getPostal_code()).isEqualTo("H1H1H1");
        assertThat(nested.getCountry()).isEqualTo("Canada");
        assertThat(nested.getSubstance()).isEqualTo("Pollen");
        assertThat(nested.getReaction()).isEqualTo("Sneezing");
        assertThat(nested.getBloodType()).isEqualTo("O+");
        assertThat(nested.getStatus().getStatus()).isEqualTo("ACTIVE");

        assertThat(scalar.getPatientId()).isEqualTo("patient-2");
        assertThat(scalar.getEmail()).isNull();
        assertThat(scalar.getStreet()).isNull();
        assertThat(scalar.getSubstance()).isNull();
        assertThat(scalar.getBloodType()).isNull();
        assertThat(scalar.getStatus().getStatus()).isEqualTo("INACTIVE");

        assertThat(nullFields.getPatientId()).isNull();
        assertThat(nullFields.getEmail()).isNull();
        assertThat(nullFields.getPhone()).isNull();
        assertThat(nullFields.getStreet()).isNull();
        assertThat(nullFields.getCity()).isNull();
        assertThat(nullFields.getProvince()).isNull();
        assertThat(nullFields.getPostal_code()).isNull();
        assertThat(nullFields.getCountry()).isNull();
        assertThat(nullFields.getSubstance()).isNull();
        assertThat(nullFields.getReaction()).isNull();
        assertThat(nullFields.getStatus()).isNull();

        assertThat(explicitNullScalars.getPatientId()).isNull();
        assertThat(explicitNullScalars.getStatus()).isNull();
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
