# Stayly React frontend

The frontend is a React and Vite client for the HotelBooker Spring Boot API.

## Run locally

For a zero-setup local demo, start the backend from the repository root with the
seeded H2 profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

This creates sample Berlin and Hamburg properties with rooms and availability.
Use the default profile instead when connecting to the configured MySQL database.

Use `demo@stayly.local` / `StaylyDemo123!` for the guest journey or
`manager@stayly.local` / `StaylyDemo123!` for the protected property workspace.

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

Backend errors and empty searches are shown honestly in the UI. To deliberately
use the bundled hotel catalog while designing without a backend, opt in with:

```bash
VITE_ENABLE_DEMO_FALLBACK=true npm run dev
```

## Available routes

| Route | Purpose |
|---|---|
| `/` | Customer landing page and stay search |
| `/search` | Availability results and filters |
| `/hotels/:hotelId` | Property information and room selection |
| `/booking` | Inventory hold and guest submission |
| `/manage-booking` | Protected booking lookup and cancellation |
| `/account` | Sign in and account registration |
| `/my-bookings` | Authenticated booking history and cancellation |
| `/admin` | Manager-only hotel and room administration |

## Backend integration

The shared client in `src/api/client.js` unwraps the API response envelope and converts error responses into `ApiRequestError`. Validation details returned by the backend are shown in the UI.
It also attaches the active bearer token automatically. Account sessions live in
browser session storage and are validated with `/auth/me` whenever the app starts.

The booking page intentionally separates the workflow into three stages:

1. initialize the booking and reserve inventory;
2. attach validated guests to the same booking;
3. tokenize the demo card in the browser and complete an idempotent payment.

If guest submission fails, retrying does not create another inventory reservation. The backend expires abandoned reservations after ten minutes and returns their room counts to inventory.

The demo checkout uses Visa `4242 4242 4242 4242`, any future expiry, and any
three-digit CVC. Only `tok_demo_visa`, the cardholder name, and an idempotency
key reach the API; raw card numbers, expiry dates, and CVC values are not sent or
stored. This adapter is enabled only by the backend `demo` profile.

Confirmed bookings include a private access code. The booking number and access
code are required together to retrieve or cancel a guest booking; cancellation
is idempotent and returns the booked room count to inventory.

## Verification

```bash
npm run build
```

Backend verification is run from the repository root:

```bash
./mvnw test
```
