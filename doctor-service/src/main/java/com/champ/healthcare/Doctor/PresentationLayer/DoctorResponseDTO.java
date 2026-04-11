package com.champ.healthcare.Doctor.PresentationLayer;

import com.champ.healthcare.Doctor.Domain.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponseDTO {

    private String doctorId;
    private String doctorFirstName;
    private String doctorLastName;
    private Boolean isActive;
    private Boolean isValid;
    private List<Speciality> speciality;
    private WorkZone workZone;
    private License license;

}

