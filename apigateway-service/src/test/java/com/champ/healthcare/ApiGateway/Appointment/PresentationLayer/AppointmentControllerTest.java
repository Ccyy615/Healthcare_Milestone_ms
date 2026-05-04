package com.champ.healthcare.ApiGateway.Appointment.PresentationLayer;

import com.champ.healthcare.ApiGateway.Appointment.BusinessLogicLayer.AppointmentService;
import com.champ.healthcare.ApiGateway.utilities.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    private AppointmentModelAssembler appointmentModelAssembler;
    private AppointmentController appointmentController;

    @BeforeEach
    void setUp() {
        appointmentModelAssembler = new AppointmentModelAssembler();
        appointmentController = new AppointmentController(appointmentService, appointmentModelAssembler);
    }

    @Test
    void getAllAppointmentsReturnsOkResponse() {
        AppointmentResponseDTO appointment = appointmentResponse();
        when(appointmentService.getAllAppointments()).thenReturn(List.of(appointment));

        ResponseEntity<List<AppointmentResponseDTO>> response = appointmentController.getAllAppointments();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(appointment);
        assertThat(response.getBody().get(0).getRequiredLink("self").getHref()).contains("/api/v1/appointments/10");
    }

    @Test
    void createAppointmentReturnsCreatedResponse() {
        AppointmentRequestDTO request = AppointmentRequestDTO.builder()
                .patientId("patient-1")
                .doctorId("doctor-1")
                .roomId("room-1")
                .build();
        AppointmentResponseDTO appointment = appointmentResponse();
        when(appointmentService.createAppointment(request)).thenReturn(appointment);

        ResponseEntity<AppointmentResponseDTO> response = appointmentController.createAppointment(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/appointments/10");
        assertThat(response.getBody()).isEqualTo(appointment);
        assertThat(response.getBody().getRequiredLink("complete").getHref()).contains("/api/v1/appointments/10/complete");
    }

    @Test
    void getAppointmentByIdPropagatesNegativePath() {
        when(appointmentService.getAppointmentById(404L)).thenThrow(new ResourceNotFoundException("missing appointment"));

        assertThatThrownBy(() -> appointmentController.getAppointmentById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("missing appointment");
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
