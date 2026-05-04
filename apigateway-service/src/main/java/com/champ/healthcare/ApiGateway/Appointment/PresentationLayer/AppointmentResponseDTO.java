package com.champ.healthcare.ApiGateway.Appointment.PresentationLayer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppointmentResponseDTO extends RepresentationModel<AppointmentResponseDTO> {

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
