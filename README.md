# Отчет Spring Security Example

## Описание проекта

Приложение является примером реализация аутентификации и авторизации с использованием Spring Security и JWT.

## Инструкции по запуску

### Клонирование репозитория

```bash
git clone https://github.com/DeaDog911/Spring-Security-Homework.git
```

### База данных
Проект использует базу данных для хранения данных о пользователях и заказах. По умолчанию используется база данных PostgreSQL.
Для настройки подключения к базе данных, переименуйте файл `application.properties.origin` из директории `src/main/resources/` в `application.properties` и укажите в нем следующие параметры:

```
spring.datasource.url=r2dbc:ваша_база_данных_url
spring.datasource.username=ваш_пользователь
spring.datasource.password=ваш_пароль
```

Для хранения refresh токенов в приложении используется Redis.

### Настройка JWT-авторизации

- `jwt.expiration.access`: Время жизни access токена в формате Duration. Например (PT1H - один час)
- `jwt.expiration.refresh`: Время жизни refresh токена в формате Duration.
- `jwt.secret.access`: Секретный ключ для подписи access токенов
- `jwt.secret.refresh`: Секретный ключ для подписи refresh токенов

### Сборка и запуск приложения
Перейдите в директорию проекта и выполните следующие команды:

Запуск Docker контейнера
```bash
docker-compose build
docker-compose up -d 
```

Запуск приложения
```bash
mvn install
mvn spring-boot:run
```

### Тестирование
Для запуска тестов выполните команду:

```bash
mvn test
```

### Документация API

После запуска приложения документация API доступна по адресу
http://localhost:8080/webjars/swagger-ui/index.html