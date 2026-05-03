package com.champ.healthcare.ClinicRoom.BusinessLogicLayer;

import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomResponseDTO;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomStatus;

import java.util.List;

public interface ClinicRoomService {

    List<ClinicRoomResponseDTO> getAllRooms();

    ClinicRoomResponseDTO getRoomById(Long id);

    ClinicRoomResponseDTO getRoomByRoomId(String roomId);

    ClinicRoomResponseDTO createRoom(ClinicRoomRequestDTO requestDTO);

    ClinicRoomResponseDTO updateRoom(Long id, ClinicRoomRequestDTO requestDTO);

    ClinicRoomResponseDTO updateRoomStatus(Long id, ClinicRoomStatus roomStatus);

    ClinicRoomResponseDTO deleteRoom(Long id);
}
