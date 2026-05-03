package com.champ.healthcare.ClinicRoom.Domain;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClinicRoomIdentifier {

    private String roomId;

    public ClinicRoomIdentifier() {
        this.roomId = UUID.randomUUID().toString();
    }

    public ClinicRoomIdentifier(String roomId) {
        this.roomId = roomId;
    }

    public ClinicRoomIdentifier(UUID uuid) {
        this.roomId = uuid.toString();
    }
}
