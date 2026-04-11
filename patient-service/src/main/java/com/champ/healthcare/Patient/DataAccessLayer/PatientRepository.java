package com.champ.healthcare.Patient.DataAccessLayer;

import com.champ.healthcare.Patient.Domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByContactInfoEmail(String email);

    Optional<Patient> findByPatientId_PatientId(String patientId);
}