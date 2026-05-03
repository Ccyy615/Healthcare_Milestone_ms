package com.champ.healthcare.ClinicRoom.Domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clinic_rooms")
@CompoundIndexes({
        @CompoundIndex(name = "clinic_room_public_id_idx", def = "{'id': 1}", unique = true),
        @CompoundIndex(name = "clinic_room_identifier_idx", def = "{'roomId.roomId': 1}", unique = true),
        @CompoundIndex(name = "clinic_room_number_idx", def = "{'roomNumber': 1}", unique = true)
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ClinicRoom {

    @Id
    private String documentId;

    private Long id;

    private ClinicRoomIdentifier roomId;

    private String roomName;

    private String roomNumber;

    private ClinicRoomStatus roomStatus;

    public boolean isAvailableForBooking() {
        return roomStatus == ClinicRoomStatus.AVAILABLE;
    }
}
