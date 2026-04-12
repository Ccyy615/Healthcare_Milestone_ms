package com.champ.healthcare.ClinicRoom.PresentationLayer;

import com.champ.healthcare.ClinicRoom.BusinessLogicLayer.ClinicRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ClinicRoomResponseDTO>> getRoomById(@PathVariable Long id) {
        ClinicRoomResponseDTO room = clinicRoomService.getRoomById(id);
        return ResponseEntity.ok(clinicRoomModelAssembler.toModel(room));
    }

    @PostMapping
    public ResponseEntity<EntityModel<ClinicRoomResponseDTO>> createRoom(
            @Valid @RequestBody ClinicRoomRequestDTO requestDTO
    ) {
        ClinicRoomResponseDTO createdRoom = clinicRoomService.createRoom(requestDTO);

//        EntityModel<ClinicRoomResponseDTO> model = clinicRoomModelAssembler.toModel(createdRoom);
//
//        URI location = linkTo(methodOn(ClinicRoomController.class).getRoomById(createdRoom.getId())).toUri();

//        return ResponseEntity.created(location).body(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(clinicRoomModelAssembler.toModel(createdRoom));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ClinicRoomResponseDTO>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody ClinicRoomRequestDTO requestDTO
    ) {
        ClinicRoomResponseDTO updatedRoom = clinicRoomService.updateRoom(id, requestDTO);
        return ResponseEntity.ok(clinicRoomModelAssembler.toModel(updatedRoom));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EntityModel<ClinicRoomResponseDTO>> deleteRoom(@PathVariable Long id) {
        ClinicRoomResponseDTO deletedRoom = clinicRoomService.deleteRoom(id);
        return ResponseEntity.status(HttpStatus.OK).body(clinicRoomModelAssembler.toModel(deletedRoom));
    }
}