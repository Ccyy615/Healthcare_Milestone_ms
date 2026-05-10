package com.champ.healthcare.Appointment.PresentationLayer;

import com.champ.healthcare.Appointment.DomainClientLayer.ClinicRoomClientResponse;
import com.champ.healthcare.Appointment.DomainClientLayer.ClinicRoomServiceClient;
import com.champ.healthcare.Appointment.DomainClientLayer.DoctorClientResponse;
import com.champ.healthcare.Appointment.DomainClientLayer.DoctorServiceClient;
import com.champ.healthcare.Appointment.DomainClientLayer.PatientClientResponse;
import com.champ.healthcare.Appointment.DomainClientLayer.PatientServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "springdoc.api-docs.enabled=false",
                "springdoc.swagger-ui.enabled=false"
        }
)
@ActiveProfiles("testing")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AppointmentControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private PatientServiceClient patientServiceClient;

    @MockitoBean
    private DoctorServiceClient doctorServiceClient;

    @MockitoBean
    private ClinicRoomServiceClient clinicRoomServiceClient;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void createAppointmentReturnsCreatedAppointmentWhenAggregateInvariantPasses() {
        mockAggregateDependencies("AVAILABLE");

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
                .expectBody()
                .jsonPath("$.appointmentId").isNotEmpty()
                .jsonPath("$.patientFullName").isEqualTo("Jordan Miles")
                .jsonPath("$.doctorFullName").isEqualTo("Avery Stone")
                .jsonPath("$.roomStatus").isEqualTo("AVAILABLE");
    }

    @Test
    void createAppointmentReturnsConflictWhenRoomIsUnavailable() {
        mockAggregateDependencies("OUT_OF_SERVICE");

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
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Clinic room room-1 is not available for booking.");
    }

    private void mockAggregateDependencies(String roomStatus) {
        when(patientServiceClient.getPatientByPatientIdentifier("patient-1")).thenReturn(
                new PatientClientResponse(
                        1L,
                        new PatientClientResponse.PatientIdentifierResponse("patient-1"),
                        "Jordan Miles",
                        new PatientClientResponse.ContactInfoResponse("jordan@example.com", "514-555-0100"),
                        "ACTIVE"
                )
        );
        when(doctorServiceClient.getDoctorByDoctorId("doctor-1")).thenReturn(
                new DoctorClientResponse("doctor-1", "Avery", "Stone", true, true)
        );
        when(clinicRoomServiceClient.getRoomByRoomId("room-1")).thenReturn(
                new ClinicRoomClientResponse(1L, "room-1", "Consultation Room A", "101", roomStatus)
        );
    }
}
