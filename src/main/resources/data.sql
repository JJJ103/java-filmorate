-- Заполнение пользователей
INSERT INTO users (email, login, name, birthday) VALUES
('user1@example.com', 'user1', 'User One', '1990-01-01'),
('user2@example.com', 'user2', 'User Two', '1992-02-02'),
('user3@example.com', 'user3', 'User Three', '1993-03-03');

-- Заполнение фильмов
INSERT INTO films (name, description, release_date, duration, rating_id) VALUES
('Film One', 'Description One', '2020-01-01', 120, 1),
('Film Two', 'Description Two', '2021-02-02', 130, 2);

-- Заполнение жанров
INSERT INTO film_genres (film_id, genre_id) VALUES
(1, 1),
(1, 2),
(2, 3);

-- Заполнение лайков
INSERT INTO likes (film_id, user_id) VALUES
(1, 1),
(2, 2);