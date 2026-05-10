package com.champ.healthcare.Appointment;

import com.champ.healthcare.Appointment.Domain.Appointment;
import com.champ.healthcare.Appointment.Domain.AppointmentStatus;
import com.champ.healthcare.Appointment.Domain.MedicalNote;
import com.champ.healthcare.Appointment.Domain.NoteType;
import com.champ.healthcare.Appointment.Domain.TimeSlot;
import com.champ.healthcare.Appointment.DomainClientLayer.ClinicRoomClientResponse;
import com.champ.healthcare.Appointment.DomainClientLayer.ClinicRoomServiceClientImpl;
import com.champ.healthcare.Appointment.DomainClientLayer.DoctorClientResponse;
import com.champ.healthcare.Appointment.DomainClientLayer.DoctorServiceClientImpl;
import com.champ.healthcare.Appointment.DomainClientLayer.PatientClientResponse;
import com.champ.healthcare.Appointment.DomainClientLayer.PatientServiceClientImpl;
import com.champ.healthcare.Appointment.Mapper.AppointmentMapper;
import com.champ.healthcare.Appointment.Mapper.MedicalNoteMapper;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentModelAssembler;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.Appointment.PresentationLayer.AppointmentResponseDTO;
import com.champ.healthcare.Appointment.PresentationLayer.MedicalNoteRequestDTO;
import com.champ.healthcare.Appointment.PresentationLayer.MedicalNoteResponseDTO;
import com.champ.healthcare.Appointment.utilities.ApiErrorResponse;
import com.champ.healthcare.Appointment.utilities.AppointmentConflictException;
import com.champ.healthcare.Appointment.utilities.DoctorNotEligibleException;
import com.champ.healthcare.Appointment.utilities.DuplicateEmailException;
import com.champ.healthcare.Appointment.utilities.DownstreamServiceException;
import com.champ.healthcare.Appointment.utilities.GlobalExceptionHandler;
import com.champ.healthcare.Appointment.utilities.InvalidInputException;
import com.champ.healthcare.Appointment.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AppointmentSupportCoverageTest {

    @Test
    void mainDelegatesToSpringApplication() {
        String[] args = {"--spring.profiles.active=test"};

        try (var springApplication = mockStatic(SpringApplication.class)) {
            AppointmentServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(AppointmentServiceApplication.class, args));
        }
    }

    @Test
    void webClientConfigCreatesBuilder() {
        assertThat(new WebClientConfig().webClientBuilder()).isNotNull();
    }

    @Test
    void mappersDomainHelpersAndDtosBehaveAsExpected() {
        AppointmentMapper mapper = new AppointmentMapper();
        AppointmentRequestDTO appointmentRequestDTO = AppointmentRequestDTO.builder()
                .patientId("patient-1")
                .doctorId("doctor-1")
                .roomId("room-1")
                .startTime(LocalDateTime.of(2026, 5, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 5, 10, 10, 0))
                .description("Annual checkup")
                .build();
        Appointment appointment = AppointmentMapper.toEntity(appointmentRequestDTO);
        AppointmentResponseDTO appointmentResponseDTO = AppointmentMapper.toResponseDTO(appointment);
        AppointmentResponseDTO appointmentWithoutStatusOrSlot = AppointmentMapper.toResponseDTO(
                Appointment.builder()
                        .appointmentId(99L)
                        .patientId("patient-9")
                        .doctorId("doctor-9")
                        .roomId("room-9")
                        .createdAt(LocalDateTime.of(2026, 5, 1, 8, 0))
                        .description("No status yet")
                        .build()
        );
        Appointment otherAppointment = Appointment.builder()
                .timeSlot(new TimeSlot(LocalDateTime.of(2026, 5, 10, 9, 30), LocalDateTime.of(2026, 5, 10, 10, 30)))
                .build();
        MedicalNoteRequestDTO medicalNoteRequestDTO = MedicalNoteRequestDTO.builder()
                .appointmentId(1L)
                .doctorId("doctor-1")
                .patientId("patient-1")
                .noteText("Consultation summary")
                .createdAt(LocalDateTime.of(2026, 5, 1, 9, 0))
                .lastUpdatedAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .noteType(NoteType.CONSULTATION)
                .build();
        MedicalNote medicalNote = MedicalNoteMapper.toEntity(medicalNoteRequestDTO, appointment);
        MedicalNoteResponseDTO medicalNoteResponseDTO = MedicalNoteMapper.toResponseDTO(medicalNote);
        AppointmentRequestDTO explicitStatusRequest = AppointmentRequestDTO.builder()
                .patientId("patient-2")
                .doctorId("doctor-2")
                .roomId("room-2")
                .status(AppointmentStatus.CANCELLED)
                .startTime(LocalDateTime.of(2026, 5, 11, 9, 0))
                .endTime(LocalDateTime.of(2026, 5, 11, 10, 0))
                .description("Explicit status")
                .build();
        Appointment explicitStatusAppointment = AppointmentMapper.toEntity(explicitStatusRequest);
        Appointment appointmentWithMissingOtherTimeSlot = Appointment.builder()
                .timeSlot(new TimeSlot(LocalDateTime.of(2026, 5, 10, 12, 0), LocalDateTime.of(2026, 5, 10, 13, 0)))
                .build();

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(mapper).isNotNull();
        assertThat(explicitStatusAppointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(appointmentResponseDTO.getPatientId()).isEqualTo("patient-1");
        assertThat(appointmentWithoutStatusOrSlot.getStatus()).isNull();
        assertThat(appointmentWithoutStatusOrSlot.getStartTime()).isNull();
        assertThat(AppointmentMapper.toResponseDTO(null)).isNull();
        assertThat(AppointmentMapper.toResponseDTOList(List.of(appointment))).hasSize(1);
        assertThat(appointment.overlapsWith(otherAppointment)).isTrue();
        assertThat(appointment.overlapsWith(null)).isFalse();
        assertThat(Appointment.builder().build().overlapsWith(otherAppointment)).isFalse();
        assertThat(appointmentWithMissingOtherTimeSlot.overlapsWith(Appointment.builder().build())).isFalse();
        assertThatThrownBy(() -> Appointment.builder().build().validateTimeSlot())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Appointment time slot is required.");
        assertThat(new TimeSlot(LocalDateTime.of(2026, 5, 10, 9, 0), LocalDateTime.of(2026, 5, 10, 10, 0))
                .overlaps(new TimeSlot(LocalDateTime.of(2026, 5, 10, 9, 30), LocalDateTime.of(2026, 5, 10, 10, 30))))
                .isTrue();
        assertThat(new TimeSlot(LocalDateTime.of(2026, 5, 10, 11, 0), LocalDateTime.of(2026, 5, 10, 12, 0))
                .overlaps(new TimeSlot(LocalDateTime.of(2026, 5, 10, 9, 0), LocalDateTime.of(2026, 5, 10, 10, 0))))
                .isFalse();
        assertThatThrownBy(() -> new TimeSlot(LocalDateTime.now(), null).validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start time and end time are required.");
        assertThat(new TimeSlot(LocalDateTime.of(2026, 5, 10, 11, 0), LocalDateTime.of(2026, 5, 10, 12, 0))
                .overlaps(new TimeSlot(LocalDateTime.of(2026, 5, 10, 12, 0), LocalDateTime.of(2026, 5, 10, 13, 0))))
                .isFalse();
        assertThat(MedicalNoteMapper.toResponseDTOList(List.of(medicalNote))).hasSize(1);
        assertThat(MedicalNoteMapper.toResponseDTO(null)).isNull();
        assertThat(MedicalNoteMapper.toResponseDTO(MedicalNote.builder().build()).getAppointmentId()).isNull();
        assertThat(medicalNoteResponseDTO.getNoteType()).isEqualTo(NoteType.CONSULTATION);
        assertThat(appointmentRequestDTO.getDescription()).isEqualTo("Annual checkup");
        assertThat(medicalNoteRequestDTO.getAppointmentId()).isEqualTo(1L);
        assertThat(appointmentResponseDTO.getStartTime()).isEqualTo(LocalDateTime.of(2026, 5, 10, 9, 0));
        assertThat(new DoctorClientResponse("doctor-2", null, "Stone", true, true).fullName()).isEqualTo("Stone");
        assertThat(new DoctorClientResponse("doctor-3", "Avery", null, true, true).fullName()).isEqualTo("Avery");
        assertThat(new PatientClientResponse(2L, null, "Taylor", null, "ACTIVE").patientIdentifier()).isNull();
        assertThat(new PatientClientResponse(2L, null, "Taylor", null, "ACTIVE").email()).isNull();
        assertThatThrownBy(() -> new TimeSlot(LocalDateTime.now(), LocalDateTime.now().minusHours(1)).validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End time must be after start time.");
        assertThatThrownBy(() -> new TimeSlot(null, LocalDateTime.now()).validate())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start time and end time are required.");
    }

    @Test
    void modelAssemblerBuildsLinks() {
        AppointmentResponseDTO responseDTO = AppointmentResponseDTO.builder()
                .appointmentId(10L)
                .doctorId("doctor-1")
                .build();

        AppointmentModelAssembler assembler = new AppointmentModelAssembler();
        EntityModel<AppointmentResponseDTO> model = assembler.toModel(responseDTO);
        CollectionModel<EntityModel<AppointmentResponseDTO>> collectionModel =
                assembler.toCollectionModel(List.of(responseDTO));

        assertThat(model.getRequiredLink("self").getHref()).contains("/api/v1/appointments/10");
        assertThat(model.getRequiredLink("doctorAppointments").getHref()).contains("/api/v1/appointments/doctor/doctor-1");
        assertThat(collectionModel.getRequiredLink("self").getHref()).contains("/api/v1/appointments");
    }

    @Test
    void globalExceptionHandlerBuildsExpectedResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/appointments/10");

        ApiErrorResponse notFound = handler.handleResourceNotFound(new ResourceNotFoundException("missing"), request).getBody();
        ApiErrorResponse invalid = handler.handleInvalidInput(new InvalidInputException("bad input"), request).getBody();
        ApiErrorResponse doctor = handler.handleDoctorNotEligible(new DoctorNotEligibleException("doctor invalid"), request).getBody();
        ApiErrorResponse conflict = handler.handleAppointmentConflict(new AppointmentConflictException("conflict"), request).getBody();
        ApiErrorResponse downstream = handler.handleDownstreamService(new DownstreamServiceException("gateway"), request).getBody();
        ApiErrorResponse generic = handler.handleGenericException(new RuntimeException("boom"), request).getBody();

        assertThat(notFound.getStatus()).isEqualTo(404);
        assertThat(invalid.getStatus()).isEqualTo(400);
        assertThat(doctor.getStatus()).isEqualTo(422);
        assertThat(conflict.getStatus()).isEqualTo(409);
        assertThat(downstream.getStatus()).isEqualTo(502);
        assertThat(generic.getStatus()).isEqualTo(500);
    }

    @Test
    void globalExceptionHandlerHandlesValidationErrorsAndUnusedExceptionTypes() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/appointments");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "appointmentRequestDTO");
        bindingResult.addError(new FieldError("appointmentRequestDTO", "patientId", "must not be blank"));
        bindingResult.addError(new ObjectError("appointmentRequestDTO", "General validation error"));
        MethodArgumentNotValidException validationException = mock(MethodArgumentNotValidException.class);

        when(validationException.getBindingResult()).thenReturn(bindingResult);

        ApiErrorResponse validation = handler.handleValidationErrors(validationException, request).getBody();
        DuplicateEmailException duplicateEmailException = new DuplicateEmailException("duplicate");

        assertThat(validation.getStatus()).isEqualTo(400);
        assertThat(validation.getMessage()).isEqualTo("Validation failed.");
        assertThat(validation.getDetails()).containsExactly("patientId: must not be blank", "General validation error");
        assertThat(duplicateEmailException).hasMessage("duplicate");
    }

    @Test
    void domainClientsHandleHappyAndAllMappedErrorPaths() {
        PatientServiceClientImpl patientClient = new PatientServiceClientImpl(
                builder(jsonResponse(HttpStatus.OK, """
                        {"id":1,"patientId":{"patientId":"patient-1"},"fullName":"Jordan Miles","contactInfo":{"email":"jordan@example.com","phone":"514-555-0100"},"status":"ACTIVE"}
                        """)),
                "http://patient-service"
        );
        DoctorServiceClientImpl doctorClient = new DoctorServiceClientImpl(
                builder(jsonResponse(HttpStatus.OK, """
                        {"doctorId":"doctor-1","doctorFirstName":"Avery","doctorLastName":"Stone","isActive":true,"isValid":true}
                        """)),
                "http://doctor-service"
        );
        ClinicRoomServiceClientImpl roomClient = new ClinicRoomServiceClientImpl(
                builder(jsonResponse(HttpStatus.OK, """
                        {"id":1,"roomId":"room-1","roomName":"Consultation Room A","roomNumber":"101","roomStatus":"AVAILABLE"}
                        """)),
                "http://clinic-room-service"
        );

        PatientClientResponse patient = patientClient.getPatientByPatientIdentifier("patient-1");
        DoctorClientResponse doctor = doctorClient.getDoctorByDoctorId("doctor-1");
        ClinicRoomClientResponse room = roomClient.getRoomByRoomId("room-1");

        assertThat(patient.patientIdentifier()).isEqualTo("patient-1");
        assertThat(patient.email()).isEqualTo("jordan@example.com");
        assertThat(doctor.fullName()).isEqualTo("Avery Stone");
        assertThat(room.roomStatus()).isEqualTo("AVAILABLE");

        PatientServiceClientImpl failingPatientClient = new PatientServiceClientImpl(
                builder(jsonResponse(HttpStatus.NOT_FOUND, "{\"message\":\"missing\"}")),
                "http://patient-service"
        );

        assertThatThrownBy(() -> failingPatientClient.getPatientByPatientIdentifier("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Patient not found with patientId: missing");

        PatientServiceClientImpl invalidPatientClient = new PatientServiceClientImpl(
                builder(jsonResponse(HttpStatus.BAD_REQUEST, "{\"message\":\"bad\"}")),
                "http://patient-service"
        );

        assertThatThrownBy(() -> invalidPatientClient.getPatientByPatientIdentifier("bad"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Invalid patient identifier: bad");

        PatientServiceClientImpl patientServiceErrorClient = new PatientServiceClientImpl(
                builder(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "{}")),
                "http://patient-service"
        );

        assertThatThrownBy(() -> patientServiceErrorClient.getPatientByPatientIdentifier("patient-9"))
                .isInstanceOf(DownstreamServiceException.class)
                .hasMessage("Patient service returned 500.");

        DoctorServiceClientImpl failingDoctorClient = new DoctorServiceClientImpl(
                builder(jsonResponse(HttpStatus.BAD_REQUEST, "{\"message\":\"bad\"}")),
                "http://doctor-service"
        );

        assertThatThrownBy(() -> failingDoctorClient.getDoctorByDoctorId("bad"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Invalid doctor identifier: bad");

        DoctorServiceClientImpl missingDoctorClient = new DoctorServiceClientImpl(
                builder(jsonResponse(HttpStatus.NOT_FOUND, "{\"message\":\"missing\"}")),
                "http://doctor-service"
        );

        assertThatThrownBy(() -> missingDoctorClient.getDoctorByDoctorId("doctor-9"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Doctor not found with doctorId: doctor-9");

        DoctorServiceClientImpl doctorServiceErrorClient = new DoctorServiceClientImpl(
                builder(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "{}")),
                "http://doctor-service"
        );

        assertThatThrownBy(() -> doctorServiceErrorClient.getDoctorByDoctorId("doctor-8"))
                .isInstanceOf(DownstreamServiceException.class)
                .hasMessage("Doctor service returned 500.");

        ClinicRoomServiceClientImpl failingRoomClient = new ClinicRoomServiceClientImpl(
                builder(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "{}")),
                "http://clinic-room-service"
        );

        assertThatThrownBy(() -> failingRoomClient.getRoomByRoomId("room-9"))
                .isInstanceOf(DownstreamServiceException.class)
                .hasMessage("Clinic room service returned 500.");

        ClinicRoomServiceClientImpl missingRoomClient = new ClinicRoomServiceClientImpl(
                builder(jsonResponse(HttpStatus.NOT_FOUND, "{\"message\":\"missing\"}")),
                "http://clinic-room-service"
        );

        assertThatThrownBy(() -> missingRoomClient.getRoomByRoomId("room-8"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Clinic room not found with roomId: room-8");

        ClinicRoomServiceClientImpl invalidRoomClient = new ClinicRoomServiceClientImpl(
                builder(jsonResponse(HttpStatus.BAD_REQUEST, "{\"message\":\"bad\"}")),
                "http://clinic-room-service"
        );

        assertThatThrownBy(() -> invalidRoomClient.getRoomByRoomId("bad-room"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Invalid clinic room identifier: bad-room");
    }

    private WebClient.Builder builder(ClientResponse response) {
        ExchangeFunction exchangeFunction = request -> Mono.just(response);
        return WebClient.builder().exchangeFunction(exchangeFunction);
    }

    private ClientResponse jsonResponse(HttpStatus status, String payload) {
        return ClientResponse.create(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .build();
    }
}
