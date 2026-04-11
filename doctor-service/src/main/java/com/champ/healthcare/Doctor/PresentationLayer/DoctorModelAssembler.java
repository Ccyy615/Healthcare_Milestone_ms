package com.champ.healthcare.Doctor.PresentationLayer;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DoctorModelAssembler {

    public EntityModel<DoctorResponseDTO> toModel(DoctorResponseDTO doctor) {
        return EntityModel.of(
                doctor,
                linkTo(methodOn(DoctorController.class).getDoctorById(doctor.getDoctorId())).withSelfRel(),
                linkTo(methodOn(DoctorController.class).getAllDoctors()).withRel("doctors"),
                linkTo(methodOn(DoctorController.class).updateDoctor(doctor.getDoctorId(), null)).withRel("update"),
                linkTo(methodOn(DoctorController.class).deleteDoctor(doctor.getDoctorId())).withRel("delete"),
                linkTo(methodOn(DoctorController.class).activateDoctor(doctor.getDoctorId())).withRel("activate"),
                linkTo(methodOn(DoctorController.class).deactivateDoctor(doctor.getDoctorId())).withRel("deactivate")
        );
    }

    public CollectionModel<EntityModel<DoctorResponseDTO>> toCollectionModel(List<DoctorResponseDTO> doctors) {
        List<EntityModel<DoctorResponseDTO>> doctorModels = doctors.stream()
                .map(this::toModel)
                .toList();

        return CollectionModel.of(
                doctorModels,
                linkTo(methodOn(DoctorController.class).getAllDoctors()).withSelfRel()
        );
    }
}