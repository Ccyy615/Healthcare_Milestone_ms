package com.champ.healthcare.Doctor.PresentationLayer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
class DoctorControllerIntegrationTest {

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
    void getAllDoctorsReturnsSeededDoctors() {
        webTestClient.get()
                .uri("/api/v1/doctors")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].doctorId").isEqualTo("e1f2a3b4-c5d6-47e8-9f01-23456789abcd");
    }

    @Test
    void getDoctorByIdReturnsNotFoundForUnknownDoctor() {
        webTestClient.get()
                .uri("/api/v1/doctors/{doctorId}", "missing-doctor")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Doctor not found with ID: missing-doctor");
    }

    @Test
    void createDoctorReturnsCreatedDoctor() {
        webTestClient.post()
                .uri("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "doctorFirstName": "Avery",
                          "doctorLastName": "Stone",
                          "workZone": {
                            "city": "Ottawa",
                            "province": "Ontario"
                          },
                          "speciality": [
                            {
                              "speciality": "Pediatrics",
                              "proficiencyLevel": "ADVANCED"
                            }
                          ]
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.doctorFirstName").isEqualTo("Avery")
                .jsonPath("$.workZone.city").isEqualTo("Ottawa")
                .jsonPath("$.doctorId").isNotEmpty();
    }

    @Test
    void activateDoctorReturnsUpdatedDoctorWhenDoctorIsEligible() {
        DoctorResponseDTO createdDoctor = webTestClient.post()
                .uri("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "doctorFirstName": "Taylor",
                          "doctorLastName": "Brooks",
                          "workZone": {
                            "city": "Montreal",
                            "province": "Quebec"
                          },
                          "speciality": [
                            {
                              "speciality": "Cardiology",
                              "proficiencyLevel": "EXPERT"
                            }
                          ]
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DoctorResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdDoctor).isNotNull();

        webTestClient.post()
                .uri("/api/v1/doctors/{doctorId}/license", createdDoctor.getDoctorId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "licenseName": "General Practice License",
                          "status": "VALID"
                        }
                        """)
                .exchange()
                .expectStatus().isOk();

        webTestClient.post()
                .uri("/api/v1/doctors/{doctorId}/activate", createdDoctor.getDoctorId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.doctorId").isEqualTo(createdDoctor.getDoctorId())
                .jsonPath("$.isActive").isEqualTo(true);
    }

    @Test
    void activateDoctorReturnsBadRequestForDoctorNotEligible() {
        DoctorResponseDTO createdDoctor = webTestClient.post()
                .uri("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "doctorFirstName": "Morgan",
                          "doctorLastName": "Lee",
                          "workZone": {
                            "city": "Montreal",
                            "province": "Quebec"
                          },
                          "speciality": []
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DoctorResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdDoctor).isNotNull();

        webTestClient.post()
                .uri("/api/v1/doctors/{doctorId}/activate", createdDoctor.getDoctorId())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").value(message ->
                        assertThat(message.toString()).contains("No verified speciality"));
    }

    @Test
    void patchDoctorActivationReturnsUpdatedDoctor() {
        DoctorResponseDTO createdDoctor = webTestClient.post()
                .uri("/api/v1/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "doctorFirstName": "Jamie",
                          "doctorLastName": "Chen",
                          "workZone": {
                            "city": "Toronto",
                            "province": "Ontario"
                          },
                          "speciality": [
                            {
                              "speciality": "Cardiology",
                              "proficiencyLevel": "EXPERT"
                            }
                          ]
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DoctorResponseDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdDoctor).isNotNull();

        webTestClient.patch()
                .uri("/api/v1/doctors/{doctorId}/activation", createdDoctor.getDoctorId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "active": false
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.doctorId").isEqualTo(createdDoctor.getDoctorId())
                .jsonPath("$.isActive").isEqualTo(false);
    }
}
