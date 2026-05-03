package com.champ.healthcare.Patient.BusinessLogicLayer;

import com.champ.healthcare.Patient.DataAccessLayer.PatientRepository;
import com.champ.healthcare.Patient.Domain.ContactInfo;
import com.champ.healthcare.Patient.Domain.Patient;
import com.champ.healthcare.Patient.Domain.PatientStatus;
import com.champ.healthcare.Patient.Mapper.PatientMapper;
import com.champ.healthcare.Patient.PresentationLayer.PatientRequestDTO;
import com.champ.healthcare.Patient.PresentationLayer.PatientResponseDTO;
import com.champ.healthcare.Patient.utilities.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientByPatientIdentifier(String patientId) {
        Patient patient = patientRepository.findByPatientId_PatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with patientId: " + patientId));

        return patientMapper.toResponseDTO(patient);
    }

    @Override
    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        log.info("Creating patient for email: {}", patientRequestDTO.getContactInfo() != null
                ? patientRequestDTO.getContactInfo().getEmail()
                : null);

        validatePatientInvariant(patientRequestDTO);

        String email = patientRequestDTO.getContactInfo().getEmail();

        if (StringUtils.hasText(email) && patientRepository.existsByContactInfoEmail(email)) {
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

        String newEmail = patientRequestDTO.getContactInfo().getEmail();
        String currentEmail = existingPatient.getContactInfo().getEmail();

        if (StringUtils.hasText(newEmail)
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
    public PatientResponseDTO updatePatientStatus(Long id, PatientStatus status) {
        log.info("Patching patient status for id: {}", id);

        if (status == null) {
            throw new IllegalArgumentException("Patient status is required.");
        }

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        if (status == PatientStatus.ACTIVE) {
            patient.activate();
        } else {
            patient.deactivate();
        }

        Patient savedPatient = patientRepository.save(patient);
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

        boolean hasEmail = StringUtils.hasText(contactInfo.getEmail());
        boolean hasPhone = StringUtils.hasText(contactInfo.getPhone());

        if (!hasEmail && !hasPhone) {
            throw new IllegalArgumentException(
                    "Patient must have at least one valid contact method: email or phone."
            );
        }
    }
}
