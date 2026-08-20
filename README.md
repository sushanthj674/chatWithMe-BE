# chatWithMeBackend

Spring Boot backend for the `chatWithMe` React Native app. It gives the app a
device registry (who's online) and a message relay, so a message sent from
one device is stored and pushed via FCM to every other registered device.

Stack: Java 17, Spring Boot 3.3, Spring Data JPA, H2 (file-mode, no external
DB server), Firebase Admin SDK for Java.

## Setup

### 1. Get a Firebase service account key (required for pushes to work)

This is **not** `android/app/google-services.json` from the RN app — that
file is client-only and the Admin SDK can't use it.

1. Firebase Console → project `notification-learn-1f3f8` → gear icon →
   Project settings → **Service accounts** tab → **Generate new private key**.
2. Save the downloaded JSON somewhere outside this repo (or anywhere covered
   by `.gitignore` — see the patterns already in `.gitignore`).
3. Never commit it.

If you skip this step, the server still runs and stores messages/devices —
it just logs a warning and silently skips the FCM push step.

The server accepts the credentials two ways:

- `FIREBASE_SERVICE_ACCOUNT_PATH` — path to the JSON file (used by the
  docker-compose volume mount locally).
- `FIREBASE_SERVICE_ACCOUNT_JSON` — the raw JSON file contents, for hosts
  (Railway, etc.) that only give you env vars and no file mounts. Paste the
  whole file's contents in as the value.

If both are set, `FIREBASE_SERVICE_ACCOUNT_JSON` wins.

### 2. Run

```bash
export FIREBASE_SERVICE_ACCOUNT_PATH=/absolute/path/to/service-account.json
./mvnw spring-boot:run
```

(No `mvnw` wrapper committed yet — if you don't have Maven installed locally,
run `mvn -N wrapper:wrapper` once to generate one, or use a local `mvn`
install: `mvn spring-boot:run`.)

The server binds `0.0.0.0:8080`.

## Run with Docker

Build and run directly:

```bash
docker build -t chatwithme-backend .
docker run -d --name chatwithme-backend -p 8080:8080 \
  -v "$(pwd)/data:/app/data" \
  -v "/absolute/path/to/service-account.json:/secrets/firebase-service-account.json:ro" \
  -e FIREBASE_SERVICE_ACCOUNT_PATH=/secrets/firebase-service-account.json \
  chatwithme-backend
```

Or with `docker-compose` (reads the service account path from `.env`):

```bash
cp .env.example .env
# edit .env: FIREBASE_SERVICE_ACCOUNT_HOST_PATH=/absolute/path/to/service-account.json
docker compose up -d --build
```

If you skip the service account entirely, both commands still work — pushes
are just disabled (see the "Setup" section above). `./data` is a bind mount
so the H2 file DB survives container restarts/rebuilds.

To host this "somewhere" reachable from a real device (not just the
emulator's `10.0.2.2`), deploy the built image to any container host (a VM,
Fly.io, Railway, a home server, etc.) and point the RN app's base URL at
that host's address/port instead of `10.0.2.2`/the LAN IP. Nothing else in
this project needs to change — the app has no other host assumptions baked
in.

## Connecting from the RN app

- **Android emulator** → use `http://10.0.2.2:8080` as the base URL (the
  emulator's alias for the host machine's `localhost`).
- **Physical Android device** → use the Mac's LAN IP (e.g.
  `http://192.168.1.23:8080`), and make sure both are on the same network.

## API docs & health

- **Swagger UI**: `/swagger-ui.html` — interactive docs generated from the
  controllers below, try requests straight from the browser.
- **Raw OpenAPI spec**: `/v3/api-docs`.
- **Health check**: `/actuator/health` → `{"status":"UP"}`. Useful for Railway
  healthchecks and for confirming a remote deploy is actually up. Only
  `health`/`info` are exposed (see `management.endpoints.web.exposure.include`
  in `application.properties`) — there's no auth in front of this app yet, so
  endpoints like `env`/`metrics` stay off to avoid leaking config to anyone
  with the URL.

## API

### Devices

`POST /api/devices/register`
```json
{ "deviceId": "uuid", "name": "Sushanth's Pixel", "platform": "android", "fcmToken": "..." }
```
→ `200 { "deviceId": "...", "registeredAt": "2026-08-20T10:00:00Z" }`
Upserts by `deviceId`; refreshes `lastSeenAt`.

`POST /api/devices/{deviceId}/heartbeat`
```json
{ "fcmToken": "..." }
```
→ `204`. Call every ~20s while the app is foregrounded. `fcmToken` is
optional — send it when it rotates.

`GET /api/devices`
→ `200 [{ "deviceId", "name", "platform", "lastSeenAt", "online" }, ...]`
`online` is computed as `now - lastSeenAt < 45s` (`app.device.online-threshold-seconds`).

### Messages

`POST /api/messages`
```json
{ "deviceId": "sender-uuid", "senderName": "Sushanth", "text": "hey" }
```
→ `201 { "id", "deviceId", "senderName", "text", "createdAt" }`
Persists the message, then sends a **data-only** FCM message (no
`notification` block) to every other registered device's token, so the RN
app's own handlers in `notifications.ts` / `index.js` decide how to display
it instead of the OS auto-rendering a generic tray notification.

`GET /api/messages?since=<iso-timestamp>&limit=50`
→ `200 [{ "id", "deviceId", "senderName", "text", "createdAt" }, ...]`
oldest → newest. Omit `since` to get the most recent `limit` messages.

## Notes / deviations from the original contract

None currently — response shapes match the agreed contract as-is.
