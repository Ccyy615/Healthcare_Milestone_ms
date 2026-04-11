package com.champ.healthcare.Patient.PresentationLayer;

import com.champ.healthcare.Patient.BusinessLogicLayer.PatientService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final PatientModelAssembler patientModelAssembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PatientResponseDTO>>> getAllPatients() {
        List<PatientResponseDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patientModelAssembler.toCollectionModel(patients));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PatientResponseDTO>> getPatientById(@PathVariable Long id) {
        PatientResponseDTO patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patientModelAssembler.toModel(patient));
    }

    @PostMapping
    public ResponseEntity<EntityModel<PatientResponseDTO>> createPatient(
            @Valid @RequestBody PatientRequestDTO patientRequestDTO
    ) {
        PatientResponseDTO createdPatient = patientService.createPatient(patientRequestDTO);

        EntityModel<PatientResponseDTO> model = patientModelAssembler.toModel(createdPatient);
        URI location = linkTo(methodOn(PatientController.class).getPatientById(createdPatient.getId())).toUri();

        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<PatientResponseDTO>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDTO patientRequestDTO
    ) {
        PatientResponseDTO updatedPatient = patientService.updatePatient(id, patientRequestDTO);
        return ResponseEntity.ok(patientModelAssembler.toModel(updatedPatient));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModel<PatientResponseDTO>> deletePatientById(@PathVariable Long id) {
        PatientResponseDTO deletedPatient = patientService.deletePatientById(id);
        return ResponseEntity.ok(patientModelAssembler.toModel(deletedPatient));
    }
}