package com.champ.healthcare.Doctor.BusinessLogicLayer;

import com.champ.healthcare.Doctor.DataAccessLayer.DoctorRepository;
import com.champ.healthcare.Doctor.Domain.Doctor;
import com.champ.healthcare.Doctor.Domain.License;
import com.champ.healthcare.Doctor.Domain.Speciality;
import com.champ.healthcare.Doctor.Mapper.DoctorMapper;
import com.champ.healthcare.Doctor.PresentationLayer.DoctorRequestDTO;
import com.champ.healthcare.Doctor.PresentationLayer.DoctorResponseDTO;
import com.champ.healthcare.Doctor.PresentationLayer.LicenseRequestDTO;
import com.champ.healthcare.Doctor.utilities.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getAllDoctors() {
        log.info("Fetching all doctors");

        return doctorRepository.findAll().stream()
                .map(doctorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(String doctorId) {
        log.info("Fetching doctor with ID: {}", doctorId);

        Doctor doctor = doctorRepository
                .findByDoctorId_DoctorId(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));

        return doctorMapper.toResponseDTO(doctor);
    }

    @Transactional
    public DoctorResponseDTO createDoctor(DoctorRequestDTO requestDTO) {
        Doctor doctor = doctorMapper.toEntity(requestDTO);

        log.info("Doctor profile created with ID: {}", doctor.getDoctorId().getDoctorId());

        Doctor savedDoctor = doctorRepository.save(doctor);

        log.info("Doctor saved with ID: {}", savedDoctor.getDoctorId().getDoctorId());
        return doctorMapper.toResponseDTO(savedDoctor);
    }

    @Transactional
    public DoctorResponseDTO updateDoctor(String doctorId, DoctorRequestDTO requestDTO) {
        log.info("Updating doctor with ID: {}", doctorId);

        Doctor existingDoctor = doctorRepository
                .findByDoctorId_DoctorId(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));

        Doctor updatedDoctorData = doctorMapper.toEntity(requestDTO);

        // Keep existing identifiers and state
        updatedDoctorData.setId(existingDoctor.getId());
        updatedDoctorData.setDoctorId(existingDoctor.getDoctorId());
        updatedDoctorData.setIsActive(existingDoctor.getIsActive());
        updatedDoctorData.setIsValid(existingDoctor.getIsValid());
        updatedDoctorData.setLicense(existingDoctor.getLicense());

        Doctor savedDoctor = doctorRepository.save(updatedDoctorData);
        return doctorMapper.toResponseDTO(savedDoctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getActiveDoctors() {
        log.info("Fetching active doctors");

        return doctorRepository.findByIsActiveTrue().stream()
                .map(doctorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getActiveDoctorBySpeciality(String specialityName) {
        log.info("Fetching active doctors with speciality: {}", specialityName);

        List<Speciality> specialities = new ArrayList<>();
        specialities.add(new Speciality(specialityName, null));

        return doctorRepository.findByIsActiveTrueAndSpecialityIn(specialities).stream()
                .map(doctorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorResponseDTO activateDoctor(String doctorId) {
        log.info("Activating doctor with ID: {}", doctorId);

        Doctor doctor = doctorRepository
                .findByDoctorId_DoctorId(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));

        try {
            doctor.activate();
            Doctor savedDoctor = doctorRepository.save(doctor);
            log.info("Doctor activated successfully: {}", doctorId);
            return doctorMapper.toResponseDTO(savedDoctor);
        } catch (DoctorNotEligibleException e) {
            log.error("Failed to activate doctor {}: {}", doctorId, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public DoctorResponseDTO deactivateDoctor(String doctorId) {
        log.info("Deactivating doctor with ID: {}", doctorId);

        Doctor doctor = doctorRepository
                .findByDoctorId_DoctorId(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));

        doctor.deactivate();
        Doctor savedDoctor = doctorRepository.save(doctor);

        log.info("Doctor deactivated successfully: {}", doctorId);
        return doctorMapper.toResponseDTO(savedDoctor);
    }

    @Transactional
    public DoctorResponseDTO updateDoctorActivation(String doctorId, Boolean active) {
        if (active == null) {
            throw new IllegalArgumentException("Doctor activation flag is required.");
        }

        return active ? activateDoctor(doctorId) : deactivateDoctor(doctorId);
    }

    @Transactional
    public DoctorResponseDTO addSpeciality(String doctorId, Speciality specialityDTO) {
        log.info("Adding speciality {} to doctor {}", specialityDTO.getSpeciality(), doctorId);

        Doctor doctor = doctorRepository
                .findByDoctorId_DoctorId(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));

        Speciality speciality = new Speciality(
                specialityDTO.getSpeciality(),
                specialityDTO.getProficiencyLevel()
        );

        doctor.addSpeciality(speciality);

        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDTO(savedDoctor);
    }

    @Transactional
    public DoctorResponseDTO removeSpeciality(String doctorId, String specialityName) {
        log.info("Removing speciality {} from doctor {}", specialityName, doctorId);

        Doctor doctor = doctorRepository
                .findByDoctorId_DoctorId(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));

        doctor.removeSpeciality(specialityName);

        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDTO(savedDoctor);
    }

    @Transactional
    public DoctorResponseDTO addLicense(String doctorId, LicenseRequestDTO requestDTO) {
        log.info("Adding a license for doctor {}", doctorId);

        Doctor doctor = doctorRepository
                .findByDoctorId_DoctorId(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));

        License license = new License(
                requestDTO.getLicenseName(),
                requestDTO.getStatus(),
                LocalDateTime.now()
        );

        doctor.setLicense(license);

        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toResponseDTO(savedDoctor);
    }

    @Transactional
    public void deleteDoctor(String doctorId) {
        log.info("Deleting doctor with ID: {}", doctorId);

        Doctor doctor = doctorRepository
                .findByDoctorId_DoctorId(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with ID: " + doctorId));

        doctorRepository.delete(doctor);
        log.info("Doctor deleted successfully: {}", doctorId);
    }
}
