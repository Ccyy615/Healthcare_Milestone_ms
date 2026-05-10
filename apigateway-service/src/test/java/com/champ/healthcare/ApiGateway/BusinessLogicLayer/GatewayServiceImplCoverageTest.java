package com.champ.healthcare.ApiGateway.BusinessLogicLayer;

import com.champ.healthcare.ApiGateway.Appointment.BusinessLogicLayer.AppointmentServiceImpl;
import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentRequestDTO;
import com.champ.healthcare.ApiGateway.Appointment.PresentationLayer.AppointmentResponseDTO;
import com.champ.healthcare.ApiGateway.Clinic.BusinessLogicLayer.ClinicRoomServiceImpl;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomResponseDTO;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomStatusPatchDTO;
import com.champ.healthcare.ApiGateway.Doctor.BusinessLogicLayer.DoctorService;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.DoctorRequestDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.DoctorResponseDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.LicenseRequestDTO;
import com.champ.healthcare.ApiGateway.Doctor.PresentationLayer.SpecialityRequestDTO;
import com.champ.healthcare.ApiGateway.Patient.BusinessLogicLayer.PatientServiceImpl;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientRequestDTO;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientResponseDTO;
import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.PatientStatusPatchDTO;
import com.champ.healthcare.ApiGateway.utilities.InvalidInputException;
import com.champ.healthcare.ApiGateway.utilities.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import reactor.core.publisher.Mono;

class GatewayServiceImplCoverageTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void patientServiceCoversCrudAndErrorMapping() throws Exception {
        PatientResponseDTO patient = patientResponse();
        PatientRequestDTO request = patientRequest();
        PatientStatusPatchDTO statusPatchDTO = new PatientStatusPatchDTO("ACTIVE");
        PatientServiceImpl patientService = new PatientServiceImpl(
                builder(requestSpec -> {
                    String path = requestSpec.url().getPath();
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/patients")) {
                        return jsonResponse(HttpStatus.OK, List.of(patient));
                    }
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/patients/1")) {
                        return jsonResponse(HttpStatus.OK, patient);
                    }
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/patients/patient-identifier/patient-1")) {
                        return jsonResponse(HttpStatus.OK, patient);
                    }
                    if (requestSpec.method() == HttpMethod.POST && path.endsWith("/api/v1/patients")) {
                        return jsonResponse(HttpStatus.OK, patient);
                    }
                    if (requestSpec.method() == HttpMethod.PUT && path.endsWith("/api/v1/patients/1")) {
                        return jsonResponse(HttpStatus.OK, patient);
                    }
                    if (requestSpec.method() == HttpMethod.PATCH && path.endsWith("/api/v1/patients/1/status")) {
                        return jsonResponse(HttpStatus.OK, patient);
                    }
                    if (requestSpec.method() == HttpMethod.DELETE && path.endsWith("/api/v1/patients/1")) {
                        return jsonResponse(HttpStatus.OK, patient);
                    }
                    return jsonResponse(HttpStatus.NOT_FOUND, "{\"message\":\"missing\"}");
                }),
                "http://patient-service"
        );

        assertThat(patientService.getAllPatients()).hasSize(1);
        assertThat(patientService.getPatientById(1L).getPatientId()).isEqualTo("patient-1");
        assertThat(patientService.getPatientByPatientIdentifier("patient-1").getEmail()).isEqualTo("jordan@example.com");
        assertThat(patientService.createPatient(request).getFullName()).isEqualTo("Jordan Miles");
        assertThat(patientService.updatePatient(1L, request).getPhone()).isEqualTo("514-555-0100");
        assertThat(patientService.updatePatientStatus(1L, statusPatchDTO).getStatus().getStatus()).isEqualTo("ACTIVE");
        assertThat(patientService.deletePatientById(1L).getId()).isEqualTo(1L);

        PatientServiceImpl failingService = new PatientServiceImpl(
                builder(requestSpec -> jsonResponse(HttpStatus.BAD_REQUEST, "{\"message\":\"bad patient request\"}")),
                "http://patient-service"
        );

        assertThatThrownBy(() -> failingService.getPatientById(99L))
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("bad patient request");
    }

    @Test
    void clinicRoomServiceCoversCrudEndpoints() throws Exception {
        ClinicRoomResponseDTO room = clinicRoomResponse();
        ClinicRoomRequestDTO request = clinicRoomRequest();
        ClinicRoomStatusPatchDTO statusPatchDTO = new ClinicRoomStatusPatchDTO("AVAILABLE");
        ClinicRoomServiceImpl clinicRoomService = new ClinicRoomServiceImpl(
                builder(requestSpec -> {
                    String path = requestSpec.url().getPath();
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/clinic-rooms")) {
                        return jsonResponse(HttpStatus.OK, List.of(room));
                    }
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/clinic-rooms/1")) {
                        return jsonResponse(HttpStatus.OK, room);
                    }
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/clinic-rooms/room-identifier/room-1")) {
                        return jsonResponse(HttpStatus.OK, room);
                    }
                    if (requestSpec.method() == HttpMethod.POST && path.endsWith("/api/v1/clinic-rooms")) {
                        return jsonResponse(HttpStatus.OK, room);
                    }
                    if (requestSpec.method() == HttpMethod.PUT && path.endsWith("/api/v1/clinic-rooms/1")) {
                        return jsonResponse(HttpStatus.OK, room);
                    }
                    if (requestSpec.method() == HttpMethod.PATCH && path.endsWith("/api/v1/clinic-rooms/1/status")) {
                        return jsonResponse(HttpStatus.OK, room);
                    }
                    if (requestSpec.method() == HttpMethod.DELETE && path.endsWith("/api/v1/clinic-rooms/1")) {
                        return jsonResponse(HttpStatus.OK, room);
                    }
                    return jsonResponse(HttpStatus.NOT_FOUND, "{\"message\":\"missing\"}");
                }),
                "http://clinic-room-service"
        );

        assertThat(clinicRoomService.getAllRooms()).hasSize(1);
        assertThat(clinicRoomService.getRoomById(1L).getRoomName()).isEqualTo("Consultation Room A");
        assertThat(clinicRoomService.getRoomByRoomId("room-1").getRoomNumber()).isEqualTo("101");
        assertThat(clinicRoomService.createRoom(request).getRoomId()).isEqualTo("room-1");
        assertThat(clinicRoomService.updateRoom(1L, request).getId()).isEqualTo(1L);
        assertThat(clinicRoomService.updateRoomStatus(1L, statusPatchDTO).getRoomStatus().getRoomStatus())
                .isEqualTo("AVAILABLE");
        assertThat(clinicRoomService.deleteRoom(1L).getRoomName()).isEqualTo("Consultation Room A");
    }

    @Test
    void clinicRoomServiceMapsDownstreamErrors() {
        ClinicRoomServiceImpl failingService = new ClinicRoomServiceImpl(
                builder(requestSpec -> jsonResponse(HttpStatus.BAD_REQUEST, "{\"message\":\"bad room request\"}")),
                "http://clinic-room-service"
        );

        assertThatThrownBy(() -> failingService.getAllRooms())
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("bad room request");
    }

    @Test
    void appointmentServiceCoversCrudAndStateTransitions() throws Exception {
        AppointmentResponseDTO appointment = appointmentResponse();
        AppointmentRequestDTO request = appointmentRequest();
        AppointmentServiceImpl appointmentService = new AppointmentServiceImpl(
                builder(requestSpec -> {
                    String path = requestSpec.url().getPath();
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/appointments")) {
                        return jsonResponse(HttpStatus.OK, List.of(appointment));
                    }
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/appointments/10")) {
                        return jsonResponse(HttpStatus.OK, appointment);
                    }
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/appointments/doctor/doctor-1")) {
                        return jsonResponse(HttpStatus.OK, List.of(appointment));
                    }
                    if (requestSpec.method() == HttpMethod.POST && path.endsWith("/api/v1/appointments")) {
                        return jsonResponse(HttpStatus.OK, appointment);
                    }
                    if (requestSpec.method() == HttpMethod.PUT && path.endsWith("/api/v1/appointments/10")) {
                        return jsonResponse(HttpStatus.OK, appointment);
                    }
                    if (requestSpec.method() == HttpMethod.DELETE && path.endsWith("/api/v1/appointments/10")) {
                        return jsonResponse(HttpStatus.OK, appointment);
                    }
                    if (requestSpec.method() == HttpMethod.PATCH && path.endsWith("/api/v1/appointments/10/complete")) {
                        return jsonResponse(HttpStatus.OK, appointment);
                    }
                    if (requestSpec.method() == HttpMethod.PATCH && path.endsWith("/api/v1/appointments/10/cancel")) {
                        return jsonResponse(HttpStatus.OK, appointment);
                    }
                    return jsonResponse(HttpStatus.NOT_FOUND, "{\"message\":\"missing\"}");
                }),
                "http://appointment-service"
        );

        assertThat(appointmentService.getAllAppointments()).hasSize(1);
        assertThat(appointmentService.getAppointmentById(10L).getAppointmentId()).isEqualTo(10L);
        assertThat(appointmentService.getAppointmentsByDoctorId("doctor-1")).hasSize(1);
        assertThat(appointmentService.createAppointment(request).getPatientFullName()).isEqualTo("Jordan Miles");
        assertThat(appointmentService.updateAppointment(10L, request).getDoctorFullName()).isEqualTo("Avery Stone");
        assertThat(appointmentService.deleteAppointment(10L).getRoomId()).isEqualTo("room-1");
        assertThat(appointmentService.completeAppointment(10L).getStatus()).isEqualTo("CONFIRMED");
        assertThat(appointmentService.cancelAppointment(10L).getRoomStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void appointmentServiceMapsDownstreamErrors() {
        AppointmentServiceImpl failingService = new AppointmentServiceImpl(
                builder(requestSpec -> jsonResponse(HttpStatus.NOT_FOUND, "{\"message\":\"appointment missing\"}")),
                "http://appointment-service"
        );

        assertThatThrownBy(() -> failingService.getAppointmentById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("appointment missing");
    }

    @Test
    void doctorServiceCoversPublicOperations() throws Exception {
        DoctorResponseDTO doctor = doctorResponse();
        DoctorRequestDTO request = doctorRequest();
        SpecialityRequestDTO specialityRequestDTO = new SpecialityRequestDTO("Cardiology", "Advanced");
        LicenseRequestDTO licenseRequestDTO = new LicenseRequestDTO();
        licenseRequestDTO.setLicenseName("LIC-1");
        licenseRequestDTO.setStatus("ACTIVE");
        licenseRequestDTO.setPerformedDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        DoctorService doctorService = new DoctorService(
                builder(requestSpec -> {
                    String path = requestSpec.url().getPath();
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/doctors")) {
                        return jsonResponse(HttpStatus.OK, List.of(doctor));
                    }
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/doctors/doctor-1")) {
                        return jsonResponse(HttpStatus.OK, doctor);
                    }
                    if (requestSpec.method() == HttpMethod.PUT && path.endsWith("/api/v1/doctors/doctor-1")) {
                        return jsonResponse(HttpStatus.OK, doctor);
                    }
                    if (requestSpec.method() == HttpMethod.POST && path.endsWith("/api/v1/doctors")) {
                        return jsonResponse(HttpStatus.OK, doctor);
                    }
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/doctors/active")) {
                        return jsonResponse(HttpStatus.OK, List.of(doctor));
                    }
                    if (requestSpec.method() == HttpMethod.GET && path.endsWith("/api/v1/doctors/active/speciality/Cardiology")) {
                        return jsonResponse(HttpStatus.OK, List.of(doctor));
                    }
                    if (requestSpec.method() == HttpMethod.POST && path.endsWith("/api/v1/doctors/doctor-1/activate")) {
                        return jsonResponse(HttpStatus.OK, doctor);
                    }
                    if (requestSpec.method() == HttpMethod.POST && path.endsWith("/api/v1/doctors/doctor-1/deactivate")) {
                        return jsonResponse(HttpStatus.OK, doctor);
                    }
                    if (requestSpec.method() == HttpMethod.PATCH && path.endsWith("/api/v1/doctors/doctor-1/activation")) {
                        return jsonResponse(HttpStatus.OK, doctor);
                    }
                    if (requestSpec.method() == HttpMethod.POST && path.endsWith("/api/v1/doctors/doctor-1/speciality")) {
                        return jsonResponse(HttpStatus.OK, doctor);
                    }
                    if (requestSpec.method() == HttpMethod.DELETE && path.endsWith("/api/v1/doctors/doctor-1/speciality/Cardiology")) {
                        return jsonResponse(HttpStatus.OK, doctor);
                    }
                    if (requestSpec.method() == HttpMethod.POST && path.endsWith("/api/v1/doctors/doctor-1/license")) {
                        return jsonResponse(HttpStatus.OK, doctor);
                    }
                    if (requestSpec.method() == HttpMethod.DELETE && path.endsWith("/api/v1/doctors/doctor-1")) {
                        return jsonResponse(HttpStatus.OK, null);
                    }
                    return jsonResponse(HttpStatus.NOT_FOUND, "{\"message\":\"missing\"}");
                }),
                "http://doctor-service"
        );

        assertThat(doctorService.getAllDoctors()).hasSize(1);
        assertThat(doctorService.getDoctorById("doctor-1").getDoctorFirstName()).isEqualTo("Avery");
        assertThat(doctorService.createDoctor(request).getDoctorId()).isEqualTo("doctor-1");
        assertThat(doctorService.updateDoctor("doctor-1", request).getDoctorLastName()).isEqualTo("Stone");
        assertThat(doctorService.getActiveDoctors()).hasSize(1);
        assertThat(doctorService.getActiveDoctorBySpeciality("Cardiology")).hasSize(1);
        assertThat(doctorService.activateDoctor("doctor-1").getIsActive()).isTrue();
        assertThat(doctorService.deactivateDoctor("doctor-1").getIsValid()).isTrue();
        assertThat(doctorService.updateDoctorActivation("doctor-1", true).getDoctorId()).isEqualTo("doctor-1");
        assertThat(doctorService.addSpeciality("doctor-1", specialityRequestDTO).getSpeciality().getSpeciality())
                .isEqualTo("Cardiology");
        assertThat(doctorService.removeSpeciality("doctor-1", "Cardiology").getDoctorId()).isEqualTo("doctor-1");
        assertThat(doctorService.addLicense("doctor-1", licenseRequestDTO).getLicenseName()).isEqualTo("College");
        doctorService.deleteDoctor("doctor-1");
    }

    @Test
    void doctorServiceMapsDownstreamErrors() {
        DoctorService failingService = new DoctorService(
                builder(requestSpec -> jsonResponse(HttpStatus.BAD_REQUEST, "{\"message\":\"bad doctor request\"}")),
                "http://doctor-service"
        );

        assertThatThrownBy(() -> failingService.getAllDoctors())
                .isInstanceOf(InvalidInputException.class)
                .hasMessageContaining("bad doctor request");
    }

    private WebClient.Builder builder(Function<ClientRequest, ClientResponse> responder) {
        ExchangeFunction exchangeFunction = request -> Mono.just(responder.apply(request));
        return WebClient.builder().exchangeFunction(exchangeFunction);
    }

    private ClientResponse jsonResponse(HttpStatus status, Object body) {
        try {
            String payload = body == null ? "" : OBJECT_MAPPER.writeValueAsString(body);
            ClientResponse.Builder builder = ClientResponse.create(status)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            if (!payload.isEmpty()) {
                builder.body(payload);
            }
            return builder.build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
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

    private ClinicRoomRequestDTO clinicRoomRequest() {
        return ClinicRoomRequestDTO.builder()
                .roomName("Consultation Room A")
                .roomNumber("101")
                .roomStatus(new ClinicRoomStatusPatchDTO("AVAILABLE"))
                .build();
    }

    private ClinicRoomResponseDTO clinicRoomResponse() {
        return ClinicRoomResponseDTO.builder()
                .id(1L)
                .roomId("room-1")
                .roomName("Consultation Room A")
                .roomNumber("101")
                .roomStatus(new ClinicRoomStatusPatchDTO("AVAILABLE"))
                .build();
    }

    private AppointmentRequestDTO appointmentRequest() {
        return AppointmentRequestDTO.builder()
                .patientId("patient-1")
                .doctorId("doctor-1")
                .roomId("room-1")
                .startTime(LocalDateTime.of(2026, 5, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 5, 10, 10, 0))
                .description("Annual checkup")
                .build();
    }

    private AppointmentResponseDTO appointmentResponse() {
        return AppointmentResponseDTO.builder()
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
