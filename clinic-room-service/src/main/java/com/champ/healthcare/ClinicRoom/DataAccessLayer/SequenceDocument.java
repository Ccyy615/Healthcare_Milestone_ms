package com.champ.healthcare.ClinicRoom.DataAccessLayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "database_sequences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SequenceDocument {

    @Id
    private String id;

    private long seq;
}
