# Star Wars Web Application

This is a full-stack web application that displays data from [SWAPI](httpsswapi.dev), using

- 🧠 Backend Java 21 + Spring Boot
- 🌐 Frontend Angular
- 🐳 Deployed with Docker Compose on port 6969

---

## ✅ Features

- People and Planets listed in separate tables
- Pagination (15 items per page)
- Search by name (case-insensitive, partial match)
- Sort by `name` and `created` (ascdesc)
- Sorting logic implemented with OpenClosed principle in the backend

---

## 🐳 How to run with Docker

### 1. Clone this repository

```bash
git clone httpsgithub.comyour-userstar-wars-app.git
cd star-wars-app
2. Build and start everything
bash
Copiar
Editar
docker-compose up --build
3. Access the app
Frontend httplocalhost6969

Backend httplocalhost8080api

🧪 How to run locally (without Docker)
Backend
bash
Copiar
Editar
cd starwars-api
.mvnw spring-bootrun
Or run BackendApplication.java from your IDE.

Frontend
bash
Copiar
Editar
cd starwars-ui
npm install
ng serve
App will be available at httplocalhost4200

⚙️ Environment config in Angular
srcenvironmentsenvironment.ts → for local dev

ts
Copiar
Editar
export const environment = {
  production false,
  apiUrl 'httplocalhost8080api'
};
srcenvironmentsenvironment.prod.ts → for Docker

ts
Copiar
Editar
export const environment = {
  production true,
  apiUrl 'httpbackend8080api'
};
These are automatically switched depending on build mode.

🗂 Project structure
bash
Copiar
Editar
star-wars-app
├── docker-compose.yml
├── README.md
├── starwars-api        # Backend Spring Boot
│   └── Dockerfile
├── starwars-ui         # Frontend Angular
│   ├── Dockerfile
│   └── srcenvironments
│       ├── environment.ts
│       └── environment.prod.ts
🧹 Clean code & principles
Backend designed with SOLID principles

Sorting is OpenClosed compliant

Angular and Spring Boot are cleanly separated

Uses Docker networking (backend8080) to connect services

🛠 Built with
Java 21

Spring Boot 3.5

Angular 17+

Docker & Docker Compose