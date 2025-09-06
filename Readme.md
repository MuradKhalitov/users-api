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
       mvn spring-boot:run -Dspring-boot.run.profiles=dev
       ```  
       или
       ```bash
       SPRING_PROFILES_ACTIVE=dev java -jar target/user-api-0.0.1-SNAPSHOT.jar
       ```

- **В Docker (профиль docker)**
    1. Собрать jar:
       ```bash
       mvn -DskipTests package
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
