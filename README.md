# Bank REST Service

Банковский REST-сервис на Spring Boot 3 с JWT-аутентификацией, управлением картами и переводами.

## Технологии

- **Java 23**, **Spring Boot 3.3.2**
- **Spring Security** — JWT (JJWT 0.12.5), BCrypt
- **Spring Data JPA** — Hibernate, PostgreSQL 16
- **Liquibase** — миграции базы данных
- **MapStruct 1.5.5** — маппинг DTO
- **Lombok**, **Jakarta Validation**
- **Swagger/OpenAPI** (springdoc 2.6.0) — документация API
- **Docker**, **Docker Compose** — контейнеризация
- **AES** — шифрование номеров карт
- **JUnit 5**, **Mockito** — тестирование

## Доступ
### Ресурс	URL

Приложение	http://localhost:8080

Swagger UI	http://localhost:8080/swagger-ui/index.html

OpenAPI JSON    http://localhost:8080/v3/api-docs

### Учётная запись администратора
Поле	Значение

Логин	admin

Пароль	Admin@123

Имя в системе	AlphaAdmin

Администратор создаётся автоматически при первом запуске через миграцию Liquibase.

## API
### Аутентификация (открытый доступ)
Метод	Эндпоинт	Описание

POST	/api/auth/register	Регистрация нового пользователя

POST	/api/auth/login	Вход в систему

POST	/api/auth/refresh	Обновление токенов

### Карты пользователя (USER, ADMIN)
Метод	Эндпоинт	Описание

GET	/api/user/cards	Свои карты (с пагинацией)

GET	/api/user/cards/{id}/balance	Баланс карты

POST	/api/user/cards/transfer	Перевод между своими картами

POST	/api/user/cards/{id}/request-block	Запросить блокировку

### Управление пользователями (USER, ADMIN)
Метод	Эндпоинт	Описание
PUT	/api/user/users/change-login	Сменить логин

PUT	/api/user/users/change-password	Сменить пароль

### Администрирование (только ADMIN)
Метод	Эндпоинт	Описание
GET	/api/admin/cards	Все карты

POST	/api/admin/cards?login={login}	Создать карту

PUT	/api/admin/cards/{id}	Обновить карту

DELETE	/api/admin/cards/{id}	Удалить карту

PUT	/api/admin/cards/{id}/status?status={status}	Сменить статус

PUT	/api/admin/cards/{id}/deposit?amount={amount}	Пополнить баланс

PUT	/api/admin/cards/{id}/approve-block	Подтвердить блокировку

PUT	/api/admin/cards/{id}/reject-block	Отклонить блокировку

GET	/api/admin/cards/by-status?status={status}	Карты по статусу

GET	/api/admin/users/{id}	Пользователь по ID

GET	/api/admin/users/by-login/{login}	Пользователь по логину

PUT	/api/admin/users/{id}	Обновить пользователя

DELETE	/api/admin/users/{id}	Удалить пользователя

PUT	/api/admin/users/{id}/role	Сменить роль на ADMIN

### Статусы карт
ACTIVE — активна

BLOCKED — заблокирована

EXPIRED — истёк срок

PENDING_BLOCK — ожидает подтверждения блокировки

### Безопасность
JWT: access-токен (10 мин) + refresh-токен (1 час).

Пароли: BCrypt (10 раундов).

Номера карт: AES-128 с ключом из application.yml.

Роли: ROLE_USER, ROLE_ADMIN.

### Переменные окружения

Переменная	По умолчанию	Описание

JWT_ACCESS_SECRET	mySuperSecretAccessKeyForJWT123!!	Ключ для access-токенов

JWT_REFRESH_SECRET	mySuperSecretRefreshKeyForJWT789!!	Ключ для refresh-токенов

CARD_ENCRYPTION_KEY	trainingKey12345	Ключ шифрования номеров карт
