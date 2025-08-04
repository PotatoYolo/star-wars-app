# Star Wars CRUD Service

Full-stack project that stores data from **SWAPI** in PostgreSQL and exposes it through a Spring Boot API.  
An Angular front-end consumes that API and lets you list, search and create **Characters** (other entities are read-only for now).

---

## Table of Contents

1. Requirements
2. Quick start (Docker)
3. Manual start (local dev)
4. API reference
5. Front-end usage
6. Project structure
7. NPM / Maven scripts
8. Roadmap
9. License

---

## 1  Requirements

| Tool          | Version |
|---------------|---------|
| Docker / compose | 24+ |
| Java | 21 |
| Node / NPM | 18 / 9 |
| Angular CLI | 17 |

---

## 2 Quick start (Docker)

git clone https://github.com/your-user/star-wars-app.git

cd star-wars-app

docker-compose up --build

Frontend http://localhost:6969

Backend http://localhost:8080/api

## 3 Manual start (local dev)

# backend
cd starwars-api
./mvnw spring-boot:run

# frontend
cd ../starwars-ui
npm install
ng serve

Frontend at http://localhost:4200

## 4 API reference

All list endpoints accept `?page=`, `&size=`, `?search=`, `?sort=field,asc|desc`.

| Resource   | List / Create                         | Detail / Update / Delete                                                  |
|------------|---------------------------------------|---------------------------------------------------------------------------|
| Characters | `GET /characters`  `POST /characters` | `GET /characters/{id}`  `PUT /characters/{id}`  `DELETE /characters/{id}` |
| Planets    | `GET /planets`                        |                                                                           |
| Species    | `GET /species`                        |                                                                           |
| Vehicles   | `GET /vehicles`                       |                                                                           |
| Starships  | `GET /starships`                      |                                                                           |
| Films      | `GET /films`                          |                                                                           |

Example  


```json
{
  "content": [
    { "id": 84, "name": "Luke Skywalker", /* … */ }
  ],
  "page": 2,
  "size": 15,
  "totalPages": 6,
  "totalElements": 84
}
```

## 5 Front-end usage
Pagination (15 rows) with ellipsis 1 2 … 6.

Search box filters by name (case-insensitive, debounced 300 ms).

Sort by Name or Created (click header, uses keyboard Enter/Space).

Create / Edit forms use Angular Reactive Forms with validation.

Delete uses SweetAlert2 confirmation dialog.
```
// dev
export const environment = { production:false, apiUrl:'http://localhost:8080/api' };

// prod (Docker)
export const environment = { production:true,  apiUrl:'http://backend:8080/api' };
```
## 6 Project structure

```text
star-wars-app
├─ docker-compose.yml
├─ starwars-api
│  ├─ src/main/java/...   # Spring Boot code
│  └─ Dockerfile
├─ starwars-ui
│  ├─ src/app/
│  │   ├─ character/        # full CRUD
│  │   ├─ planets/          # read-only table
│  │   ├─ …                 # other entities
│  │   └─ shared/
│  │        confirm.ts      # SweetAlert helper
│  │        pagination.util.ts
│  └─ Dockerfile
└─ README.md
```


## 7 Scripts
| Location     | Command              | Purpose                     |
|--------------|----------------------|-----------------------------|
| root         | `docker-compose up`  | build & run full stack      |
| starwars-ui  | `npm start`          | dev server                  |
| starwars-ui  | `npm run build`      | production build            |
| starwars-ui  | `npm run lint`       | ESLint + Sonar              |
| starwars-ui  | `npm run test`       | unit tests (Karma)          |
| starwars-api | `./mvnw test`        | backend tests               |



## 8 Roadmap

PUT/DELETE for all entities

OAuth 2 login (Keycloak)

Caching with Redis

Kubernetes manifests

