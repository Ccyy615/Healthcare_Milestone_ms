package com.champ.healthcare.Patient.BusinessLogicLayer;

import com.champ.healthcare.Patient.DataAccessLayer.PatientRepository;
import com.champ.healthcare.Patient.Domain.ContactInfo;
import com.champ.healthcare.Patient.Domain.Patient;
import com.champ.healthcare.Patient.Mapper.PatientMapper;
import com.champ.healthcare.Patient.PresentationLayer.PatientRequestDTO;
import com.champ.healthcare.Patient.PresentationLayer.PatientResponseDTO;
import com.champ.healthcare.Patient.utilities.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(patientMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        return patientMapper.toResponseDTO(patient);
    }

    @Override
    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        log.info("Creating patient for email: {}", patientRequestDTO.getContactInfo() != null
                ? patientRequestDTO.getContactInfo().getEmail()
                : null);

        validatePatientInvariant(patientRequestDTO);

        String email = patientRequestDTO.getContactInfo() != null
                ? patientRequestDTO.getContactInfo().getEmail()
                : null;

        if (email != null && !email.isBlank()
                && patientRepository.existsByContactInfoEmail(email)) {
            throw new DuplicateEmailException("Email already exists: " + email);
        }

        Patient patient = patientMapper.toEntity(patientRequestDTO);
        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toResponseDTO(savedPatient);
    }

    @Override
    @Transactional
    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO patientRequestDTO) {
        log.info("Updating patient for id: {}", id);

        validatePatientInvariant(patientRequestDTO);

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        String newEmail = patientRequestDTO.getContactInfo() != null
                ? patientRequestDTO.getContactInfo().getEmail()
                : null;

        String currentEmail = existingPatient.getContactInfo() != null
                ? existingPatient.getContactInfo().getEmail()
                : null;

        if (newEmail != null && !newEmail.isBlank()
                && !newEmail.equals(currentEmail)
                && patientRepository.existsByContactInfoEmail(newEmail)) {
            throw new DuplicateEmailException("Email already exists: " + newEmail);
        }

        Patient updatedPatientData = patientMapper.toEntity(patientRequestDTO);
        updatedPatientData.setId(existingPatient.getId());
        updatedPatientData.setPatientId(existingPatient.getPatientId());

        Patient savedPatient = patientRepository.save(updatedPatientData);
        return patientMapper.toResponseDTO(savedPatient);
    }

    @Override
    @Transactional
    public PatientResponseDTO deletePatientById(Long id) {
        log.info("Deleting patient for id: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        patientRepository.delete(patient);
        return patientMapper.toResponseDTO(patient);
    }

    private void validatePatientInvariant(PatientRequestDTO patientRequestDTO) {
        ContactInfo contactInfo = patientRequestDTO.getContactInfo();

        if (contactInfo == null) {
            throw new IllegalArgumentException(
                    "Patient must have at least one valid contact method: email or phone."
            );
        }

        boolean hasEmail = contactInfo.getEmail() != null && !contactInfo.getEmail().isBlank();
        boolean hasPhone = contactInfo.getPhone() != null && !contactInfo.getPhone().isBlank();

        if (!hasEmail && !hasPhone) {
            throw new IllegalArgumentException(
                    "Patient must have at least one valid contact method: email or phone."
            );
        }
    }
}