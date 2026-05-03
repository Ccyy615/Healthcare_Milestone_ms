package com.champ.healthcare.ApiGateway.Clinic.PresentationLayer;

import com.champ.healthcare.ApiGateway.Clinic.BusinessLogicLayer.ClinicRoomService;
import com.champ.healthcare.ApiGateway.Clinic.PresentationLayer.*;
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
    private final ClinicRoomModelAssembler clinicRoomModelAssembler;

    @GetMapping
    public ResponseEntity<List<ClinicRoomResponseDTO>> getAllRooms() {
        List<ClinicRoomResponseDTO> rooms = clinicRoomService.getAllRooms();
        return ResponseEntity.ok(clinicRoomModelAssembler.addLinks(rooms));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicRoomResponseDTO> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(clinicRoomModelAssembler.addLinks(clinicRoomService.getRoomById(id)));
    }

    @GetMapping("/room-identifier/{roomId}")
    public ResponseEntity<ClinicRoomResponseDTO> getRoomByRoomId(@PathVariable String roomId) {
        return ResponseEntity.ok(clinicRoomModelAssembler.addLinks(
                clinicRoomService.getRoomByRoomId(roomId)
        ));
    }

    @PostMapping
    public ResponseEntity<ClinicRoomResponseDTO> createRoom(
            @Valid @RequestBody ClinicRoomRequestDTO requestDTO
    ) {
        ClinicRoomResponseDTO createdRoom = clinicRoomModelAssembler.addLinks(
                clinicRoomService.createRoom(requestDTO)
        );
        URI location = URI.create("/api/v1/clinic-rooms/" + createdRoom.getId());
        return ResponseEntity.created(location).body(createdRoom);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicRoomResponseDTO> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody ClinicRoomRequestDTO requestDTO
    ) {
        return ResponseEntity.ok(clinicRoomModelAssembler.addLinks(
                clinicRoomService.updateRoom(id, requestDTO)
        ));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ClinicRoomResponseDTO> updateRoomStatus(
            @PathVariable Long id,
            @RequestBody ClinicRoomStatusPatchDTO patchDTO
    ) {
        return ResponseEntity.ok(clinicRoomModelAssembler.addLinks(
                clinicRoomService.updateRoomStatus(id, patchDTO)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ClinicRoomResponseDTO> deleteRoom(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(clinicRoomModelAssembler.addLinks(clinicRoomService.deleteRoom(id)));
    }
}
