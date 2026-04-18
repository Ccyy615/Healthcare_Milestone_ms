package com.champ.healthcare.Appointment.PresentationLayer;

import com.champ.healthcare.Appointment.BusinessLogicLayer.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentModelAssembler appointmentModelAssembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<AppointmentResponseDTO>>> getAllAppointments() {
        List<AppointmentResponseDTO> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointmentModelAssembler.toCollectionModel(appointments));
    }

    @GetMapping("/{appointment_id}")
    public ResponseEntity<EntityModel<AppointmentResponseDTO>> getAppointmentById(
            @PathVariable("appointment_id") Long appointmentId
    ) {
        AppointmentResponseDTO appointment = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(appointmentModelAssembler.toModel(appointment));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<CollectionModel<EntityModel<AppointmentResponseDTO>>> getAppointmentsByDoctorId(
            @PathVariable String doctorId
    ) {
        List<AppointmentResponseDTO> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        return ResponseEntity.ok(appointmentModelAssembler.toCollectionModel(appointments));
    }

    @PostMapping
    public ResponseEntity<EntityModel<AppointmentResponseDTO>> createAppointment(
            @RequestBody AppointmentRequestDTO dto
    ) {
        AppointmentResponseDTO createdAppointment = appointmentService.createAppointment(dto);
        EntityModel<AppointmentResponseDTO> model = appointmentModelAssembler.toModel(createdAppointment);
        URI location = linkTo(methodOn(AppointmentController.class).getAppointmentById(createdAppointment.getAppointmentId())).toUri();

        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{appointment_id}")
    public ResponseEntity<EntityModel<AppointmentResponseDTO>> updateAppointment(
            @PathVariable("appointment_id") Long appointmentId,
            @RequestBody AppointmentRequestDTO dto
    ) {
        AppointmentResponseDTO updatedAppointment = appointmentService.updateAppointment(appointmentId, dto);
        return ResponseEntity.ok(appointmentModelAssembler.toModel(updatedAppointment));
    }

    @DeleteMapping("/{appointment_id}")
    public ResponseEntity<EntityModel<AppointmentResponseDTO>> deleteAppointment(
            @PathVariable("appointment_id") Long appointmentId
    ) {
        AppointmentResponseDTO deletedAppointment = appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.ok(appointmentModelAssembler.toModel(deletedAppointment));
    }

    @PatchMapping("/{appointment_id}/complete")
    public ResponseEntity<EntityModel<AppointmentResponseDTO>> completeAppointment(
            @PathVariable("appointment_id") Long appointmentId
    ) {
        AppointmentResponseDTO appointment = appointmentService.completeAppointment(appointmentId);
        return ResponseEntity.ok(appointmentModelAssembler.toModel(appointment));
    }

    @PatchMapping("/{appointment_id}/cancel")
    public ResponseEntity<EntityModel<AppointmentResponseDTO>> cancelAppointment(
            @PathVariable("appointment_id") Long appointmentId
    ) {
        AppointmentResponseDTO appointment = appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.ok(appointmentModelAssembler.toModel(appointment));
    }
}