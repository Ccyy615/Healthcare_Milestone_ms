package com.champ.healthcare.Appointment.PresentationLayer;

import com.champ.healthcare.Appointment.Domain.*;
import com.champ.healthcare.Doctor.Domain.DoctorIdentifier;
import com.champ.healthcare.Patient.Domain.PatientIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalNoteRequestDTO {

    private Appointment appointmentId;
    private DoctorIdentifier doctorId;
    private PatientIdentifier patientId;

    private String noteText;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdatedAt;

    private NoteType noteType;
}
