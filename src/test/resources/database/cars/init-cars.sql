TRUNCATE TABLE cars RESTART IDENTITY;

INSERT INTO cars (id, model, brand, type, inventory, daily_fee) VALUES
                                                                    (1, 'Model S',     'Tesla',        'SEDAN',      3, 100.0),
                                                                    (2, 'Civic',        'Honda',        'SEDAN',        5,  50.0),
                                                                    (3, 'Golf',        'Volkswagen',   'HATCHBACK',  4,  40.0);

ALTER TABLE cars ALTER COLUMN id RESTART WITH 4;