package com.champ.healthcare.ClinicRoom.DataAccessLayer;

import com.champ.healthcare.ClinicRoom.Domain.ClinicRoom;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomIdentifier;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClinicRoomMongoDataLoader implements ApplicationRunner {

    private final ClinicRoomRepository clinicRoomRepository;
    private final MongoOperations mongoOperations;

    @Override
    public void run(ApplicationArguments args) {
        if (clinicRoomRepository.count() > 0) {
            return;
        }

        clinicRoomRepository.saveAll(List.of(
                buildRoom(1L, "r1a2b3c4-d5e6-47f8-9a10-111111111111", "Consultation Room A", "101",
                        ClinicRoomStatus.AVAILABLE),
                buildRoom(2L, "r2a2b3c4-d5e6-47f8-9a10-222222222222", "Consultation Room B", "102",
                        ClinicRoomStatus.AVAILABLE),
                buildRoom(3L, "r3a2b3c4-d5e6-47f8-9a10-333333333333", "Treatment Room", "201",
                        ClinicRoomStatus.OUT_OF_SERVICE)
        ));

        Query query = Query.query(Criteria.where("_id").is(SequenceGeneratorService.CLINIC_ROOM_SEQUENCE));
        Update update = new Update().set("seq", 3L);
        mongoOperations.upsert(query, update, SequenceDocument.class);

        log.info("Seeded clinic-room-service Mongo collection with starter data.");
    }

    private ClinicRoom buildRoom(
            Long id,
            String roomIdentifier,
            String roomName,
            String roomNumber,
            ClinicRoomStatus roomStatus
    ) {
        return ClinicRoom.builder()
                .id(id)
                .roomId(new ClinicRoomIdentifier(roomIdentifier))
                .roomName(roomName)
                .roomNumber(roomNumber)
                .roomStatus(roomStatus)
                .build();
    }
}
