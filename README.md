# F1 Gjettespill - Backend
F1 Gjettespill går ut på at deltakerne gjetter på sluttresultatene av Formel 1 sesongen. I tillegg gjettes det på hvem som kommer på første- og tiendeplass i hvert løp gjennom året. Deretter blir det beregnet poeng utfra hvor nærme gjetningen var det faktiske resultatet. Vinneren for året er den som har flest poeng når sesongen er over.

Dette repoet er koden for backenden til nettsiden. Koden for frontenden ligger [her](https://github.com/voiestad/f1-gjettespill-frontend).

## Oversikt
1. [Utvikler](#utvikler)
2. [Krav](#krav)
3. [Oppsett](#oppsett-for-første-gang)
4. [Kjøre Applikasjonen](#kjøre-applikasjonen)

## Utvikler
**Vebjørn Øiestad**

## Krav
* Java JDK 17 eller nyere
* Maven

## Oppsett for Første Gang
1. Lag en ny fil i **`/src/main/resources`** med navnet **`secret.properties`**.
2. Fyll inn følgende informasjon:
```
spring.security.oauth2.client.registration.google.client-id={id}
spring.security.oauth2.client.registration.google.client-secret={secret}
spring.datasource.username={database username}
spring.datasource.password={database password}
ntfy.user={ntfy username}
ntfy.password={ntfy password}
```

## Kjøre Applikasjonen
For å starte applikasjonen, kjør følgende kommando:
```
mvn spring-boot:run
```

## Docker Compose
Legg til linje i `secret.properties`:
```
spring.datasource.url=jdbc:postgresql://database:5432/f1
```
Legg til linjer i `.env`:
```
POSTGRES_USER={database username}
POSTGRES_PASSWORD={database password}
POSTGRES_DB=f1
```
Kjøre:
```
docker compose up
```
