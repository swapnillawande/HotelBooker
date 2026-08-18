
# HotelBooker API

> The repository now includes the Stayly React client in [`frontend/`](frontend/README.md). Run the Spring Boot API and Vite client side by side for the complete booking experience.

## Overview

HotelBooker is a Spring Boot based hotel booking backend application that allows administrators to manage hotels and rooms while enabling customers to browse hotels and create bookings.

The application uses:

- Java 17
- Spring Boot 4
- Spring Web
- Spring Data JPA
- MySQL
- Maven
- Hibernate ORM

The project follows a layered architecture using:

- Controller Layer
- Service Layer
- Repository Layer
- DTO Mapping
- Entity Models

---

# Features

## Admin Features

### Hotel Management
- Create hotel
- List manager-owned properties
- Update hotel
- Activate hotel
- Delete hotel
- Get hotel by ID
- Enforce property ownership across hotel and room administration

### Room Management
- Create rooms for a hotel
- View all rooms in a hotel
- Get room by ID
- Delete room

---

## Customer Features

### Hotel Browsing
- Search hotels
- View hotel details and information
- View date-specific room availability and exact stay pricing

### Booking Features
- Initialize booking
- Add guests to booking
- Confirm and cancel bookings
- Complete an idempotent demo payment before confirmation
- Manage bookings with a private guest token
- Register, sign in, and view account booking history

---

# Project Structure

```bash
HotelBooker/
│
├── src/main/java/com/swappy
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   ├── service
│   └── config
│
├── src/main/resources
│   └── application.properties
│
├── pom.xml
└── README.md
```

---

# Technologies Used

| Technology | Purpose |
|---|---|
| Java 17 | Backend Language |
| Spring Boot | Application Framework |
| Spring Web | REST APIs |
| Spring Data JPA | Database Operations |
| Hibernate | ORM Framework |
| MySQL | Database |
| Maven | Dependency Management |

---

# Prerequisites

Before running the project make sure the following software is installed:

- Java 17+
- Maven
- MySQL Server
- IDE such as IntelliJ IDEA or Eclipse
- Postman (recommended for API testing)

---

# Quick Demo (No MySQL Setup)

Run the API with the `demo` profile to use an in-memory H2 database populated with
sample Berlin and Hamburg properties, rooms, and one year of availability:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Then start the React client in another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173). Demo data is recreated each
time the API starts; the normal profile continues to use MySQL.

Demo accounts:

| Role | Email | Password |
|---|---|---|
| Guest | `demo@stayly.local` | `StaylyDemo123!` |
| Hotel manager | `manager@stayly.local` | `StaylyDemo123!` |

Authentication uses an opaque bearer token. Register or sign in through
`/api/v1/auth/register` or `/api/v1/auth/login`, then send the returned token as
`Authorization: Bearer <token>`. Account bookings are available at
`/api/v1/account/bookings`.

The demo profile also enables the checkout token `tok_demo_visa`. Raw card
details are never accepted by the API. Demo payments are disabled by default in
the normal profile so a real payment provider can replace this adapter safely.

---

# Database Setup

## Step 1: Create Database

Open MySQL and run:

```sql
CREATE DATABASE hotelbooker;
```

---

## Step 2: Update Database Credentials

Open:

```bash
src/main/resources/application.properties
```

Update:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

Replace with your own MySQL username and password if required.

---

# Application Configuration

Current configuration:

```properties
spring.application.name=HotelBooker

spring.datasource.url=jdbc:mysql://localhost:3306/hotelbooker
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.servlet.context-path=/api/v1
springdoc.swagger-ui.path=/swagger-ui.html
```

---

# How to Run the Application

## Method 1: Using IDE

### IntelliJ IDEA / Eclipse

1. Open the project
2. Wait for Maven dependencies to download
3. Run:

```bash
HotelBookerApplication.java
```

4. Application starts on:

```bash
http://localhost:8080/api/v1
```

---

## Method 2: Using Maven

