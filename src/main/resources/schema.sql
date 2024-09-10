-- Таблица Users
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    login VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    birthday DATE
);

-- Таблица Films
CREATE TABLE IF NOT EXISTS films (
    film_id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    release_date DATE NOT NULL,
    duration INT NOT NULL
);

-- Таблица Friendships
CREATE TABLE IF NOT EXISTS friendships (
    user_id BIGINT,
    friend_id BIGINT,
    status VARCHAR(50),
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (friend_id) REFERENCES users(user_id)
);

-- Таблица Likes
CREATE TABLE IF NOT EXISTS likes (
    film_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Таблица Genres
CREATE TABLE IF NOT EXISTS genres (
    genre_id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Таблица FilmGenres
CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT,
    genre_id BIGINT,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id),
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);

-- Таблица Ratings
CREATE TABLE IF NOT EXISTS ratings (
    rating_id BIGINT PRIMARY KEY,
    MPA VARCHAR(5) NOT NULL
);

-- Таблица FilmRatings
CREATE TABLE IF NOT EXISTS film_ratings (
    film_id BIGINT,
    rating_id BIGINT,
    PRIMARY KEY (film_id, rating_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id),
    FOREIGN KEY (rating_id) REFERENCES ratings(rating_id)
);
