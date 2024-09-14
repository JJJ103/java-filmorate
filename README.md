## Диаграмма базы данных

![Диаграмма базы данных](./QuickDBD-export.png)

## Пояснение к схеме базы данных

Диаграмма базы данных представляет структуру хранения пользователей, фильмов, их жанров, рейтингов и отношений дружбы между пользователями. Основные сущности и их связи спроектированы в третьей нормальной форме (3NF), чтобы избежать дублирования данных и обеспечить целостность информации.

### Основные операции:

- **Получение всех фильмов:**
  ```sql
  SELECT * FROM films;

- **Получение пользователя по ID:**
  ```sql
  SELECT * FROM users WHERE user_id = ?;

- **Добавление нового друга:**
  ```sql
  INSERT INTO friendships (user_id, friend_id, status)
  VALUES (?, ?, FALSE);

- **Получение списка общих друзей двух пользователей:**
  ```sql
  SELECT u.*
  FROM users u
  JOIN friendships f1 ON u.user_id = f1.friend_id
  JOIN friendships f2 ON u.user_id = f2.friend_id
  WHERE f1.user_id = ? AND f2.user_id = ? AND f1.status = TRUE AND f2.status = TRUE;

- **Получение топ N популярных фильмов:**
  ```sql
  SELECT f.*, COUNT(l.user_id) AS likes_count
  FROM films f
  LEFT JOIN likes l ON f.film_id = l.film_id
  GROUP BY f.film_id
  ORDER BY likes_count DESC
  LIMIT ?;