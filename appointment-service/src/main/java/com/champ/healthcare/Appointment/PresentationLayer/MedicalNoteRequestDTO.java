package com.champ.healthcare.Appointment.PresentationLayer;

import com.champ.healthcare.Appointment.Domain.NoteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalNoteRequestDTO {

    private Long appointmentId;
    private String doctorId;
    private String patientId;
    private String noteText;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private NoteType noteType;
}