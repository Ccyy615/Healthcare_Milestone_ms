package com.champ.healthcare.ApiGateway.Doctor.PresentationLayer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorRequestDTO {

    @NotBlank(message = "First name is required")
    private String doctorFirstName;

    @NotBlank(message = "Last name is required")
    private String doctorLastName;

    @NotNull(message = "Work zone is required")
//    private WorkZone workZone;
    private String city;
    private String province;

//    private List<Speciality> speciality;
    private SpecialityRequestDTO speciality;




}