Open terminal inside project folder:

```bash
mvn spring-boot:run
```

---

## Method 3: Build JAR File

```bash
mvn clean install
```

Run generated jar:

```bash
java -jar target/HotelBooker-0.0.1-SNAPSHOT.jar
```

---

# API Base URL

```bash
http://localhost:8080/api/v1
```

---

# API Endpoints

# 1. Hotel Management APIs

## Create Hotel

### Endpoint

```http
POST /api/v1/admin/hotels
```

### Sample Request

```json
{
  "name": "Luxury Palace",
  "city": "Berlin",
  "address": "Alexanderplatz",
  "description": "5 star hotel"
}
```

---

## Get Hotel By ID

```http
GET /api/v1/admin/hotels/{id}
```

---

## Update Hotel

```http
PUT /api/v1/admin/hotels/{id}
```

---

## Delete Hotel

```http
DELETE /api/v1/admin/hotels/{id}
```

---

## Activate Hotel

```http
PATCH /api/v1/admin/hotels/{id}/activate
```

---

# 2. Room Management APIs

## Create Room

```http
POST /api/v1/admin/hotels/{hotelId}/rooms
```

### Sample Request

```json
{
  "roomType": "DELUXE",
  "price": 2000,
  "capacity": 2
}
```

---

## Get All Rooms

```http
GET /api/v1/admin/hotels/{hotelId}/rooms
```

---

## Get Room By ID

```http
GET /api/v1/admin/hotels/{hotelId}/rooms/{roomId}
```

---

## Delete Room

```http
DELETE /api/v1/admin/hotels/{hotelId}/rooms/{roomId}
```

---

# 3. Hotel Browse APIs

## Search Hotels

```http
POST /api/v1/hotels/search
```

### Sample Request

```json
{
  "city": "Berlin",
  "checkInDate": "2026-05-20",
  "checkOutDate": "2026-05-25",
  "rooms": 1
}
```

---

## Get Hotel Information

```http
GET /api/v1/hotels/{hotelId}/info
```

---

# 4. Booking APIs

## Initialize Booking

```http
POST /api/v1/bookings/init
```

### Sample Request

```json
{
  "hotelId": 1,
  "roomId": 1,
  "checkInDate": "2026-05-20",
  "checkOutDate": "2026-05-25"
}
```

---

## Add Guests

```http
POST /api/v1/bookings/{bookingId}/addGuests
```

### Sample Request

```json
[
  {
    "name": "John Doe",
    "age": 28,
    "gender": "MALE"
  },
  {
    "name": "Jane Doe",
    "age": 26,
    "gender": "FEMALE"
  }
]
```

---

# Testing APIs

You can test APIs using:

- Postman
- Swagger UI
- Thunder Client

---

# Swagger UI

If SpringDoc is configured correctly, open:

```bash
http://localhost:8080/api/v1/swagger-ui/index.html
```

or

```bash
http://localhost:8080/swagger-ui.html
```

---

# Common Issues and Fixes

## 1. MySQL Connection Error

### Problem

```text
Communications link failure
```

### Solution

- Make sure MySQL server is running
- Verify username and password
- Verify database exists

---

## 2. Port Already In Use

### Problem

```text
Port 8080 already in use
```

### Solution

Change port in:

```properties
server.port=8081
```

---

## 3. Maven Dependency Errors

Run:

```bash
mvn clean install
```

or refresh Maven project in IDE.

---

# Future Improvements

Possible enhancements:

- JWT Authentication
- Role Based Access Control
- Payment Gateway Integration
- Email Notifications
- Booking Cancellation
- Docker Support
- Kubernetes Deployment
- Redis Caching
- CI/CD Pipeline
- Unit and Integration Testing

---

# Recommended API Testing Flow

1. Create hotel
2. Activate hotel
3. Create rooms
4. Search hotel
5. Initialize booking
6. Add guests

---

# Author

Developed using Spring Boot and MySQL.
