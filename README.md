# Frank M-Pesa — franksproject

Everything for the Frank M-Pesa business system in one place: a cross-border money transfer / forex
operation (Uganda ↔ Kenya, Congo, Tanzania, Rwanda) with two shops, **Town Branch** and **Kisenyi**,
taking and sending payments through **Safaricom M-Pesa (Daraja)** and **Equity Bank (Jenga)**.

Open **`franksproject.code-workspace`** in VS Code (the desktop shortcut does this).

## What's in here

| Folder | What it is | Tech | Port |
|---|---|---|---|
| `server/` | **Main API.** Shop records (buying, selling, expenses, open/close, deposits, withdrawals, commission), exchange rates, customers, users/login, M-Pesa STK push + B2C, Jenga STK push / send money / bank transfer. Talks to two MySQL databases: `kisenyi` and `customer`. | Spring Boot 3.2, Java 17, Gradle | 8080 |
| `mobile-app/` | **Owner/staff Android app.** Town & Kisenyi shop screens, rates, customers, major transactions (M-Pesa / Equity send + prompt customer), settings, biometric login, offline Room cache + WorkManager sync. Every request is AES-encrypted with an RSA-wrapped key. | Android (Java), Retrofit, Room | — |
| `customer-portal/backend/` | **Customer website API.** Customer registration, login (session), password reset by email, today's rates. Reads the `Customer` database directly. | Node.js, Express 5, mysql2 | 3000 |
| `customer-portal/frontend/` | React rewrite of the customer pages (in progress). | React 19, Vite, TypeScript | 5173 |
| `customer-portal/legacy/` | The original plain HTML/CSS/JS customer pages (`Customers/`, served by the backend) and the old single-file server `serverjs.js`. Kept for reference until the React version replaces them. | HTML, Express | — |
| `website/owner-help/` | **Help Center** for the mobile app (light/dark). Published at <https://hawer-tow.github.io/FrankM-pesa/> from the `Hawer-tow/FrankM-pesa` repo, where `helpcenter.html` is named `index.html`. | Static HTML | — |
| `website/customer-help/` | Placeholder for a customer help center. | — | — |
| `database/` | `init.sql` (creates the two databases), optional `docker-compose.yml`, and the working SQL scripts in `scripts/`. | MySQL 8 | 3306 |
| `docs/` | Board resolution, the shortcut icon, and the old WSL run notes. | — | — |

```
Android app ──(encrypted HTTPS via ngrok)──► server :8080 ──► MySQL: kisenyi, customer
                                                  │
                                                  ├──► Safaricom Daraja (sandbox)
                                                  └──► Equity Jenga (UAT)

Browser ──► customer-portal backend :3000 ──► MySQL: Customer
```

## First-time setup

1. **Secrets.** Each part reads its passwords and API keys from a local `.env` file that git ignores:
   `server/.env`, `customer-portal/backend/.env`, `database/.env`. They were filled in from the old
   project. On a new machine, copy each `.env.example` to `.env` and fill in the values.
   The private keys in `server/src/main/resources/keys/*private*.pem` and `server/certs/password.txt`
   are also local-only.
2. **Java 17** at `C:\Program Files\Java\jdk-17` (the server tasks point `JAVA_HOME` there).
3. **MySQL 8**: the Windows service `MySQL80`. Run `database/init.sql` once to create the databases.
4. **Node packages**: run the task **Customer portal: install packages**.
5. **Android**: run the task **Mobile app: open in Android Studio**.

## Quick start: the server the phone app needs

