package com.champ.healthcare.ApiGateway.Appointment.PresentationLayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequestDTO {

    private String patientId;
    private String doctorId;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private String status;
}
