package com.champ.healthcare.Appointment.PresentationLayer;

import com.champ.healthcare.Appointment.Domain.*;
import com.champ.healthcare.Doctor.Domain.DoctorIdentifier;
import com.champ.healthcare.Patient.Domain.PatientIdentifier;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalNoteResponseDTO {

    private Long noteId;

    private Appointment appointmentId;
    private DoctorIdentifier doctorId;
    private PatientIdentifier patientId;

    private String noteText;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdatedAt;

    private NoteType noteType;
}
