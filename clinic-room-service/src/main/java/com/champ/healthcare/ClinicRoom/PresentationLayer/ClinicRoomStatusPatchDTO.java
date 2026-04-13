package com.champ.healthcare.ClinicRoom.PresentationLayer;

import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicRoomStatusPatchDTO {
    private ClinicRoomStatus roomStatus;
}
