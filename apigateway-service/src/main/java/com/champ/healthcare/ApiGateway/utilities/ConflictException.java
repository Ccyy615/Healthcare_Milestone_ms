package com.champ.healthcare.ApiGateway.utilities;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