Double-click **`start-frankmpesa.bat`** (in this folder). It checks MySQL is running and port 8080 is free,
then opens two windows: the **server** (`gradlew bootRun` in `server\`) and the **ngrok tunnel**
(`ngrok http 8080`). Wait for `Started MobileDbApplication`, then pull down the profile list in the app.
Close the two windows to stop. (Running `gradlew bootrun` from this top folder no longer works: the server
project is in `server\`.)

**Customer website:** double-click **`start-website.bat`**. It checks MySQL and port 3000, installs the
website packages on first run, starts the portal server (`node server.js` in `customer-portal\backend`) and
opens <http://localhost:3000/customers.html>. Close its window to stop. The **Help Center** is a plain page:
open `website\owner-help\helpcenter.html`. Desktop shortcuts: *Start FrankMpesa server*, *FrankMpesa website*.

## Running (Terminal → Run Task…)

| Task | Does |
|---|---|
| MySQL: start service (asks for admin) | Starts `MySQL80` (it is set to Manual start). |
| Main server: Spring Boot (port 8080) | `gradlew bootRun` in `server/`. |
| Tunnel: ngrok for the app (port 8080) | Opens `https://geomagnetic-confessedly-juanita.ngrok-free.dev` → port 8080, the address the phone app uses. |
| Start app backend (server + tunnel) | The two above together: what the phone app needs. |
| Customer portal: backend (port 3000) | `node server.js`. Customer pages at <http://localhost:3000/customers.html>. |
| Customer portal: React frontend (Vite) | `npm run dev`. |
| Start everything | The three servers above, each in its own terminal. |
| Website: open Help Center in browser | Opens `website/owner-help/helpcenter.html`. |

The mobile app's server address is the ngrok URL in
`mobile-app/app/src/main/java/com/example/mobiledbclient/ApiClient.java` (`BASE_URL`), and the
Daraja callback URLs in `server/src/main/resources/application.properties` use the same tunnel.

## Where this came from

Copied on 2026-10-19 from the four original locations, which were left untouched:

| Here | Original |
|---|---|
| `server/` | `C:\Users\PC\git\MobileDbClient_SERVER_frank\MobileDb` (GitHub `Hawer-tow/MobileDbClient_SERVER_frank`) |
| `mobile-app/` | `C:\Users\PC\AndroidStudioProjects\MobileDbClient` (app git: `Hawer-tow/MobileDbClient_frank`) |
| `customer-portal/` | `OneDrive\Documents\MobileDb\transaction-server` + `MobileDb\Customers` |
| `website/` | `OneDrive\Documents\frankmpesawebsite\Help` |
| `database/scripts/` | `OneDrive\Documents\*.sql` |

Not copied (can be regenerated or downloaded again): build output, `node_modules`, the bundled
`gradle-9.3.1` distribution, `.vs`/`.idea`/Eclipse files, the MySQL installer, old shortcuts.

## ⚠️ Security: rotate these

The public GitHub repo `Hawer-tow/MobileDbClient_SERVER_frank` contains the MySQL root password,
the Jenga API key + consumer secret + private key, the Spring Security password, the Daraja keys and the
app↔server private key. Moving them to `.env` here stops new leaks but does not undo old ones:
**change each of them**, then make that repo private or delete it.

## Payments (server)

Every request to Safaricom (Daraja) or Equity (Jenga) is logged in the `provider_transactions` table
(created automatically on the next server start): reference, provider ids, phone, amount, status
(`PENDING`, `SUCCESS`, `FAILED`, `CANCELLED`, `TIMEOUT`), result message and receipt.

- **Payment prompts** (`POST /mpesa/stkpush`, `POST /equity/stkpush`): the buy is recorded only when the
  provider confirms the customer paid. Cancelled or unanswered prompts never become buys.
- **Send money** (`POST /mpesa/sendMoney`, `POST /equity/sendmoney`, `POST /equity/sendToAccount`): the sale is
  created as `PENDING` when the provider accepts the request and updated when the result arrives
  (Equity-to-Equity transfers are final immediately).
- **Status**: `GET /payments/{reference}`. For an M-Pesa prompt still pending after 30 s it queries Safaricom
  directly, so a missed callback (tunnel down) does not leave it stuck.
- Rules from the provider docs are applied before sending: Kenyan numbers as `2547…`, whole shillings for
  Daraja, `"500.00"` strings for Jenga, length limits on references and descriptions, Jenga signatures in the
  documented field order. Unit tests: `gradlew test --tests "com.example.MobileDb.payments.*" --tests "com.example.MobileDb.service.PaymentRecorderTest"`.
- Equity merchant account, name and callback address are in `application.properties` (`jenga.*`).

## Known issues to fix

- [x] ~~Admin login skips the password check.~~ Admins now need a valid password or biometric; they still skip the inactive-user check.
- [x] ~~M-Pesa callbacks never arrive.~~ Daraja URLs now point at `/mpesa/callback/...`, and `UserAccessFilter` lets Daraja and Jenga callbacks through.
- [ ] **Callbacks are not authenticated.** Anyone who knows the URL can post a fake "success" result. Add a secret token to the callback URLs (or an IP allowlist).
- [ ] **There is no real login session.** The server trusts the `X-User-Id` header, and the encryption key is public (`/api/public-key`), so anyone can call the API as any user. Login should return a token that later requests must carry.
- [x] ~~`UserAccessFilter` calls `userOpt.get()` before checking `isEmpty()`.~~ An unknown `X-User-Id` now gets a 401.
- [ ] `GET /api/users` is open and returns password hashes. The app uses those hashes for offline login, so fixing this needs an app change too. (The server no longer logs raw passwords, AES keys or decrypted payloads.)
- [x] ~~Biometric public keys are kept in memory.~~ Fingerprint login now stores the phone's public key in `users.biometric_public_key`; the phone signs a time-stamped login message with a fingerprint-locked Keystore key (EC P-256), and the server verifies it (`BiometricService`, with tests).
- [x] ~~RSA keys are read from `src/main/resources/...` file paths.~~ They now load from the classpath, so a packaged jar works.
- [x] ~~The app calls `PUT api/users/{id}/biometric-status`, which the server doesn't have.~~ It now turns fingerprint login off. Turning it on (`enable-biometrics`) requires the profile password.
- [x] ~~Customer portal pages call the old API paths.~~ The redesigned pages call `/api/auth/*`, `/api/customers/currentCustomer` and `/api/rates/*`, and the customer area is now really behind the login check. The React app (`frontend/`) still has no Vite proxy to port 3000 and still uses the old look.
- [x] ~~The Settings "Fingerprint login" switch does nothing.~~ It now sets fingerprint login up (after asking for the password) or removes it.
- [x] ~~Login right after logging out could be refused~~ (the login request had no `X-User-Id`); the filter now lets `/api/users/login` through without one.
- [ ] Customer passwords are predictable (`Fm/YYMMHHMM/<code>6<code>/<id>`), stored in plain text, and returned by the server's `GET /api/customers`.
- [ ] Session secret in `customer-portal/backend/server.js` is a placeholder.
- [ ] `server/build.gradle` has duplicate dependencies, two MySQL drivers and H2.
- [ ] Hard-coded ngrok URL in the app; `usesCleartextTraffic="true"`.
