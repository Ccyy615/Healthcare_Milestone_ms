package com.champ.healthcare.ApiGateway.Doctor.PresentationLayer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpecialityRequestDTO {

    @NotBlank(message = "Speciality is required")
    private String speciality;

    private String proficiencyLevel;
}