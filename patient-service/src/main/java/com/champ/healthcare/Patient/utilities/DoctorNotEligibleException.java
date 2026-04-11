package com.champ.healthcare.Patient.utilities;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DoctorNotEligibleException extends RuntimeException {
    public DoctorNotEligibleException(String message) {
        super(message);
    }
}