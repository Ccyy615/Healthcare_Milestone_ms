package com.champ.healthcare.ClinicRoom.Domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clinic_rooms")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ClinicRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Embedded
    @AttributeOverride(
            name = "roomId",
            column = @Column(name = "room_id", nullable = false, unique = true)
    )
    private ClinicRoomIdentifier roomId;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false)
    private ClinicRoomStatus roomStatus;

    public boolean isAvailableForBooking() {
        return roomStatus == ClinicRoomStatus.AVAILABLE;
    }
}