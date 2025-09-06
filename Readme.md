# User API

REST API для управления пользователями.  
Java 17 + Spring Boot 3 + PostgreSQL + Liquibase + Redis Cache.

---

## 🔧 Стек технологий
- Spring Boot 3 (Web, Data JPA, Validation, Cache)
- PostgreSQL 17
- Liquibase (миграции схемы `user_schema`)
- Redis 7 (кэширование с JSON-сериализацией)
- MapStruct (маппинг DTO ↔ Entity)
- JUnit 5 + MockMvc + Testcontainers
- Docker / docker-compose

---

## 🚀 Запуск приложения

- **Локально (профиль dev)**
    1. Поднять Postgres и Redis:
       ```bash
       docker-compose up -d postgres redis
       ```  
    2. Запустить приложение:
       ```bash
       .\mvnw clean package -DskipTests
       ```
       ```bash
       java -jar target\user-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
       ```

- **В Docker (профиль docker)**
    1. Собрать jar:
       ```bash
       .\mvnw clean package -DskipTests
       ```  
    2. Поднять сервисы (Postgres, Redis и приложение):
       ```bash
       docker-compose up --build
       ```  

Приложение доступно: [http://localhost:8080/api](http://localhost:8080/api)

---

## 📑 API эндпоинты

### POST `/api/createNewUser`
Создать пользователя.  
Пример тела:
```json
{
  "fio": "Иван Иванов",
  "phoneNumber": "+79001234567",
  "avatar": "https://via.placeholder.com/150",
  "role": "ROLE_USER"
}
``` 

### GET `/api/users?userID={uuid}`
Получить пользователя  

### PUT `/api/userDetailsUpdate`
Обновить пользователя.

Пример тела:
```json
{
  "uuid": "user-uuid",
  "fio": "Иван Иванович",
  "phoneNumber": "+79005557788",
  "avatar": "https://via.placeholder.com/300",
  "role": "ROLE_ADMIN"
}
``` 

### DELETE `/api/users?userID={uuid}`
Удалить пользователя.

## Валидация
### fio — обязательное поле

### phoneNumber — только + и 10–15 цифр

### avatar — корректный URL

### role — обязательное поле