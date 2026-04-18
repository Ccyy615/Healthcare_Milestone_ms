package com.champ.healthcare.ApiGateway.Doctor.PresentationLayer;

import com.champ.healthcare.ApiGateway.Doctor.BusinessLogicLayer.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable String doctorId) {
        return ResponseEntity.ok(doctorService.getDoctorById(doctorId));
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(
            @Valid @RequestBody DoctorRequestDTO doctorRequestDTO
    ) {
        DoctorResponseDTO createdDoctor = doctorService.createDoctor(doctorRequestDTO);
        URI location = URI.create("/api/v1/doctors/" + createdDoctor.getDoctorId());
        return ResponseEntity.created(location).body(createdDoctor);
    }

    @PutMapping("/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable String doctorId,
            @Valid @RequestBody DoctorRequestDTO doctorRequestDTO
    ) {
        return ResponseEntity.ok(doctorService.updateDoctor(doctorId, doctorRequestDTO));
    }

    @GetMapping("/active")
    public ResponseEntity<List<DoctorResponseDTO>> getActiveDoctors() {
        return ResponseEntity.ok(doctorService.getActiveDoctors());
    }

    @GetMapping("/active/speciality/{specialityName}")
    public ResponseEntity<List<DoctorResponseDTO>> getActiveDoctorBySpeciality(
            @PathVariable String specialityName
    ) {
        return ResponseEntity.ok(doctorService.getActiveDoctorBySpeciality(specialityName));
    }

    @PostMapping("/{doctorId}/activate")
    public ResponseEntity<DoctorResponseDTO> activateDoctor(@PathVariable String doctorId) {
        return ResponseEntity.ok(doctorService.activateDoctor(doctorId));
    }

    @PostMapping("/{doctorId}/deactivate")
    public ResponseEntity<DoctorResponseDTO> deactivateDoctor(@PathVariable String doctorId) {
        return ResponseEntity.ok(doctorService.deactivateDoctor(doctorId));
    }

    @PatchMapping("/{doctorId}/activation")
    public ResponseEntity<DoctorResponseDTO> updateDoctorActivation(
            @PathVariable String doctorId,
            @RequestBody DoctorActivationPatchDTO patchDTO
    ) {
        return ResponseEntity.ok(doctorService.updateDoctorActivation(doctorId, patchDTO.getActive()));
    }

    @PostMapping("/{doctorId}/speciality")
    public ResponseEntity<DoctorResponseDTO> addSpeciality(
            @PathVariable String doctorId,
            @Valid @RequestBody SpecialityRequestDTO specialityDTO
    ) {
        return ResponseEntity.ok(doctorService.addSpeciality(doctorId, specialityDTO));
    }

    @DeleteMapping("/{doctorId}/speciality/{specialityName}")
    public ResponseEntity<DoctorResponseDTO> removeSpeciality(
            @PathVariable String doctorId,
            @PathVariable String specialityName
    ) {
        return ResponseEntity.ok(doctorService.removeSpeciality(doctorId, specialityName));
    }

    @PostMapping("/{doctorId}/license")
    public ResponseEntity<DoctorResponseDTO> addLicense(
            @PathVariable String doctorId,
            @Valid @RequestBody LicenseRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(doctorService.addLicense(doctorId, requestDTO));
    }

    @DeleteMapping("/{doctorId}")
    public ResponseEntity<DoctorResponseDTO> deleteDoctor(@PathVariable String doctorId) {
        DoctorResponseDTO doctor = doctorService.getDoctorById(doctorId);
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.status(HttpStatus.OK).body(doctor);
    }
}