package com.champ.healthcare.Doctor.PresentationLayer;

import com.champ.healthcare.Doctor.Domain.DoctorIdentifier;
import com.champ.healthcare.Doctor.Domain.Speciality;
import com.champ.healthcare.Doctor.Domain.WorkZone;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorRequestDTO {

    @NotBlank(message = "First name is required")
    private String doctorFirstName;

    @NotBlank(message = "Last name is required")
    private String doctorLastName;

    @NotNull(message = "Work zone is required")
    private WorkZone workZone;

    private List<Speciality> speciality;


}
