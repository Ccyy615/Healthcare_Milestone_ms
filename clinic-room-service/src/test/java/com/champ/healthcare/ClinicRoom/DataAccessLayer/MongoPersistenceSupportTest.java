package com.champ.healthcare.ClinicRoom.DataAccessLayer;

import com.champ.healthcare.ClinicRoom.Domain.ClinicRoom;
import com.champ.healthcare.ClinicRoom.Domain.ClinicRoomStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MongoPersistenceSupportTest {

    @Test
    void sequenceGeneratorReturnsNextSequenceValue() {
        MongoOperations mongoOperations = mock(MongoOperations.class);
        SequenceGeneratorService sequenceGeneratorService = new SequenceGeneratorService(mongoOperations);

        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(SequenceDocument.class)
        )).thenReturn(SequenceDocument.builder().id("clinic_rooms_sequence").seq(4L).build());

        long nextValue = sequenceGeneratorService.getNextSequence(SequenceGeneratorService.CLINIC_ROOM_SEQUENCE);

        assertThat(nextValue).isEqualTo(4L);
    }

    @Test
    void sequenceGeneratorThrowsWhenMongoCounterCannotBeCreated() {
        MongoOperations mongoOperations = mock(MongoOperations.class);
        SequenceGeneratorService sequenceGeneratorService = new SequenceGeneratorService(mongoOperations);

        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(SequenceDocument.class)
        )).thenReturn(null);

        assertThatThrownBy(() -> sequenceGeneratorService.getNextSequence("missing-sequence"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to generate Mongo sequence for: missing-sequence");
    }

    @Test
    void mongoDataLoaderSeedsStarterDataAndSequence() throws Exception {
        ClinicRoomRepository clinicRoomRepository = mock(ClinicRoomRepository.class);
        MongoOperations mongoOperations = mock(MongoOperations.class);
        ClinicRoomMongoDataLoader dataLoader = new ClinicRoomMongoDataLoader(clinicRoomRepository, mongoOperations);

        when(clinicRoomRepository.count()).thenReturn(0L);

        dataLoader.run(new DefaultApplicationArguments(new String[0]));

        @SuppressWarnings("unchecked")
        var roomsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);

        verify(clinicRoomRepository).saveAll(roomsCaptor.capture());
        verify(mongoOperations).upsert(any(Query.class), any(Update.class), eq(SequenceDocument.class));

        List<ClinicRoom> rooms = roomsCaptor.getValue();
        assertThat(rooms).hasSize(3);
        assertThat(rooms.get(0).getId()).isEqualTo(1L);
        assertThat(rooms.get(2).getRoomStatus()).isEqualTo(ClinicRoomStatus.OUT_OF_SERVICE);
    }

    @Test
    void mongoDataLoaderSkipsSeedingWhenDataAlreadyExists() throws Exception {
        ClinicRoomRepository clinicRoomRepository = mock(ClinicRoomRepository.class);
        MongoOperations mongoOperations = mock(MongoOperations.class);
        ClinicRoomMongoDataLoader dataLoader = new ClinicRoomMongoDataLoader(clinicRoomRepository, mongoOperations);

        when(clinicRoomRepository.count()).thenReturn(2L);

        dataLoader.run(new DefaultApplicationArguments(new String[0]));

        verify(clinicRoomRepository, never()).saveAll(any());
        verify(mongoOperations, never()).upsert(any(Query.class), any(Update.class), eq(SequenceDocument.class));
    }
}
