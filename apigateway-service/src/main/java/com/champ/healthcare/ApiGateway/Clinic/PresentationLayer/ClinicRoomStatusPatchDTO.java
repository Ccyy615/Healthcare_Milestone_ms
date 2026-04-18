package com.champ.healthcare.ApiGateway.Clinic.PresentationLayer;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicRoomStatusPatchDTO {
    private String roomStatus;
}
