# Stayly React frontend

The frontend is a React and Vite client for the HotelBooker Spring Boot API.

## Run locally

Start the backend from the repository root:

```bash
JAVA_HOME=/path/to/java-17-or-newer ./mvnw spring-boot:run
```

Then start the frontend:

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173).

The Vite server proxies `/api` to `http://localhost:8080` by default. To use a backend on another port:

```bash
VITE_API_TARGET=http://localhost:8081 npm run dev -- --port 5174
```

## Available routes

| Route | Purpose |
|---|---|
| `/` | Customer landing page and stay search |
| `/search` | Availability results and filters |
| `/hotels/:hotelId` | Property information and room selection |
| `/booking` | Inventory hold and guest submission |
| `/admin` | Hotel and room administration |

## Backend integration

The shared client in `src/api/client.js` unwraps the API response envelope and converts error responses into `ApiRequestError`. Validation details returned by the backend are shown in the UI.

The booking page intentionally separates the workflow into two stages:

1. initialize the booking and reserve inventory;
2. attach validated guests to the same booking.

If guest submission fails, retrying does not create another inventory reservation. The backend expires abandoned reservations after ten minutes and returns their room counts to inventory.

## Verification

```bash
npm run build
```

Backend verification is run from the repository root:

```bash
./mvnw test
```
