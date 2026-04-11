package com.champ.healthcare.Appointment.DataAccessLayer;

import com.champ.healthcare.Appointment.Domain.Appointment;
import com.champ.healthcare.Appointment.Domain.MedicalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalNoteRepository extends JpaRepository<MedicalNote, Long> {

    Optional<MedicalNote> findByNoteId(Long noteId);

}
