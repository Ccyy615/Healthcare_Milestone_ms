package com.champ.healthcare.ApiGateway.Patient.PresentationLayer;

import com.champ.healthcare.ApiGateway.Patient.BusinessLogicLayer.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@RestController
@Tag(name = "Patient Controller", description = "API for patient")
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final PatientModelAssembler patientModelAssembler;

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        return ResponseEntity.ok(patientModelAssembler.addLinks(patientService.getAllPatients()));
    }
    @Operation(summary = "Retrieves all patients by Id", description = "gives a Long id to patient from service")
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientModelAssembler.addLinks(patientService.getPatientById(id)));
    }

    @GetMapping("/patient-identifier/{patientId}")
    public ResponseEntity<PatientResponseDTO> getPatientByPatientIdentifier(@PathVariable String patientId) {
        return ResponseEntity.ok(patientModelAssembler.addLinks(
                patientService.getPatientByPatientIdentifier(patientId)
        ));
    }

    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(
            @Valid @RequestBody PatientRequestDTO patientRequestDTO
    ) {
        PatientResponseDTO createdPatient = patientModelAssembler.addLinks(
                patientService.createPatient(patientRequestDTO)
        );
        URI location = URI.create("/api/v1/patients/" + createdPatient.getId());
        return ResponseEntity.created(location).body(createdPatient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDTO patientRequestDTO
    ) {
        return ResponseEntity.ok(patientModelAssembler.addLinks(
                patientService.updatePatient(id, patientRequestDTO)
        ));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PatientResponseDTO> updatePatientStatus(
            @PathVariable Long id,
            @RequestBody PatientStatusPatchDTO patchDTO
    ) {
        return ResponseEntity.ok(patientModelAssembler.addLinks(
                patientService.updatePatientStatus(id, patchDTO)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> deletePatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientModelAssembler.addLinks(patientService.deletePatientById(id)));
    }
}
