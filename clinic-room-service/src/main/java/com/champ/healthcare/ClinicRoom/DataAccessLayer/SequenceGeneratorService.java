package com.champ.healthcare.ClinicRoom.DataAccessLayer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    public static final String CLINIC_ROOM_SEQUENCE = "clinic_rooms_sequence";

    private final MongoOperations mongoOperations;

    public long getNextSequence(String sequenceName) {
        Query query = Query.query(Criteria.where("_id").is(sequenceName));
        Update update = new Update().inc("seq", 1);

        SequenceDocument counter = mongoOperations.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                SequenceDocument.class
        );

        if (counter == null) {
            throw new IllegalStateException("Unable to generate Mongo sequence for: " + sequenceName);
        }

        return counter.getSeq();
    }
}
