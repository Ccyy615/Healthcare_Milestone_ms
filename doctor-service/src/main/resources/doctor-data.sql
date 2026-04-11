-- //////////////////////////////////////////
-- Data for doctors
-- Only one doctor for the clinic
-- //////////////////////////////////////////

INSERT INTO doctors(
    id, doctor_id, first_name, last_name, is_active, is_valid,
    work_zone_city, work_zone_province
) VALUES
    (1, 'e1f2a3b4-c5d6-47e8-9f01-23456789abcd', 'John', 'Smith', TRUE, TRUE, 'Montreal', 'Quebec');

-- IMPORTANT:
-- doctor_specialities.doctor_id now references doctors.id (numeric PK), not doctors.doctor_id (UUID string)
INSERT INTO doctor_specialities(
    doctor_id, speciality_name, proficiency_level
) VALUES
    (1, 'General Practice', 3);

-- IMPORTANT:
-- doctor_license.doctor_id now references doctors.id (numeric PK), not doctors.doctor_id (UUID string)
INSERT INTO doctor_license(
    license_id, license_name, doctor_id, status, performed_date, expiry_date
) VALUES
    (24001, 'General Practice License', 1, 0, '2022-01-15 00:00:00', '2027-01-15 00:00:00');
