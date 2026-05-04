package com.champ.healthcare.Appointment.PresentationLayer;

import com.champ.healthcare.Appointment.BusinessLogicLayer.AppointmentService;
import com.champ.healthcare.Appointment.utilities.ResourceNotFoundException;
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

    private AppointmentController appointmentController;

    @BeforeEach
    void setUp() {
        appointmentController = new AppointmentController(appointmentService);
    }

    @Test
    void getAllAppointmentsReturnsOkResponse() {
        AppointmentResponseDTO appointment = appointmentResponse();
        when(appointmentService.getAllAppointments()).thenReturn(List.of(appointment));

        ResponseEntity<List<AppointmentResponseDTO>> response = appointmentController.getAllAppointments();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(appointment);
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
    }

    @Test
    void getAppointmentByIdReturnsOkResponse() {
        AppointmentResponseDTO appointment = appointmentResponse();
        when(appointmentService.getAppointmentById(10L)).thenReturn(appointment);

        assertThat(appointmentController.getAppointmentById(10L).getBody().getAppointmentId()).isEqualTo(10L);
    }

    @Test
    void getAppointmentsByDoctorIdReturnsOkResponse() {
        AppointmentResponseDTO appointment = appointmentResponse();
        when(appointmentService.getAppointmentsByDoctorId("doctor-1")).thenReturn(List.of(appointment));

        assertThat(appointmentController.getAppointmentsByDoctorId("doctor-1").getBody()).hasSize(1);
    }

    @Test
    void updateAppointmentReturnsOkResponse() {
        AppointmentRequestDTO request = AppointmentRequestDTO.builder()
                .patientId("patient-1")
                .doctorId("doctor-1")
                .roomId("room-1")
                .build();
        when(appointmentService.updateAppointment(10L, request)).thenReturn(appointmentResponse());

        assertThat(appointmentController.updateAppointment(10L, request).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteAppointmentReturnsOkResponse() {
        when(appointmentService.deleteAppointment(10L)).thenReturn(appointmentResponse());

        assertThat(appointmentController.deleteAppointment(10L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void completeAppointmentReturnsOkResponse() {
        when(appointmentService.completeAppointment(10L)).thenReturn(appointmentResponse());

        assertThat(appointmentController.completeAppointment(10L).getBody().getAppointmentId()).isEqualTo(10L);
    }

    @Test
    void cancelAppointmentReturnsOkResponse() {
        when(appointmentService.cancelAppointment(10L)).thenReturn(appointmentResponse());

        assertThat(appointmentController.cancelAppointment(10L).getBody().getAppointmentId()).isEqualTo(10L);
    }

    @Test
    void getAppointmentByIdPropagatesNegativePath() {
        when(appointmentService.getAppointmentById(404L))
                .thenThrow(new ResourceNotFoundException("Appointment not found with ID: 404"));

        assertThatThrownBy(() -> appointmentController.getAppointmentById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Appointment not found with ID: 404");
    }

    private AppointmentResponseDTO appointmentResponse() {
        return AppointmentResponseDTO.builder()
                .appointmentId(10L)
                .patientId("patient-1")
                .patientFullName("Jordan Miles")
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
