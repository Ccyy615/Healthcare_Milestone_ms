package com.champ.healthcare.Doctor.PresentationLayer;

import com.champ.healthcare.Doctor.BusinessLogicLayer.DoctorService;
import com.champ.healthcare.Doctor.Domain.Speciality;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorModelAssembler doctorModelAssembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<DoctorResponseDTO>>> getAllDoctors() {
        List<DoctorResponseDTO> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctorModelAssembler.toCollectionModel(doctors));
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<EntityModel<DoctorResponseDTO>> getDoctorById(@PathVariable String doctorId) {
        DoctorResponseDTO doctor = doctorService.getDoctorById(doctorId);
        return ResponseEntity.ok(doctorModelAssembler.toModel(doctor));
    }

    @PostMapping
    public ResponseEntity<EntityModel<DoctorResponseDTO>> createDoctor(
            @Valid @RequestBody DoctorRequestDTO doctorRequestDTO
    ) {
        DoctorResponseDTO createdDoctor = doctorService.createDoctor(doctorRequestDTO);

        EntityModel<DoctorResponseDTO> model = doctorModelAssembler.toModel(createdDoctor);
        URI location = linkTo(methodOn(DoctorController.class).getDoctorById(createdDoctor.getDoctorId())).toUri();

        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{doctorId}")
    public ResponseEntity<EntityModel<DoctorResponseDTO>> updateDoctor(
            @PathVariable String doctorId,
            @Valid @RequestBody DoctorRequestDTO doctorRequestDTO
    ) {
        DoctorResponseDTO updatedDoctor = doctorService.updateDoctor(doctorId, doctorRequestDTO);
        return ResponseEntity.ok(doctorModelAssembler.toModel(updatedDoctor));
    }

    @GetMapping("/active")
    public ResponseEntity<CollectionModel<EntityModel<DoctorResponseDTO>>> getActiveDoctors() {
        List<DoctorResponseDTO> doctors = doctorService.getActiveDoctors();
        return ResponseEntity.ok(doctorModelAssembler.toCollectionModel(doctors));
    }

    @GetMapping("/active/speciality/{specialityName}")
    public ResponseEntity<CollectionModel<EntityModel<DoctorResponseDTO>>> getActiveDoctorBySpeciality(
            @PathVariable String specialityName
    ) {
        List<DoctorResponseDTO> doctors = doctorService.getActiveDoctorBySpeciality(specialityName);
        return ResponseEntity.ok(doctorModelAssembler.toCollectionModel(doctors));
    }

    @PostMapping("/{doctorId}/activate")
    public ResponseEntity<EntityModel<DoctorResponseDTO>> activateDoctor(@PathVariable String doctorId) {
        DoctorResponseDTO doctor = doctorService.activateDoctor(doctorId);
        return ResponseEntity.ok(doctorModelAssembler.toModel(doctor));
    }

    @PostMapping("/{doctorId}/deactivate")
    public ResponseEntity<EntityModel<DoctorResponseDTO>> deactivateDoctor(@PathVariable String doctorId) {
        DoctorResponseDTO doctor = doctorService.deactivateDoctor(doctorId);
        return ResponseEntity.ok(doctorModelAssembler.toModel(doctor));
    }

    @PostMapping("/{doctorId}/speciality")
    public ResponseEntity<EntityModel<DoctorResponseDTO>> addSpeciality(
            @PathVariable String doctorId,
            @Valid @RequestBody Speciality specialityDTO
    ) {
        DoctorResponseDTO doctor = doctorService.addSpeciality(doctorId, specialityDTO);
        return ResponseEntity.ok(doctorModelAssembler.toModel(doctor));
    }

    @DeleteMapping("/{doctorId}/speciality/{specialityName}")
    public ResponseEntity<EntityModel<DoctorResponseDTO>> removeSpeciality(
            @PathVariable String doctorId,
            @PathVariable String specialityName
    ) {
        DoctorResponseDTO doctor = doctorService.removeSpeciality(doctorId, specialityName);
        return ResponseEntity.ok(doctorModelAssembler.toModel(doctor));
    }

    @PostMapping("/{doctorId}/license")
    public ResponseEntity<EntityModel<DoctorResponseDTO>> addLicense(
            @PathVariable String doctorId,
            @Valid @RequestBody LicenseRequestDTO requestDTO
    ) {
        DoctorResponseDTO doctor = doctorService.addLicense(doctorId, requestDTO);
        return ResponseEntity.ok(doctorModelAssembler.toModel(doctor));
    }

    @DeleteMapping("/{doctorId}")
    public ResponseEntity<EntityModel<DoctorResponseDTO>> deleteDoctor(@PathVariable String doctorId) {
        DoctorResponseDTO doctor = doctorService.getDoctorById(doctorId);
        doctorService.deleteDoctor(doctorId);
        return ResponseEntity.status(HttpStatus.OK).body(doctorModelAssembler.toModel(doctor));
    }
}