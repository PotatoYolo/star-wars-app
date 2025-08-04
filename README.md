# Star Wars Web Application

This is a full-stack web application that displays data from SWAPI, using:

- Backend: Java 21 + Spring Boot
- Frontend: Angular
- Deployed with Docker Compose on port 6969

## Features

- People and Planets listed in separate tables
- Pagination (15 items per page)
- Search by name (case-insensitive, partial match)
- Sort by name and created (asc/desc)
- Sorting logic implemented with Open/Closed principle in the backend

## How to run with Docker

1. Clone this repository:

git clone https://github.com/your-user/star-wars-app.git  
cd star-wars-app

2. Build and start everything:

docker-compose up --build

3. Access the app:

Frontend: http://localhost:6969  
Backend: http://localhost:8080/api

## How to run locally (without Docker)

### Backend

cd starwars-api  
./mvnw spring-boot:run  
(or run BackendApplication.java from your IDE)

### Frontend

cd starwars-ui  
npm install  
ng serve

App will be available at: http://localhost:4200

## Environment config in Angular

File: src/environments/environment.ts (for local development)

export const environment = {  
production: false,  
apiUrl: 'http://localhost:8080/api'  
};

File: src/environments/environment.prod.ts (for Docker)

export const environment = {  
production: true,  
apiUrl: 'http://backend:8080/api'  
};

Angular will automatically switch between these depending on build mode.

## Project structure

star-wars-app/  
├── docker-compose.yml  
├── README.md  
├── starwars-api/        (Spring Boot backend)  
│   └── Dockerfile  
├── starwars-ui/         (Angular frontend)  
│   ├── Dockerfile  
│   └── src/environments/  
│       ├── environment.ts  
│       └── environment.prod.ts

## Clean code and architecture

- Backend follows SOLID principles
- Sorting logic is extensible (Open/Closed principle)
- Angular and Spring Boot are cleanly separated
- Docker networking used to connect frontend and backend (via service name "backend")

## Technologies used

- Java 21
- Spring Boot 3.5
- Angular 17+
- Docker & Docker Compose
