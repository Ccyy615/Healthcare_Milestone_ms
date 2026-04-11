package com.champ.healthcare.ClinicRoom.DataAccessLayer;

import com.champ.healthcare.ClinicRoom.Domain.ClinicRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicRoomRepository extends JpaRepository<ClinicRoom, Long> {

    Optional<ClinicRoom> findByRoomId_RoomId(String roomId);

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);
}