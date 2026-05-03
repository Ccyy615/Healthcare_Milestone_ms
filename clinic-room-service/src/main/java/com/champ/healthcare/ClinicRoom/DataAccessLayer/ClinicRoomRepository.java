package com.champ.healthcare.ClinicRoom.DataAccessLayer;

import com.champ.healthcare.ClinicRoom.Domain.ClinicRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ClinicRoomRepository extends MongoRepository<ClinicRoom, String> {

    Optional<ClinicRoom> findById(Long id);

    Optional<ClinicRoom> findByRoomId_RoomId(String roomId);

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);
}
