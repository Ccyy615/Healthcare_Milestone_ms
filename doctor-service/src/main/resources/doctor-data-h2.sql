INSERT INTO doctors(
    id, doctor_id, first_name, last_name, is_active, is_valid,
    work_zone_city, work_zone_province
) VALUES
    (1, 'e1f2a3b4-c5d6-47e8-9f01-23456789abcd', 'John', 'Smith', FALSE, FALSE, 'Montreal', 'Quebec');

ALTER TABLE doctors ALTER COLUMN id RESTART WITH 2;
