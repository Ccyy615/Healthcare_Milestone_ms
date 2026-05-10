package com.champ.healthcare.ApiGateway.Doctor.PresentationLayer;

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

import java.time.LocalDateTime;
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
class DoctorControllerIntegrationTest {

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
    void getAllDoctorsReturnsDoctorLinks() {
        when(doctorService.getAllDoctors()).thenReturn(List.of(doctorResponse()));

        webTestClient.get()
                .uri("/api/v1/doctors")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].doctorId").isEqualTo("doctor-1");
    }

    @Test
    void createDoctorReturnsCreatedResponse() {
        when(doctorService.createDoctor(any(DoctorRequestDTO.class))).thenReturn(doctorResponse());

        webTestClient.post()
                .uri("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "doctorFirstName": "Avery",
                          "doctorLastName": "Stone",
                          "city": "Montreal",
                          "province": "QC",
                          "speciality": { "speciality": "Cardiology", "description": "Advanced" }
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/api/v1/doctors/doctor-1")
                .expectBody()
                .jsonPath("$._links.activate.href").isEqualTo("http://localhost:" + port + "/api/v1/doctors/doctor-1/activate");
    }

    @Test
    void getDoctorByIdReturnsNotFoundWhenServiceThrows() {
        when(doctorService.getDoctorById("doctor-404")).thenThrow(new ResourceNotFoundException("Doctor not found"));

        webTestClient.get()
                .uri("/api/v1/doctors/doctor-404")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Doctor not found");
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
