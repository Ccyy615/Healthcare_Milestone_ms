package com.champ.healthcare.ClinicRoom.PresentationLayer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
class ClinicRoomControllerIntegrationTest {

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
    void getAllRoomsReturnsSeededRooms() {
        webTestClient.get()
                .uri("/api/v1/clinic-rooms")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].roomNumber").isEqualTo("101");
    }

    @Test
    void getRoomByIdReturnsNotFoundForUnknownRoom() {
        webTestClient.get()
                .uri("/api/v1/clinic-rooms/{id}", 9999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Clinic room not found with id: 9999");
    }

    @Test
    void createRoomReturnsCreatedRoom() {
        webTestClient.post()
                .uri("/api/v1/clinic-rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "roomName": "Radiology Room",
                          "roomNumber": "301",
                          "roomStatus": "AVAILABLE"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.roomName").isEqualTo("Radiology Room")
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    void createRoomReturnsConflictForDuplicateRoomNumber() {
        webTestClient.post()
                .uri("/api/v1/clinic-rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "roomName": "Duplicate Room",
                          "roomNumber": "101",
                          "roomStatus": "AVAILABLE"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("A clinic room with this room number already exists.");
    }

    @Test
    void patchRoomStatusReturnsUpdatedRoom() {
        webTestClient.patch()
                .uri("/api/v1/clinic-rooms/{id}/status", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "roomStatus": "OUT_OF_SERVICE"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.roomStatus").isEqualTo("OUT_OF_SERVICE");
    }
}
