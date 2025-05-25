# Проект Библиотека

## Описание

Этот проект состоит из трех микросервисов для управления библиотечной системой:

- auth-service: Сервис аутентификации и авторизации  
- book-storage-service: Сервис управления хранением книг  
- book-tracker-service: Сервис отслеживания использования книг  

## Требования

- Docker и Docker Compose  

## Настройка Google OAuth

Для корректной работы авторизации через Google необходимо выполнить следующие шаги:

1. Создайте новый проект в [Google Cloud Console](https://console.cloud.google.com/).

2. В разделе "APIs & Services" выберите "Credentials" и создайте OAuth 2.0 Client ID для веб-приложения.

3. В настройках OAuth укажите URI перенаправления (redirect URI) как:http://localhost:8083/login/oauth2/code/google

4. Полученные Client ID и Client Secret необходимо заменить в переменных окружения `GOOGLE_CLIENT_ID` и `GOOGLE_CLIENT_SECRET` в файле `docker-compose.yml`.


## Запуск проекта через Docker Compose (рекомендуется)

Для запуска всех сервисов, базы данных MySQL, Kafka и Zookeeper используйте команду: `docker-compose up --build`

- MySQL будет доступен на порту `13306`  
- book-storage-service на порту `8081`  
- book-tracker-service на порту `8082`  
- auth-service на порту `8083`  

Для остановки всех сервисов используйте: `docker-compose down`


### Авторизация через Google

Для входа в систему используйте Google OAuth по адресу: http://localhost:8083/login

После успешной авторизации через Google вы получите JWT токен, который используется для доступа к другим сервисам.
Пример:
{"token": "ваш_токен"}

## Конфигурация

- База данных: MySQL в Docker, имя базы `library_db`, пользователь `root`, пароль `1111`  
- Kafka и Zookeeper также запускаются в Docker через docker-compose  
- JWT и учетные данные Google OAuth задаются через переменные окружения в `docker-compose.yml`  

## Коллекция Postman

Прилагается коллекция с примерами запросов Postman:

`Library.postman_collection.json`

Расположена в корне проекта. Импортируйте её в Postman для быстрого доступа к основным запросам.

### Ввод токена

Для авторизации в Postman это можно сделать на вкладке `Authorization`:

- Тип: Bearer Token  
- Токен: `<ваш_токен>`

## Примечания

- Убедитесь, что Docker запущен перед использованием Docker Compose  
- Порты можно изменить в файле `docker-compose.yml` при необходимости
