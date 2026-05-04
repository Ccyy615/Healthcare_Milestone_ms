package com.champ.healthcare.ApiGateway.utilities;

public class DownstreamServiceException extends RuntimeException {

    public DownstreamServiceException(String message) {
        super(message);
    }
}
