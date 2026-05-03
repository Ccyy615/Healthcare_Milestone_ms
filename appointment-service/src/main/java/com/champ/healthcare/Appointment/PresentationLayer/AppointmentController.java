package com.champ.healthcare.Appointment.PresentationLayer;

import com.champ.healthcare.Appointment.BusinessLogicLayer.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments() {
        List<AppointmentResponseDTO> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{appointment_id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
            @PathVariable("appointment_id") Long appointmentId
    ) {
        AppointmentResponseDTO appointment = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByDoctorId(
            @PathVariable String doctorId
    ) {
        List<AppointmentResponseDTO> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        return ResponseEntity.ok(appointments);
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(
            @RequestBody AppointmentRequestDTO dto
    ) {
        AppointmentResponseDTO createdAppointment = appointmentService.createAppointment(dto);
        URI location = URI.create("/api/v1/appointments/" + createdAppointment.getAppointmentId());

        return ResponseEntity.created(location).body(createdAppointment);
    }

    @PutMapping("/{appointment_id}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(
            @PathVariable("appointment_id") Long appointmentId,
            @RequestBody AppointmentRequestDTO dto
    ) {
        AppointmentResponseDTO updatedAppointment = appointmentService.updateAppointment(appointmentId, dto);
        return ResponseEntity.ok(updatedAppointment);
    }

    @DeleteMapping("/{appointment_id}")
    public ResponseEntity<AppointmentResponseDTO> deleteAppointment(
            @PathVariable("appointment_id") Long appointmentId
    ) {
        AppointmentResponseDTO deletedAppointment = appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.ok(deletedAppointment);
    }

    @PatchMapping("/{appointment_id}/complete")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(
            @PathVariable("appointment_id") Long appointmentId
    ) {
        AppointmentResponseDTO appointment = appointmentService.completeAppointment(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    @PatchMapping("/{appointment_id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable("appointment_id") Long appointmentId
    ) {
        AppointmentResponseDTO appointment = appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.ok(appointment);
    }
}
