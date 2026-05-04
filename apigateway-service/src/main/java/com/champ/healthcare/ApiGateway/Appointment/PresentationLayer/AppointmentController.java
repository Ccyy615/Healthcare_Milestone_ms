package com.champ.healthcare.ApiGateway.Appointment.PresentationLayer;

import com.champ.healthcare.ApiGateway.Appointment.BusinessLogicLayer.AppointmentService;
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
    private final AppointmentModelAssembler appointmentModelAssembler;

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentModelAssembler.addLinks(appointmentService.getAllAppointments()));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentModelAssembler.addLinks(
                appointmentService.getAppointmentById(appointmentId)
        ));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByDoctorId(@PathVariable String doctorId) {
        return ResponseEntity.ok(appointmentModelAssembler.addLinks(
                appointmentService.getAppointmentsByDoctorId(doctorId)
        ));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@RequestBody AppointmentRequestDTO appointmentRequestDTO) {
        AppointmentResponseDTO createdAppointment = appointmentModelAssembler.addLinks(
                appointmentService.createAppointment(appointmentRequestDTO)
        );
        return ResponseEntity.created(URI.create("/api/v1/appointments/" + createdAppointment.getAppointmentId()))
                .body(createdAppointment);
    }

    @PutMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(
            @PathVariable Long appointmentId,
            @RequestBody AppointmentRequestDTO appointmentRequestDTO
    ) {
        return ResponseEntity.ok(appointmentModelAssembler.addLinks(
                appointmentService.updateAppointment(appointmentId, appointmentRequestDTO)
        ));
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponseDTO> deleteAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentModelAssembler.addLinks(
                appointmentService.deleteAppointment(appointmentId)
        ));
    }

    @PatchMapping("/{appointmentId}/complete")
    public ResponseEntity<AppointmentResponseDTO> completeAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentModelAssembler.addLinks(
                appointmentService.completeAppointment(appointmentId)
        ));
    }

    @PatchMapping("/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentModelAssembler.addLinks(
                appointmentService.cancelAppointment(appointmentId)
        ));
    }
}
