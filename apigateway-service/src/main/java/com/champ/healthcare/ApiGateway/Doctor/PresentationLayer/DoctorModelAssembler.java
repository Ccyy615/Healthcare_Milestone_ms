package com.champ.healthcare.ApiGateway.Doctor.PresentationLayer;

import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DoctorModelAssembler {

    public DoctorResponseDTO addLinks(DoctorResponseDTO doctor) {
        if (doctor == null) {
            return null;
        }

        doctor.removeLinks();
        doctor.add(linkTo(methodOn(DoctorController.class).getDoctorById(doctor.getDoctorId())).withSelfRel());
        doctor.add(linkTo(methodOn(DoctorController.class).getAllDoctors()).withRel("doctors"));
        doctor.add(linkTo(methodOn(DoctorController.class)
                .updateDoctor(doctor.getDoctorId(), null)).withRel("update"));
        doctor.add(linkTo(methodOn(DoctorController.class)
                .activateDoctor(doctor.getDoctorId())).withRel("activate"));
        doctor.add(linkTo(methodOn(DoctorController.class)
                .deactivateDoctor(doctor.getDoctorId())).withRel("deactivate"));
        doctor.add(linkTo(methodOn(DoctorController.class)
                .deleteDoctor(doctor.getDoctorId())).withRel("delete"));
        return doctor;
    }

    public List<DoctorResponseDTO> addLinks(List<DoctorResponseDTO> doctors) {
        return doctors.stream()
                .map(this::addLinks)
                .toList();
    }
}
