package com.champ.healthcare.ApiGateway.Patient.BusinessLogicLayer;

import com.champ.healthcare.ApiGateway.Patient.PresentationLayer.*;

import java.util.List;

public interface PatientService {
    List<PatientResponseDTO> getAllPatients();
    PatientResponseDTO getPatientById(Long id);
    PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO);
    PatientResponseDTO updatePatient(Long id, PatientRequestDTO patientRequestDTO);
    PatientResponseDTO updatePatientStatus(Long id, PatientStatusPatchDTO status);
    PatientResponseDTO deletePatientById(Long id);
}
