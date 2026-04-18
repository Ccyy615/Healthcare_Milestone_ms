package com.champ.healthcare.Appointment.PresentationLayer;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AppointmentModelAssembler {

    public EntityModel<AppointmentResponseDTO> toModel(AppointmentResponseDTO appointment) {
        return EntityModel.of(
                appointment,
                linkTo(methodOn(AppointmentController.class).getAppointmentById(appointment.getAppointmentId())).withSelfRel(),
                linkTo(methodOn(AppointmentController.class).getAllAppointments()).withRel("appointments"),
                linkTo(methodOn(AppointmentController.class).getAppointmentsByDoctorId(appointment.getDoctorId())).withRel("doctorAppointments"),
                linkTo(methodOn(AppointmentController.class).updateAppointment(appointment.getAppointmentId(), null)).withRel("update"),
                linkTo(methodOn(AppointmentController.class).deleteAppointment(appointment.getAppointmentId())).withRel("delete"),
                linkTo(methodOn(AppointmentController.class).completeAppointment(appointment.getAppointmentId())).withRel("complete"),
                linkTo(methodOn(AppointmentController.class).cancelAppointment(appointment.getAppointmentId())).withRel("cancel")
        );
    }

    public CollectionModel<EntityModel<AppointmentResponseDTO>> toCollectionModel(List<AppointmentResponseDTO> appointments) {
        List<EntityModel<AppointmentResponseDTO>> appointmentModels = appointments.stream()
                .map(this::toModel)
                .toList();

        return CollectionModel.of(
                appointmentModels,
                linkTo(methodOn(AppointmentController.class).getAllAppointments()).withSelfRel()
        );
    }
}