INSERT INTO location (region_name, latitude, longitude, nx, ny, created_at, updated_at)
VALUES ('서울대학교', 37.49, 126.93, 59, 125, NOW(6), NOW(6));

INSERT INTO weather_forecast (region_code, pop, pty, pcp, sky, wsd, base_date_and_time, fcst_date_and_time, fetched_at)
VALUES (SELECT id FROM location WHERE region_name = '서울대학교', 10, 0, '1mm 미만', 1, 2.2, '2025-12-24 00:00:00', '2025-12-24 01:00:00', NOW(6)),
    (SELECT id FROM location WHERE region_name = '서울대학교', 0, 0, '1mm 미만', 3, 2.3, '2025-12-24 00:00:00', '2025-12-24 02:00:00', NOW(6));