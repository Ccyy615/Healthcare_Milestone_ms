package com.champ.healthcare.ApiGateway.Patient.PresentationLayer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientStatusPatchDTO {
    private String status;
}
