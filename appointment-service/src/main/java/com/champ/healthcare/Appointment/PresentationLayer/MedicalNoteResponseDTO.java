package com.champ.healthcare.Appointment.PresentationLayer;

import com.champ.healthcare.Appointment.Domain.NoteType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalNoteResponseDTO {

    private Long noteId;
    private Long appointmentId;
    private String doctorId;
    private String patientId;
    private String noteText;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private NoteType noteType;
}