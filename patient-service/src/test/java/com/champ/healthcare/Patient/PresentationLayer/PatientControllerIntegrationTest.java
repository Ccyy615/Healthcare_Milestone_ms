package com.champ.healthcare.Patient.PresentationLayer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
class PatientControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void getAllPatientsReturnsSeededPatients() {
        webTestClient.get()
                .uri("/api/v1/patients")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].fullName").isEqualTo("John Smith");
    }

    @Test
    void getPatientByIdReturnsNotFoundForUnknownPatient() {
        webTestClient.get()
                .uri("/api/v1/patients/{id}", 9999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Patient not found with id: 9999");
    }

    @Test
    void getPatientByPatientIdentifierReturnsPatient() {
        webTestClient.get()
                .uri("/api/v1/patients/patient-identifier/{patientId}", "c1a2b3c4-d5e6-f7a8-b9c0-d1e2f3a4b5c6")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.fullName").isEqualTo("John Smith");
    }

    @Test
    void createPatientReturnsCreatedPatient() {
        webTestClient.post()
                .uri("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "fullName": "Jordan Miles",
                          "dateOfBirth": "1994-02-10",
                          "gender": "F",
                          "contactInfo": {
                            "email": "jordan.miles@example.com",
                            "phone": "514-555-0303"
                          },
                          "address": {
                            "street": "55 King",
                            "city": "Toronto",
                            "province": "Ontario",
                            "postal_code": "M5H1J9",
                            "country": "Canada"
                          },
                          "insuranceNumber": "INS-777",
                          "allergy": {
                            "substance": "None",
                            "reaction": "None"
                          },
                          "bloodType": "AB",
                          "status": "ACTIVE"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.fullName").isEqualTo("Jordan Miles")
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    void createPatientReturnsConflictForDuplicateEmail() {
        webTestClient.post()
                .uri("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "fullName": "Duplicate Email",
                          "dateOfBirth": "1994-02-10",
                          "gender": "F",
                          "contactInfo": {
                            "email": "john.smith@example.com",
                            "phone": "514-555-0303"
                          },
                          "address": {
                            "street": "55 King",
                            "city": "Toronto",
                            "province": "Ontario",
                            "postal_code": "M5H1J9",
                            "country": "Canada"
                          },
                          "insuranceNumber": "INS-888",
                          "allergy": {
                            "substance": "None",
                            "reaction": "None"
                          },
                          "bloodType": "AB",
                          "status": "ACTIVE"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Email already exists: john.smith@example.com");
    }

    @Test
    void patchPatientStatusReturnsUpdatedPatient() {
        webTestClient.patch()
                .uri("/api/v1/patients/{id}/status", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "status": "INACTIVE"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.status").isEqualTo("INACTIVE");
    }
}
