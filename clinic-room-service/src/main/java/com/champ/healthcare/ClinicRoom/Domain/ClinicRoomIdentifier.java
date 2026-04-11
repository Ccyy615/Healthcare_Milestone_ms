package com.champ.healthcare.ClinicRoom.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
public class ClinicRoomIdentifier {

    @Column(name = "room_id", nullable = false)
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