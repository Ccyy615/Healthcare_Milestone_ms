package com.champ.healthcare.Doctor.DataAccessLayer;

import com.champ.healthcare.Doctor.Domain.Doctor;
import com.champ.healthcare.Doctor.Domain.Speciality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByDoctorId_DoctorId(String doctorId);

    List<Doctor> findByIsActiveTrue();

    List<Doctor> findByIsActiveTrueAndSpecialityIn(List<Speciality> speciality);
}