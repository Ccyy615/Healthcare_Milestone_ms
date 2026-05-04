package com.champ.healthcare.ApiGateway.Appointment.PresentationLayer;

import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AppointmentModelAssembler {

    public AppointmentResponseDTO addLinks(AppointmentResponseDTO appointment) {
        if (appointment == null) {
            return null;
        }

        appointment.removeLinks();
        appointment.add(linkTo(methodOn(AppointmentController.class)
                .getAppointmentById(appointment.getAppointmentId())).withSelfRel());
        appointment.add(linkTo(methodOn(AppointmentController.class)
                .getAllAppointments()).withRel("appointments"));
        appointment.add(linkTo(methodOn(AppointmentController.class)
                .getAppointmentsByDoctorId(appointment.getDoctorId())).withRel("doctorAppointments"));
        appointment.add(linkTo(methodOn(AppointmentController.class)
                .updateAppointment(appointment.getAppointmentId(), null)).withRel("update"));
        appointment.add(linkTo(methodOn(AppointmentController.class)
                .deleteAppointment(appointment.getAppointmentId())).withRel("delete"));
        appointment.add(linkTo(methodOn(AppointmentController.class)
                .completeAppointment(appointment.getAppointmentId())).withRel("complete"));
        appointment.add(linkTo(methodOn(AppointmentController.class)
                .cancelAppointment(appointment.getAppointmentId())).withRel("cancel"));
        return appointment;
    }

    public List<AppointmentResponseDTO> addLinks(List<AppointmentResponseDTO> appointments) {
        return appointments.stream()
                .map(this::addLinks)
                .toList();
    }
}
