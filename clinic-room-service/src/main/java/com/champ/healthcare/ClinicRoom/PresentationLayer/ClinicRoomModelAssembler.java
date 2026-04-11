package com.champ.healthcare.ClinicRoom.PresentationLayer;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ClinicRoomModelAssembler {

    public EntityModel<ClinicRoomResponseDTO> toModel(ClinicRoomResponseDTO room) {
        return EntityModel.of(
                room,
                linkTo(methodOn(ClinicRoomController.class).getRoomById(room.getId())).withSelfRel(),
                linkTo(methodOn(ClinicRoomController.class).getAllRooms()).withRel("clinicRooms"),
                linkTo(methodOn(ClinicRoomController.class).updateRoom(room.getId(), null)).withRel("update"),
                linkTo(methodOn(ClinicRoomController.class).deleteRoom(room.getId())).withRel("delete")
        );
    }

    public CollectionModel<EntityModel<ClinicRoomResponseDTO>> toCollectionModel(List<ClinicRoomResponseDTO> rooms) {
        List<EntityModel<ClinicRoomResponseDTO>> roomModels = rooms.stream()
                .map(this::toModel)
                .toList();

        return CollectionModel.of(
                roomModels,
                linkTo(methodOn(ClinicRoomController.class).getAllRooms()).withSelfRel()
        );
    }
}