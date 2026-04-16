# 🐾 Buchi Pet Finder — Backend API

A Spring Boot REST API for the Buchi pet adoption platform. Search for pets from both a local database and the [Petfinder API](https://www.petfinder.com/developers/), then request to adopt them.

---

## Quick Start

```bash
# 1. Clone the repo
git clone https://github.com/deve1070/buchi-pet-finder.git
cd buchi-pet-finder

# 2. Set up environment variables
cp .env.example .env
# Edit .env and add your Petfinder API credentials

# 3. Start everything with a single command
docker-compose up
```

The API will be available at **http://localhost:8080**

Swagger UI (API docs): **http://localhost:8080/swagger-ui.html**

---

## Tech Stack

| Layer            | Technology                     |
| ---------------- | ------------------------------ |
| Language         | Java 17                        |
| Framework        | Spring Boot 3.3.5              |
| Database         | PostgreSQL 16                  |
| ORM              | Spring Data JPA / Hibernate    |
| Migrations       | Flyway                         |
| HTTP Client      | Spring WebFlux (WebClient)     |
| API Docs         | SpringDoc OpenAPI / Swagger UI |
| Testing          | JUnit 5 + Mockito              |
| Containerization | Docker + Docker Compose        |

---

## API Endpoints

| Method | Endpoint                        | Description                          |
| ------ | ------------------------------- | ------------------------------------ |
| POST   | `/api/v1/create_pet`            | Create a pet with optional photos    |
| GET    | `/api/v1/get_pets`              | Search pets (local DB + Petfinder)   |
| POST   | `/api/v1/add_customer`          | Add or retrieve a customer by phone  |
| POST   | `/api/v1/adopt`                 | Submit an adoption request           |
| GET    | `/api/v1/get_adoption_requests` | List adoption requests by date range |
| POST   | `/api/v1/generate_report`       | adoption stats report                |

---

## 📋 Endpoint Examples

### POST `/api/v1/create_pet`

```bash
curl -X POST http://localhost:8080/api/v1/create_pet \
  -F "type=Dog" \
  -F "gender=male" \
  -F "size=small" \
  -F "age=baby" \
  -F "good_with_children=true" \
  -F "name=Buddy" \
  -F "photos=@/path/to/photo.jpg"
```

### GET `/api/v1/get_pets`

```bash
# All filters optional except limit. Supports multi-value for type, gender, size, age
curl "http://localhost:8080/api/v1/get_pets?type=Dog&type=Cat&limit=5"
```

### POST `/api/v1/add_customer`

```bash
# If phone exists, returns existing customer_id (no duplicate created)
curl -X POST http://localhost:8080/api/v1/add_customer \
  -H "Content-Type: application/json" \
  -d '{"name": "Abebe Kebede", "phone": "0912345678"}'
```

### POST `/api/v1/adopt`

```bash
curl -X POST http://localhost:8080/api/v1/adopt \
  -H "Content-Type: application/json" \
  -d '{"customerId": 1, "petId": 1}'
```

### GET `/api/v1/get_adoption_requests`

```bash
curl "http://localhost:8080/api/v1/get_adoption_requests?from_date=2024-01-01&to_date=2027-12-31"
```

### POST `/api/v1/generate_report`

```bash
curl -X POST http://localhost:8080/api/v1/generate_report \
  -H "Content-Type: application/json" \
  -d '{"fromDate": "2024-01-01", "toDate": "2027-12-31"}'
```

---

## Database Schema

```
pets              — id, type, gender, size, age, good_with_children, name, status
pet_photos        — id, pet_id (FK), file_path, url, is_primary
customers         — id, name, phone (UNIQUE), email
adoption_requests — id, customer_id (FK), pet_id (FK), status, requested_at
                    UNIQUE(customer_id, pet_id)
```

---

## Running Tests

```bash
# Run all tests (uses H2 in-memory DB, no PostgreSQL needed)
./mvnw test
```

---

## Petfinder API Setup

1. Go to **https://www.petfinder.com/developers/**
2. Sign up for a free account
3. Generate API Key + Secret
4. Add to your `.env` file:

```
PETFINDER_API_KEY=your_key
PETFINDER_API_SECRET=your_secret
```

> If credentials are not set, the app gracefully falls back to local results only.

---

## Git History

```
chore:  init Spring Boot project with dependencies
feat:   database schema - entities and Flyway migration
feat:   create_pet endpoint with photo upload
feat:   petfinder api client with token caching
feat:   get_pets endpoint - local first, petfinder fills remainder
feat:   add_customer endpoint with upsert logic
feat:   adopt endpoint with validation
feat:   get_adoption_requests endpoint with date range filter
feat:   generate_report bonus endpoint
test:   unit tests for all service and controller layers
chore:  dockerize - Dockerfile + docker-compose
```
