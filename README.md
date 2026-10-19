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

## Running (Terminal → Run Task…)

| Task | Does |
|---|---|
| MySQL: start service (asks for admin) | Starts `MySQL80` (it is set to Manual start). |
| Main server: Spring Boot (port 8080) | `gradlew bootRun` in `server/`. |
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

## Known issues to fix

- [ ] **Admin login skips the password check.** `UserController.login` returns success for any `is_admin` user.
- [ ] **M-Pesa callbacks never arrive.** Daraja is given `/callback/...` URLs but the controller listens on `/mpesa/callback/...`, and `UserAccessFilter` rejects callbacks because they carry no encryption headers. Transactions stay `PENDING`.
- [ ] `UserAccessFilter` calls `userOpt.get()` before checking `isEmpty()`, so an unknown `X-User-Id` gives a 500.
- [ ] `GET /api/users` is open and returns password hashes. The server also logs raw passwords, AES keys and decrypted payloads.
- [ ] Biometric public keys are kept in memory (`BiometricService`), so biometric login breaks after every restart.
- [ ] RSA keys are read from `src/main/resources/...` file paths, which only works with `bootRun`, not from a packaged jar.
- [ ] The app calls `PUT api/users/{id}/biometric-status`, which the server doesn't have.
- [ ] Customer portal: the pages call `/api/login`, `/api/logout`, `/api/currentCustomer`, `/api/kenya2ugxrates`, … but `backend/` serves `/api/auth/*`, `/api/customers/currentCustomer`, `/api/rates/*` (only `legacy/serverjs.js` matches). The React app has no Vite proxy to port 3000.
- [ ] Customer passwords are predictable (`Fm/YYMMHHMM/<code>6<code>/<id>`), stored in plain text, and returned by the server's `GET /api/customers`.
- [ ] Session secret in `customer-portal/backend/server.js` is a placeholder.
- [ ] `server/build.gradle` has duplicate dependencies, two MySQL drivers and H2.
- [ ] Hard-coded ngrok URL in the app; `usesCleartextTraffic="true"`.
