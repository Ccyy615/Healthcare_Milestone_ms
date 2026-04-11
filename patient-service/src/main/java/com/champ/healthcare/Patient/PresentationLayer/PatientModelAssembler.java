package com.champ.healthcare.Patient.PresentationLayer;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PatientModelAssembler {

    public EntityModel<PatientResponseDTO> toModel(PatientResponseDTO patient) {
        return EntityModel.of(
                patient,
                linkTo(methodOn(PatientController.class).getPatientById(patient.getId())).withSelfRel(),
                linkTo(methodOn(PatientController.class).getAllPatients()).withRel("patients"),
                linkTo(methodOn(PatientController.class).updatePatient(patient.getId(), null)).withRel("update"),
                linkTo(methodOn(PatientController.class).deletePatientById(patient.getId())).withRel("delete")
        );
    }

    public CollectionModel<EntityModel<PatientResponseDTO>> toCollectionModel(List<PatientResponseDTO> patients) {
        List<EntityModel<PatientResponseDTO>> patientModels = patients.stream()
                .map(this::toModel)
                .toList();

        return CollectionModel.of(
                patientModels,
                linkTo(methodOn(PatientController.class).getAllPatients()).withSelfRel()
        );
    }
}