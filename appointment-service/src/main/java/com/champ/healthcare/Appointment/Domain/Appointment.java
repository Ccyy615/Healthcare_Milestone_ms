package com.champ.healthcare.Appointment.Domain;

import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomIdentifier;
import com.champ.healthcare.Doctor.Domain.DoctorIdentifier;
import com.champ.healthcare.Patient.Domain.PatientIdentifier;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long appointmentId;

    @Embedded
    @AttributeOverride(
            name = "patientId",
            column = @Column(name = "patient_id", nullable = false)
    )
    private PatientIdentifier patientId;

    @Embedded
    @AttributeOverride(
            name = "doctorId",
            column = @Column(name = "doctor_id", nullable = false)
    )
    private DoctorIdentifier doctorId;

    @Embedded
    @AttributeOverride(
            name = "roomId",
            column = @Column(name = "room_id", nullable = false)
    )
    private ClinicRoomIdentifier roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_status", nullable = false)
    private AppointmentStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startTime", column = @Column(name = "appointment_start", nullable = false)),
            @AttributeOverride(name = "endTime", column = @Column(name = "appointment_end", nullable = false))
    })
    private TimeSlot timeSlot;

    @Column(name = "descriptions", length = 5000)
    private String description;

    public void validateTimeSlot() {
        if (timeSlot == null) {
            throw new IllegalArgumentException("Appointment time slot is required.");
        }
        timeSlot.validate();
    }

    public boolean overlapsWith(Appointment other) {
        if (other == null || other.getTimeSlot() == null || this.timeSlot == null) {
            return false;
        }
        return this.timeSlot.overlaps(other.getTimeSlot());
    }
}