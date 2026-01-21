
-- 1. Thêm sân bay nếu chưa có
INSERT IGNORE INTO airports (airport_code, airport_name, city, country) VALUES 
('HAN', 'Sân bay Quốc tế Nội Bài', 'Hà Nội', 'Việt Nam'),
('SGN', 'Sân bay Quốc tế Tân Sơn Nhất', 'Hồ Chí Minh', 'Việt Nam');

-- 2. Thêm các chuyến bay đi từ Hà Nội (HAN) đến Sài Gòn (SGN) ngày 28/01
-- Tìm ID của sân bay HAN và SGN để insert chính xác
SET @han_id = (SELECT airport_id FROM airports WHERE airport_code = 'HAN' LIMIT 1);
SET @sgn_id = (SELECT airport_id FROM airports WHERE airport_code = 'SGN' LIMIT 1);

INSERT INTO flights (flight_number, airline, departure_airport_id, arrival_airport_id, departure_time, arrival_time, seat_capacity, price) VALUES 
('VN123', 'Vietnam Airlines', @han_id, @sgn_id, '2026-01-28 08:00:00', '2026-01-28 10:15:00', 180, 1550000),
('VJ456', 'Vietjet Air', @han_id, @sgn_id, '2026-01-28 14:30:00', '2026-01-28 16:45:00', 200, 1200000);

-- 3. Thêm chuyến bay khứ hồi từ Sài Gòn (SGN) về Hà Nội (HAN) ngày 30/01
INSERT INTO flights (flight_number, airline, departure_airport_id, arrival_airport_id, departure_time, arrival_time, seat_capacity, price) VALUES 
('VN128', 'Vietnam Airlines', @sgn_id, @han_id, '2026-01-30 19:00:00', '2026-01-30 21:15:00', 180, 1600000);