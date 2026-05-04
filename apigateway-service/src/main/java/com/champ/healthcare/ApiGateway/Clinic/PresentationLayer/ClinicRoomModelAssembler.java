package com.champ.healthcare.ApiGateway.Clinic.PresentationLayer;

import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ClinicRoomModelAssembler {

    public ClinicRoomResponseDTO addLinks(ClinicRoomResponseDTO room) {
        if (room == null) {
            return null;
        }

        room.removeLinks();
        room.add(linkTo(methodOn(ClinicRoomController.class).getRoomById(room.getId())).withSelfRel());
        room.add(linkTo(methodOn(ClinicRoomController.class).getAllRooms()).withRel("clinicRooms"));
        room.add(linkTo(methodOn(ClinicRoomController.class)
                .getRoomByRoomId(room.getRoomId())).withRel("roomIdentifier"));
        room.add(linkTo(methodOn(ClinicRoomController.class)
                .updateRoom(room.getId(), null)).withRel("update"));
        room.add(linkTo(methodOn(ClinicRoomController.class)
                .updateRoomStatus(room.getId(), null)).withRel("status"));
        room.add(linkTo(methodOn(ClinicRoomController.class)
                .deleteRoom(room.getId())).withRel("delete"));
        return room;
    }

    public List<ClinicRoomResponseDTO> addLinks(List<ClinicRoomResponseDTO> rooms) {
        return rooms.stream()
                .map(this::addLinks)
                .toList();
    }
}
