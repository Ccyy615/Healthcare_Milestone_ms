package com.champ.healthcare.Appointment.DomainClientLayer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PatientClientResponse(
        Long id,
        PatientIdentifierResponse patientId,
        String fullName,
        ContactInfoResponse contactInfo,
        String status
) {
    public String patientIdentifier() {
        return patientId != null ? patientId.patientId() : null;
    }

    public String email() {
        return contactInfo != null ? contactInfo.email() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PatientIdentifierResponse(String patientId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContactInfoResponse(String email, String phone) {
    }
}
