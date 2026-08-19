# Banking demo frontend (React + Vite)

A small single-page app that walks the whole backend flow — register → login →
create wallet → top up → transfer (fraud-checked) → read the ledger — with an
activity log showing every request and response. All traffic goes through the
**API gateway** (single origin), so the browser only ever talks to one URL.

```
React (localhost:3000)  ──►  api-gateway (localhost:8090)  ──►  auth / user / wallet / payment / ledger
```

## Run it

Bring up the backend first, then the gateway, then this app.

```bash
# 1. Infra (from repo root)
docker compose up -d                 # postgres DBs + kafka

# 2. Backend services (each in its own terminal, or background), JDK 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn -q -pl user-service    spring-boot:run
mvn -q -pl wallet-service  spring-boot:run
mvn -q -pl auth-service    spring-boot:run
mvn -q -pl fraud-service   spring-boot:run
mvn -q -pl ledger-service  spring-boot:run
mvn -q -pl payment-service spring-boot:run
mvn -q -pl api-gateway     spring-boot:run   # :8090

# 3. Frontend
cd frontend
npm install
npm run dev                          # http://localhost:3000
```

The gateway base URL defaults to `http://localhost:8090`; override with a `.env`
(`VITE_API_BASE=...`, see `.env.example`).

## What each step shows

| Step | Calls | Demonstrates |
|---|---|---|
| Register | `POST /api/auth/register` | auth-service creates the user over gRPC; returns the shared `userId` + JWT + refresh token |
| Login | `POST /api/auth/login` | login by `userId`; issues a fresh JWT |
| Wallet | `POST /api/wallets`, `GET /api/wallets/user/{id}` | JWT-protected; gives you an `account_no` |
| Top up | `POST /api/payment/topup` | payment → wallet (gRPC), then a Kafka event to the ledger |
| Transfer | `POST /api/payment/transfer` | **fraud pre-check** (gRPC) then wallet transfer; try > 10,000.00 to get a 403 |
| Ledger | `GET /api/ledger/accounts/{acct}/transactions` | what the ledger consumed from Kafka |

Amounts are entered in main units (e.g. `50.00`) and sent as integer minor units.

## ⚠️ Two backend changes needed for the payment steps to work

The register/login/wallet/ledger steps work against the services as-is. The
**payment** steps currently won't, for two pre-existing reasons:

1. **payment-service has JPA on the classpath but no datasource configured**, so
   it fails to start. It needs either a database (the k8s manifests wire it a
   `payment_db`) or `DataSourceAutoConfiguration` excluded if it truly has no
   persistence.
2. **`PaymentController` accepts protobuf-generated types** (`CreditRequest`,
   `TransferRequest`) as `@RequestBody`. Jackson can't bind JSON to those, so
   `/topup` and `/transfer` reject the request. Switching them to plain record
   DTOs (like the wallet controller uses) fixes it.

Both are quick — say the word and I'll apply them so the full demo runs green.
