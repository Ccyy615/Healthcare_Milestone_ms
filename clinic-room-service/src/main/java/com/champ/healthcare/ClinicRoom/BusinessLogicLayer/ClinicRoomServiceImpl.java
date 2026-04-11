package com.champ.healthcare.ClinicRoom.BusinessLogicLayer;

import com.champ.healthcare.ClinicRoom.DataAccessLayer.ClinicRoomRepository;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoom;
import com.champ.healthcare.ClinicRoom.Mapper.ClinicRoomMapper;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomRequestDTO;
import com.champ.healthcare.ClinicRoom.PresentationLayer.ClinicRoomResponseDTO;
import com.champ.healthcare.ClinicRoom.utilities.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClinicRoomServiceImpl implements ClinicRoomService {

    private final ClinicRoomRepository clinicRoomRepository;
    private final ClinicRoomMapper clinicRoomMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClinicRoomResponseDTO> getAllRooms() {
        return clinicRoomMapper.toResponseDTOList(clinicRoomRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicRoomResponseDTO getRoomById(Long id) {
        ClinicRoom room = clinicRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic room not found with id: " + id));

        return clinicRoomMapper.toResponseDTO(room);
    }

    @Override
    @Transactional
    public ClinicRoomResponseDTO createRoom(ClinicRoomRequestDTO requestDTO) {
        validateRequest(requestDTO);

        if (clinicRoomRepository.existsByRoomNumber(requestDTO.getRoomNumber())) {
            throw new IllegalStateException("A clinic room with this room number already exists.");
        }

        ClinicRoom room = clinicRoomMapper.toEntity(requestDTO);
        ClinicRoom savedRoom = clinicRoomRepository.save(room);

        return clinicRoomMapper.toResponseDTO(savedRoom);
    }

    @Override
    @Transactional
    public ClinicRoomResponseDTO updateRoom(Long id, ClinicRoomRequestDTO requestDTO) {
        validateRequest(requestDTO);

        ClinicRoom existingRoom = clinicRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic room not found with id: " + id));

        if (clinicRoomRepository.existsByRoomNumberAndIdNot(requestDTO.getRoomNumber(), id)) {
            throw new IllegalStateException("A clinic room with this room number already exists.");
        }

        existingRoom.setRoomName(requestDTO.getRoomName());
        existingRoom.setRoomNumber(requestDTO.getRoomNumber());
        existingRoom.setRoomStatus(requestDTO.getRoomStatus());

        ClinicRoom savedRoom = clinicRoomRepository.save(existingRoom);
        return clinicRoomMapper.toResponseDTO(savedRoom);
    }

    @Override
    @Transactional
    public ClinicRoomResponseDTO deleteRoom(Long id) {
        ClinicRoom room = clinicRoomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic room not found with id: " + id));

        clinicRoomRepository.delete(room);
        return clinicRoomMapper.toResponseDTO(room);
    }

    private void validateRequest(ClinicRoomRequestDTO requestDTO) {
        if (requestDTO.getRoomName() == null || requestDTO.getRoomName().isBlank()) {
            throw new IllegalArgumentException("Room name is required.");
        }

        if (requestDTO.getRoomNumber() == null || requestDTO.getRoomNumber().isBlank()) {
            throw new IllegalArgumentException("Room number is required.");
        }

        if (requestDTO.getRoomStatus() == null) {
            throw new IllegalArgumentException("Room status is required.");
        }
    }
}