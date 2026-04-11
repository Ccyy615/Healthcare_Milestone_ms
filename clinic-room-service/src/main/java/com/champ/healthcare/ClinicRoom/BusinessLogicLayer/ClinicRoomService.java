package com.champ.healthcare.ClinicRoom.BusinessLogicLayer;

import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomResponseDTO;

import java.util.List;

public interface ClinicRoomService {

    List<ClinicRoomResponseDTO> getAllRooms();

    ClinicRoomResponseDTO getRoomById(Long id);

    ClinicRoomResponseDTO createRoom(ClinicRoomRequestDTO requestDTO);

    ClinicRoomResponseDTO updateRoom(Long id, ClinicRoomRequestDTO requestDTO);

    ClinicRoomResponseDTO deleteRoom(Long id);
}