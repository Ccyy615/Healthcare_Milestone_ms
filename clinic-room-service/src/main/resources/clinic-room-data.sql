-- //////////////////////////////////////////
-- Data for clinic rooms
-- Replaces old clinic_schedules data
-- //////////////////////////////////////////

INSERT INTO clinic_rooms(
    id, room_id, room_name, room_number, room_status
) VALUES
      (1, 'r1a2b3c4-d5e6-47f8-9a10-111111111111', 'Consultation Room A', '101', 'AVAILABLE'),
      (2, 'r2a2b3c4-d5e6-47f8-9a10-222222222222', 'Consultation Room B', '102', 'AVAILABLE'),
      (3, 'r3a2b3c4-d5e6-47f8-9a10-333333333333', 'Treatment Room', '201', 'OUT_OF_SERVICE');
