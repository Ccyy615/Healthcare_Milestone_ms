package com.champ.healthcare.Appointment.Domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class TimeSlot {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public void validate() {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time are required.");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
    }

    public boolean overlaps(TimeSlot other) {
        return this.startTime.isBefore(other.endTime)
                && this.endTime.isAfter(other.startTime);
    }
}