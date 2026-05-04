package com.champ.healthcare.Appointment.DomainClientLayer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DoctorClientResponse(
        String doctorId,
        String doctorFirstName,
        String doctorLastName,
        Boolean isActive,
        Boolean isValid
) {
    public String fullName() {
        return ((doctorFirstName == null ? "" : doctorFirstName) + " "
                + (doctorLastName == null ? "" : doctorLastName)).trim();
    }
}
