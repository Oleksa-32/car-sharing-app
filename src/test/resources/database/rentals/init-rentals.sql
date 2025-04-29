TRUNCATE TABLE rentals RESTART IDENTITY;

INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id) VALUES
                                                                                            (1, '2024-01-01 10:00:00', '2024-01-10 10:00:00', NULL, 1, 1),
                                                                                            (2, '2024-01-02 10:00:00', '2024-01-12 10:00:00', '2024-01-11 10:00:00', 2, 1),
                                                                                            (3, '2024-01-03 10:00:00', '2024-01-13 10:00:00', NULL, 3, 2);

ALTER TABLE rentals ALTER COLUMN id RESTART WITH 4;