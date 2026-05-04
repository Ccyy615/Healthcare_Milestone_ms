package com.champ.healthcare.ApiGateway.Patient.PresentationLayer;

import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PatientModelAssembler {

    public PatientResponseDTO addLinks(PatientResponseDTO patient) {
        if (patient == null) {
            return null;
        }

        patient.removeLinks();
        patient.add(linkTo(methodOn(PatientController.class).getPatientById(patient.getId())).withSelfRel());
        patient.add(linkTo(methodOn(PatientController.class).getAllPatients()).withRel("patients"));
        patient.add(linkTo(methodOn(PatientController.class)
                .getPatientByPatientIdentifier(patient.getPatientId())).withRel("patientIdentifier"));
        patient.add(linkTo(methodOn(PatientController.class)
                .updatePatient(patient.getId(), null)).withRel("update"));
        patient.add(linkTo(methodOn(PatientController.class)
                .updatePatientStatus(patient.getId(), null)).withRel("status"));
        patient.add(linkTo(methodOn(PatientController.class)
                .deletePatientById(patient.getId())).withRel("delete"));
        return patient;
    }

    public List<PatientResponseDTO> addLinks(List<PatientResponseDTO> patients) {
        return patients.stream()
                .map(this::addLinks)
                .toList();
    }
}
