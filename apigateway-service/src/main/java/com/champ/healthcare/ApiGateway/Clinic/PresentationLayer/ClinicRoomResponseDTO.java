package com.champ.healthcare.ApiGateway.Clinic.PresentationLayer;

import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.ClinicRoomStatusPatchDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicRoomResponseDTO extends RepresentationModel<ClinicRoomResponseDTO> {

    private Long id;
    private String roomId;
    private String roomName;
    private String roomNumber;
    private ClinicRoomStatusPatchDTO roomStatus;
}
