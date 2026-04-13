package com.champ.healthcare.ClinicRoom.utilities;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRoomNumberException extends RuntimeException {

    public DuplicateRoomNumberException(String message) {
        super(message);
    }
}
