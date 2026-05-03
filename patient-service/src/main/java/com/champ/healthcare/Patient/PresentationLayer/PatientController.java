package com.champ.healthcare.Patient.PresentationLayer;

import com.champ.healthcare.Patient.BusinessLogicLayer.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/patient-identifier/{patientId}")
    public ResponseEntity<PatientResponseDTO> getPatientByPatientIdentifier(@PathVariable String patientId) {
        return ResponseEntity.ok(patientService.getPatientByPatientIdentifier(patientId));
    }

    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(
            @Valid @RequestBody PatientRequestDTO patientRequestDTO
    ) {
        PatientResponseDTO createdPatient = patientService.createPatient(patientRequestDTO);
        URI location = URI.create("/api/v1/patients/" + createdPatient.getId());
        return ResponseEntity.created(location).body(createdPatient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDTO patientRequestDTO
    ) {
        return ResponseEntity.ok(patientService.updatePatient(id, patientRequestDTO));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PatientResponseDTO> updatePatientStatus(
            @PathVariable Long id,
            @RequestBody PatientStatusPatchDTO patchDTO
    ) {
        return ResponseEntity.ok(patientService.updatePatientStatus(id, patchDTO.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> deletePatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.deletePatientById(id));
    }
}
