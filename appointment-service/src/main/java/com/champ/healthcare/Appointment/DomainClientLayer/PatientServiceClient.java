package com.champ.healthcare.Appointment.DomainClientLayer;

public interface PatientServiceClient {

    PatientClientResponse getPatientByPatientIdentifier(String patientIdentifier);
}
