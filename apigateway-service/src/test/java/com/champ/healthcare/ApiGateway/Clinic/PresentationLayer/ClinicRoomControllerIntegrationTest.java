package com.champ.healthcare.ApiGateway.Clinic.PresentationLayer;

import com.champ.healthcare.ApiGateway.Appointment.BusinessLogicLayer.AppointmentService;
import com.champ.healthcare.ApiGateway.Clinic.BusinessLogicLayer.ClinicRoomService;
import com.champ.healthcare.ApiGateway.Doctor.BusinessLogicLayer.DoctorService;
import com.champ.healthcare.ApiGateway.Patient.BusinessLogicLayer.PatientService;
import com.champ.healthcare.ApiGateway.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "springdoc.api-docs.enabled=false",
                "springdoc.swagger-ui.enabled=false"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClinicRoomControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private DoctorService doctorService;

    @MockitoBean
    private ClinicRoomService clinicRoomService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void getAllRoomsReturnsRoomLinks() {
        when(clinicRoomService.getAllRooms()).thenReturn(List.of(roomResponse()));

        webTestClient.get()
                .uri("/api/v1/clinic-rooms")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].roomId").isEqualTo("room-1");
    }

    @Test
    void createRoomReturnsCreatedResponse() {
        when(clinicRoomService.createRoom(any(ClinicRoomRequestDTO.class))).thenReturn(roomResponse());

        webTestClient.post()
                .uri("/api/v1/clinic-rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "roomName": "Consultation Room A",
                          "roomNumber": "101",
                          "roomStatus": { "roomStatus": "AVAILABLE" }
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/api/v1/clinic-rooms/1")
                .expectBody()
                .jsonPath("$._links.status.href").isEqualTo("http://localhost:" + port + "/api/v1/clinic-rooms/1/status");
    }

    @Test
    void getRoomByIdReturnsNotFoundWhenServiceThrows() {
        when(clinicRoomService.getRoomById(999L)).thenThrow(new ResourceNotFoundException("Room not found"));

        webTestClient.get()
                .uri("/api/v1/clinic-rooms/999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Room not found");
    }

    private ClinicRoomResponseDTO roomResponse() {
        return ClinicRoomResponseDTO.builder()
                .id(1L)
                .roomId("room-1")
                .roomName("Consultation Room A")
                .roomNumber("101")
                .roomStatus(new ClinicRoomStatusPatchDTO("AVAILABLE"))
                .build();
    }
}
