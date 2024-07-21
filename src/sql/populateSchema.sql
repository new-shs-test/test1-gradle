INSERT INTO users (password, nickname, email)
VALUES
    ('password123', 'user1', 'user1@example.com'),
    ('password456', 'user2', 'user2@example.com'),
    ('password789', 'user3', 'user3@example.com'),
    ('Marsul2023', 'marsul', 'marsul@example.com');

-- Insert data into the "token_info" table
INSERT INTO token_info (token, player_id, created_at, last_used_at)
VALUES
    ('YzqJb7xUZkDjFe9SGnB49KCbKgqCGeDN', 1, 1634800000, 1634801000),
    ('4HvEpZbfvKXyFZPjfBgeFmJ6W56kC53w', 2, 1634802000, 1634803000),
    ('W5ckq6t8bY9JzSDj3n4bE9JkqZCfvJGF', 3, 1634804000, 1634805000),
    ('BnE9Hv4CGKj5CkYPf3qbRjZ5JWf6ZfbX', 4, 1634804000, 1634805000);

-- Insert data into the "lobby" table
INSERT INTO lobby (player_A, player_B, grid, opening_rules, variant)
VALUES
    (1, 2, 15, 'Standard', 'Standard'),
    (1, 2, 15, 'Standard', 'Standard'),
    (1, 3, 15, 'Standard', 'Standard'),
    (4, 1, 15, 'Standard', 'Standard'),
    (4, 2, 15, 'Standard', 'Standard');


-- Insert data into the "match" table
INSERT INTO match (match_id, start_time, end_time)
VALUES
    (1, 1634800000, 1634801000),
    (2, 1634802000, 1634803000),
    (3, 1634804000, 1634805000),
    (4, 1634802000, 1634803000),
    (5, 1634802000, 1634803000);


-- Insert data into the "outcome" table
INSERT INTO outcome (match_id, winner, a_points, b_points, duration)
VALUES
    (1, 1, 20, 10, 3600),
    (2, 1, 20, 10, 2700),
    (3, 1, 20, 10, 1800),
    (4, 4, 20, 10, 2700),
    (5, 4, 20, 10, 2700);


-- Insert data into the "play" table
INSERT INTO play (player_id, match_id, line, col)
VALUES
    (1, 1, 1, 1),
    (2, 1, 1, 2),
    (1, 1, 2, 1),
    (2, 1, 2, 2);


