package com.champ.healthcare.ApiGateway.Patient.PresentationLayer;

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

import java.time.LocalDate;
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
class PatientControllerIntegrationTest {

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
    void getAllPatientsReturnsPatientLinks() {
        when(patientService.getAllPatients()).thenReturn(List.of(patientResponse()));

        webTestClient.get()
                .uri("/api/v1/patients")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].patientId").isEqualTo("patient-1");
    }

    @Test
    void createPatientReturnsCreatedResponse() {
        when(patientService.createPatient(any(PatientRequestDTO.class))).thenReturn(patientResponse());

        webTestClient.post()
                .uri("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "fullName": "Jordan Miles",
                          "dateOfBirth": "1990-05-01",
                          "gender": "F",
                          "email": "jordan@example.com",
                          "phone": "514-555-0100",
                          "street": "1 Main",
                          "city": "Montreal",
                          "province": "QC",
                          "postal_code": "H1H1H1",
                          "country": "Canada",
                          "insuranceNumber": "INS-1",
                          "substance": "Pollen",
                          "reaction": "Sneezing",
                          "bloodType": "O+",
                          "status": { "status": "ACTIVE" }
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/api/v1/patients/1")
                .expectBody()
                .jsonPath("$._links.status.href").isEqualTo("http://localhost:" + port + "/api/v1/patients/1/status");
    }

    @Test
    void getPatientByIdReturnsNotFoundWhenServiceThrows() {
        when(patientService.getPatientById(999L)).thenThrow(new ResourceNotFoundException("Patient not found"));

        webTestClient.get()
                .uri("/api/v1/patients/999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Patient not found");
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
}
