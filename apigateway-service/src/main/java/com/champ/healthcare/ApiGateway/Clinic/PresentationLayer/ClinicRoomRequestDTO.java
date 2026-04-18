package com.champ.healthcare.ApiGateway.Clinic.PresentationLayer;

import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomStatusPatchDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicRoomRequestDTO {

    @NotBlank(message = "Room name is required.")
    private String roomName;

    @NotBlank(message = "Room number is required.")
    private String roomNumber;

    @NotNull(message = "Room status is required.")
    private ClinicRoomStatusPatchDTO roomStatus;
}