package com.champ.healthcare.Appointment.DomainClientLayer;

public interface DoctorServiceClient {

    DoctorClientResponse getDoctorByDoctorId(String doctorId);
}
