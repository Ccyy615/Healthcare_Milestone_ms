package com.champ.healthcare.ApiGateway.Clinic.BusinessLogicLayer;

import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.*;

import java.util.List;

public interface ClinicRoomService {

    List<ClinicRoomResponseDTO> getAllRooms();

    ClinicRoomResponseDTO getRoomById(Long id);

    ClinicRoomResponseDTO getRoomByRoomId(String roomId);

    ClinicRoomResponseDTO createRoom(ClinicRoomRequestDTO requestDTO);

    ClinicRoomResponseDTO updateRoom(Long id, ClinicRoomRequestDTO requestDTO);

    ClinicRoomResponseDTO updateRoomStatus(Long id, ClinicRoomStatusPatchDTO roomStatus);

    ClinicRoomResponseDTO deleteRoom(Long id);


}
