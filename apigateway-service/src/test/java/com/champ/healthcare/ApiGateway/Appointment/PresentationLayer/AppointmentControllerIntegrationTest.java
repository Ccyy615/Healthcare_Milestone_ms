package com.champ.healthcare.ApiGateway.Appointment.PresentationLayer;

import com.champ.healthcare.ApiGateway.Appointment.BusinessLogicLayer.AppointmentService;
import com.champ.healthcare.ApiGateway.Clinic.BusinessLogicLayer.ClinicRoomService;
import com.champ.healthcare.ApiGateway.Doctor.BusinessLogicLayer.DoctorService;
import com.champ.healthcare.ApiGateway.Patient.BusinessLogicLayer.PatientService;
import com.champ.healthcare.ApiGateway.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppointmentControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private ClinicRoomService clinicRoomService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void getAllAppointmentsReturnsOkResponse() {
        when(appointmentService.getAllAppointments()).thenReturn(List.of(appointmentResponse()));

        webTestClient.get()
                .uri("/api/v1/appointments")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].appointmentId").isEqualTo(10)
                .jsonPath("$[0].doctorFullName").isEqualTo("Avery Stone");
    }

    @Test
    void createAppointmentReturnsCreatedResponse() {
        when(appointmentService.createAppointment(org.mockito.ArgumentMatchers.any(AppointmentRequestDTO.class)))
                .thenReturn(appointmentResponse());

        webTestClient.post()
                .uri("/api/v1/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "patientId": "patient-1",
                          "doctorId": "doctor-1",
                          "roomId": "room-1",
                          "startTime": "2026-05-10T09:00:00",
                          "endTime": "2026-05-10T10:00:00",
                          "description": "Annual checkup"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/api/v1/appointments/10")
                .expectBody()
                .jsonPath("$.patientFullName").isEqualTo("Jordan Miles")
                .jsonPath("$._links.complete.href").isEqualTo("http://localhost:" + port + "/api/v1/appointments/10/complete");
    }

    @Test
    void getAppointmentByIdReturnsNotFoundWhenGatewayServiceRaisesResourceNotFound() {
        when(appointmentService.getAppointmentById(999L))
                .thenThrow(new ResourceNotFoundException("Appointment not found with ID: 999"));

        webTestClient.get()
                .uri("/api/v1/appointments/999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Appointment not found with ID: 999");
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
                .createdAt(LocalDateTime.of(2026, 5, 2, 12, 0))
                .startTime(LocalDateTime.of(2026, 5, 10, 9, 0))
                .endTime(LocalDateTime.of(2026, 5, 10, 10, 0))
                .description("Annual checkup")
                .build();
    }
}
