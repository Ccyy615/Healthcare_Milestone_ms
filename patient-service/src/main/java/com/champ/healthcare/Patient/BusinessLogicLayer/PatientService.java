package com.champ.healthcare.Patient.BusinessLogicLayer;

import com.champ.healthcare.Patient.PresentationLayer.PatientRequestDTO;
import com.champ.healthcare.Patient.PresentationLayer.PatientResponseDTO;
import com.champ.healthcare.Patient.Domain.PatientStatus;

import java.util.List;

public interface PatientService {
    List<PatientResponseDTO> getAllPatients();
    PatientResponseDTO getPatientById(Long id);
    PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO);
    PatientResponseDTO updatePatient(Long id, PatientRequestDTO patientRequestDTO);
    PatientResponseDTO updatePatientStatus(Long id, PatientStatus status);
    PatientResponseDTO deletePatientById(Long id);
}
