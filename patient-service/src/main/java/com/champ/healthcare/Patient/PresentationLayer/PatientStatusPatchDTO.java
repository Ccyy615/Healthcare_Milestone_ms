package com.champ.healthcare.Patient.PresentationLayer;

import com.champ.healthcare.Patient.Domain.PatientStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientStatusPatchDTO {
    private PatientStatus status;
}
