package com.champ.healthcare.ClinicRoom.PresentationLayer;

import com.champ.healthcare.ClinicRoom.BusinessLogicLayer.ClinicRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/api/v1/clinic-rooms")
@RequiredArgsConstructor
public class ClinicRoomController {

    private final ClinicRoomService clinicRoomService;

    @GetMapping
    public ResponseEntity<List<ClinicRoomResponseDTO>> getAllRooms() {
        List<ClinicRoomResponseDTO> rooms = clinicRoomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicRoomResponseDTO> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(clinicRoomService.getRoomById(id));
    }

    @GetMapping("/room-identifier/{roomId}")
    public ResponseEntity<ClinicRoomResponseDTO> getRoomByRoomId(@PathVariable String roomId) {
        return ResponseEntity.ok(clinicRoomService.getRoomByRoomId(roomId));
    }

    @PostMapping
    public ResponseEntity<ClinicRoomResponseDTO> createRoom(
            @Valid @RequestBody ClinicRoomRequestDTO requestDTO
    ) {
        ClinicRoomResponseDTO createdRoom = clinicRoomService.createRoom(requestDTO);
        URI location = URI.create("/api/v1/clinic-rooms/" + createdRoom.getId());
        return ResponseEntity.created(location).body(createdRoom);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicRoomResponseDTO> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody ClinicRoomRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(clinicRoomService.updateRoom(id, requestDTO));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClinicRoomResponseDTO> updateRoomStatus(
            @PathVariable Long id,
            @RequestBody ClinicRoomStatusPatchDTO patchDTO
    ) {
        return ResponseEntity.ok(clinicRoomService.updateRoomStatus(id, patchDTO.getRoomStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ClinicRoomResponseDTO> deleteRoom(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(clinicRoomService.deleteRoom(id));
    }
}
