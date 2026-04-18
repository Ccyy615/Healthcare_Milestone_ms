package com.champ.healthcare.Appointment.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "medical_notes")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalNote {

    @Id
    @GeneratedValue
    @Column(name = "note_id")
    private Long noteId;

    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "doctor_id", nullable = false)
    private String doctorId;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "text")
    private String noteText;

    @Column(name = "note_create_at")
    private LocalDateTime createdAt;

    @Column(name = "last_update")
    private LocalDateTime lastUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type")
    private NoteType noteType;
}