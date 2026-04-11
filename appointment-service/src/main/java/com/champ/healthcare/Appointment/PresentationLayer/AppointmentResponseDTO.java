package com.champ.healthcare.Appointment.PresentationLayer;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {

    private Long appointmentId;

    private String patientId;
    private String patientFullName;
    private String patientEmail;

    private String doctorId;
    private String doctorFullName;

    private String roomId;
    private String roomName;
    private String roomNumber;
    private String roomStatus;

    private String status;
    private LocalDateTime createdAt;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String description;
}