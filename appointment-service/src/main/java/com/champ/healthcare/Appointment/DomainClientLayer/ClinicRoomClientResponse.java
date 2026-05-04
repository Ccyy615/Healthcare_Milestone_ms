package com.champ.healthcare.Appointment.DomainClientLayer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClinicRoomClientResponse(
        Long id,
        String roomId,
        String roomName,
        String roomNumber,
        String roomStatus
) {
}
