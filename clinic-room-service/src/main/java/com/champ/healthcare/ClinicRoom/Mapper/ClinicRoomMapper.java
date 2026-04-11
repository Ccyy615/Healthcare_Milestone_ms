package com.champ.healthcare.ClinicRoom.Mapper;

import com.champ.healthcare.ClinicRoom.Domain.ClinicRoom;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomIdentifier;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClinicRoomMapper {

    public ClinicRoom toEntity(ClinicRoomRequestDTO dto) {
        return ClinicRoom.builder()
                .roomId(new ClinicRoomIdentifier())
                .roomName(dto.getRoomName())
                .roomNumber(dto.getRoomNumber())
                .roomStatus(dto.getRoomStatus())
                .build();
    }

    public ClinicRoomResponseDTO toResponseDTO(ClinicRoom room) {
        return ClinicRoomResponseDTO.builder()
                .id(room.getId())
                .roomId(room.getRoomId() != null ? room.getRoomId().getRoomId() : null)
                .roomName(room.getRoomName())
                .roomNumber(room.getRoomNumber())
                .roomStatus(room.getRoomStatus())
                .build();
    }

    public List<ClinicRoomResponseDTO> toResponseDTOList(List<ClinicRoom> rooms) {
        return rooms.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}