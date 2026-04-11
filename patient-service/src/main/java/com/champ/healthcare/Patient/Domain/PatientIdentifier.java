package com.champ.healthcare.Patient.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
public class PatientIdentifier {

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    public PatientIdentifier() {
        this.patientId = UUID.randomUUID().toString();
    }

    public PatientIdentifier(String patientId) {
        this.patientId = patientId;
    }
}