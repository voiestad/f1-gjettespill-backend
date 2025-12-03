# F1 Gjettespill - Backend
F1 Gjettespill (F1 Guessing Game) is a game where the participants guess the end results of the Formula 1 season. 
In addition, the participants also guess on who gets pole, first place and tenth place in each grand prix. 
Participants can create leagues so that friends can compete against each other.
Points are calculated based on how close the guess was to the actual result.
The winner for the year is the one that has the most points when the season is over.
There is both a global leaderboard and a leaderboard for each league.

This is the repository for the backend code to the website. The code for the frontend is [here](https://github.com/voiestad/f1-gjettespill-frontend).

## Overview
1. [Developer](#developer)
2. [Requirements](#requirements)
3. [Setting up](#first-time-set-up)
4. [Running Application](#running-application)

## Developer
**Vebjørn Øiestad**

## Requirements
* Java JDK 17
* Maven
* PostgreSQL

Can also be ran fully in Docker.

## First Time Set Up
1. Create a file in `/src/main/resources` named `secret.properties`.
2. Add the following information:
```
spring.security.oauth2.client.registration.google.client-id={id}
spring.security.oauth2.client.registration.google.client-secret={secret}
spring.datasource.username={database username}
spring.datasource.password={database password}
ntfy.user={ntfy username}
ntfy.password={ntfy password}
```

You also need to set up a PostgreSQL database named f1 at port 5432. The schema is available in 
[schema.sql](schema.sql).

## Running Application
To start the application, run the following command:
```
mvn spring-boot:run
```

## Docker Compose
Add line in `secret.properties`:
```
spring.datasource.url=jdbc:postgresql://database:5432/f1
```
Add lines in `.env`:
```
POSTGRES_USER={database username}
POSTGRES_PASSWORD={database password}
POSTGRES_DB=f1
```
Run:
```
docker compose up
```

## Disclaimer
F1 Gjettespill is in no way affiliated with Formula 1 and the trademark F1 belongs to Formula One Group.