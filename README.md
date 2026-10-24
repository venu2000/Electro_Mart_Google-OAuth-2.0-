# ElectroMart

A learning project: Spring Boot 3 backend with Google OAuth2/OIDC login, an application-issued JWT in an HttpOnly cookie, MySQL persistence, and a React + Vite frontend.

## Architecture

- **Backend** (`backend/`): Spring Boot 3.5, Java 21, Maven, Spring Security 6, Spring Data JPA, MySQL, jjwt 0.12.
- **Frontend** (`frontend/`): React 18, Vite 5, React Router 6, Axios. Calls the backend directly at `http://localhost:8080` with credentials. There is no Vite proxy.

## Authentication flow

1. The user clicks "Sign in with Google" on `http://localhost:5173/login`. The browser navigates to `http://localhost:8080/oauth2/authorization/google`.
2. Google authenticates the user and redirects to `http://localhost:8080/login/oauth2/code/google`.
3. The backend validates Google's ID token (Spring's OIDC support). `CustomOidcUserService` requires `email_verified = true`, finds or creates the local user keyed by Google's `sub`, and assigns the role from the database.
4. `OAuth2LoginSuccessHandler` issues **the application's own JWT** (HS256, signed with `JWT_SECRET`) in the `ELECTROMART_JWT` cookie (HttpOnly, SameSite=Lax, Path=/, Secure when `COOKIE_SECURE=true`). It invalidates the temporary OAuth session and redirects to the frontend `/products`.
5. Each request to `/api/**` is authenticated by `JwtAuthenticationFilter`, which reads the cookie (or an `Authorization: Bearer` header) and validates the signature, issuer and expiry.
6. Requests without a valid JWT receive **401 JSON**. They are never redirected to Google.

Google tokens are never sent to the browser. Tokens never appear in URLs.

### Step-by-step implementation

| Step | Where it happens | What it does |
|---|---|---|
| 1. Start login | [LoginPage.jsx](frontend/src/pages/LoginPage.jsx) | The button calls `loginWithGoogle()` in [authService.js](frontend/src/services/authService.js), which does a full-page redirect to `/oauth2/authorization/google`. |
| 2. Google consent | Google | Google asks for the `openid`, `profile` and `email` scopes (set in [application.yml](backend/src/main/resources/application.yml)). |
| 3. Token validation | Spring `oauth2Login` | Spring checks Google's ID token (signature, issuer, audience, nonce). |
| 4. Verified email check | [CustomOidcUserService.java](backend/src/main/java/com/electromart/security/CustomOidcUserService.java) | Rejects the login unless `email_verified` is true. |
| 5. Local user | [UserService.java](backend/src/main/java/com/electromart/service/UserService.java) | Finds the `users` row by Google `sub`, or creates it with role `USER`. Profile fields refresh on each login. |
| 6. App JWT | [JwtService.java](backend/src/main/java/com/electromart/security/JwtService.java) | Signs an HS256 token with `JWT_SECRET`. Subject is the local user id. Claims: `email`, `role`. Expires after `app.jwt.expiration` (1h). |
| 7. Cookie and redirect | [OAuth2LoginSuccessHandler.java](backend/src/main/java/com/electromart/security/OAuth2LoginSuccessHandler.java) | Sets the `ELECTROMART_JWT` cookie, ends the temporary session, redirects to `FRONTEND_URL/products`. |
| 8. Authenticated calls | [JwtAuthenticationFilter.java](backend/src/main/java/com/electromart/security/JwtAuthenticationFilter.java) | Reads the cookie (or `Authorization: Bearer`) on every request and sets the security context. Expired or invalid tokens yield 401 JSON. |
| 9. Current user | [AuthContext.jsx](frontend/src/context/AuthContext.jsx) | On page load, calls `GET /api/users/me` to learn who is logged in. JavaScript never reads the JWT. |
| 10. Logout | `POST /api/auth/logout` | Returns 204 and clears the cookie. The frontend then clears its user state. |

### Where the JWT lifetime and secret live

- Lifetime: `app.jwt.expiration: 1h` in [application.yml](backend/src/main/resources/application.yml). It is bound to `AppProperties` and read by `JwtService`. The cookie's max age uses the same value.
- Secret: the `JWT_SECRET` environment variable. It is never stored in the repository.

## Login page

The route `/login` ([LoginPage.jsx](frontend/src/pages/LoginPage.jsx), [login.css](frontend/src/styles/login.css)) contains:

- A full-screen hero with a parallax background and a glass-style sign-in card.
- A "Why shop with us" section with icon cards.
- A "Browse by category" section with images loaded from Unsplash. These need internet access. If an image fails, its card is hidden.
- A closing sign-in card.
- Scroll effects: elements are pulled toward a central black hole as the user scrolls, and content fades in on reveal.
- The effects are disabled when the operating system has reduced motion turned on.

While the session check is running, the page shows a loading spinner. If the user is already signed in, the page redirects to `/products` (or to the page they originally requested).

## Security notes

- CSRF protection is **on**. The token is in the `XSRF-TOKEN` cookie (readable by JavaScript) and must be sent in the `X-XSRF-TOKEN` header on POST/PUT/DELETE. The frontend does this automatically.
- CORS allows only the exact `FRONTEND_URL` origin, with credentials.
- Identity is Google's `sub` claim, not the email address.
- Stack traces are logged on the server only, never returned to clients.
- Secrets are read from environment variables. `.env` is git-ignored.

### Logout limitation

JWTs are stateless. Logout clears the cookie in the browser, but a copy of the token captured before logout remains valid until it expires (default 1 hour). Real revocation would need a server-side denylist or short-lived tokens with refresh, which are out of scope here.

## Prerequisites

- JDK 21 or newer (Java 21 target)
- Maven 3.9+
- MySQL 8+
- Node.js 18+ and npm
- A Google Cloud OAuth 2.0 Client ID and Secret

## Credentials you must provide before running

The application does not start without these. None of them are stored in the repository.

| Variable | Required | Default | Where to get it |
|---|---|---|---|
| `GOOGLE_CLIENT_ID` | Yes | none | Google Cloud Console (see below) |
| `GOOGLE_CLIENT_SECRET` | Yes | none | Google Cloud Console (see below) |
| `JWT_SECRET` | Yes | none | You generate it (at least 32 characters) |
| `DB_USERNAME` | Yes | none | Your MySQL user (for example `root`) |
| `DB_PASSWORD` | Yes | none | Your MySQL password |
| `DB_URL` | No | `jdbc:mysql://localhost:3306/electromart?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | Change only if MySQL runs elsewhere |
| `FRONTEND_URL` | No | `http://localhost:5173` | Frontend origin |
| `COOKIE_SECURE` | No | `false` | `true` only when served over HTTPS |

Generate a `JWT_SECRET` (PowerShell):

```powershell
[guid]::NewGuid().ToString("N") + [guid]::NewGuid().ToString("N")
```

Or on macOS/Linux: `openssl rand -base64 48`

Never commit real values, and never paste them into issues or chat.

## Google OAuth setup

1. Open Google Cloud Console, then APIs & Services. Create or select a project.
2. Under **OAuth consent screen**, choose **External**, fill in the app name and your email, and add the scopes `openid`, `.../auth/userinfo.profile` and `.../auth/userinfo.email`.
3. While the app is in **Testing** mode, add your Google account under **Test users**. Otherwise Google blocks sign-in.
4. Under **Credentials**, choose **Create credentials, OAuth client ID**, and select type **Web application**.
5. Add the authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
6. Add the authorized JavaScript origin: `http://localhost:5173` (optional for this flow).
7. Copy the Client ID (ends with `.apps.googleusercontent.com`) and the Client Secret into `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET`.

## MySQL setup

Option A: a dedicated user (recommended). Use a strong password of your own in place of the placeholder.

```sql
CREATE DATABASE electromart CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'electromart'@'localhost' IDENTIFIED BY 'your-strong-password';
GRANT ALL PRIVILEGES ON electromart.* TO 'electromart'@'localhost';
```

Then set `DB_USERNAME=electromart` and `DB_PASSWORD=your-strong-password`.

Option B: use the existing `root` user and set `DB_USERNAME=root` with its password.

The application also creates the database if the user is allowed to (`createDatabaseIfNotExist=true`). Tables are created automatically (`ddl-auto: update`). Ten sample products are seeded on first startup and skipped on later startups.

## Environment variables

Spring Boot does **not** read `.env` files. The variables must exist in the environment of the process that starts the backend. `.env.example` at the repository root is a template that lists every variable with placeholder values.

Set them for the current PowerShell window only:

```powershell
$env:GOOGLE_CLIENT_ID = "your-client-id.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET = "your-client-secret"
$env:JWT_SECRET = "your-generated-secret-of-at-least-32-characters"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-mysql-password"
$env:FRONTEND_URL = "http://localhost:5173"
$env:COOKIE_SECURE = "false"
```

To keep them across sessions on Windows, store them as User variables. Using `Read-Host` keeps the values out of your command history:

```powershell
[Environment]::SetEnvironmentVariable("GOOGLE_CLIENT_ID", (Read-Host "Google Client ID"), "User")
[Environment]::SetEnvironmentVariable("GOOGLE_CLIENT_SECRET", (Read-Host "Google Client Secret"), "User")
[Environment]::SetEnvironmentVariable("JWT_SECRET", (Read-Host "JWT secret"), "User")
[Environment]::SetEnvironmentVariable("DB_USERNAME", (Read-Host "MySQL username"), "User")
[Environment]::SetEnvironmentVariable("DB_PASSWORD", (Read-Host "MySQL password"), "User")
```

Open a new terminal after this, so it picks up the values.

On macOS or Linux, use `export NAME=value` in the shell, or put the same lines in your shell profile.

## Run locally

On the machine where you run the backend, make sure the variables above are set. Then:

Backend (terminal 1):

```powershell
cd backend
mvn spring-boot:run
```

Frontend (terminal 2):

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173/login`. The optional file `frontend/.env.example` only sets `VITE_API_BASE_URL`, which defaults to `http://localhost:8080`.

### Setup on a new machine (checklist)

1. Install JDK 21, Maven 3.9+, MySQL 8+, Node.js 18+.
2. Clone the repository and create the MySQL user or use `root` (see MySQL setup).
3. Create the Google OAuth Web client with the redirect URI from the Google OAuth setup section. Use an email that is added as a test user.
4. Set the required variables (see Credentials you must provide). Open a new terminal.
5. Run the backend, then the frontend.
6. Open `http://localhost:5173/login`, sign in with Google, and confirm you land on `/products` with 10 items.

Each developer creates their own Google client and MySQL credentials. Do not share them through the repository.

## Testing

Unit tests (JWT behaviour):

```powershell
cd backend
mvn test
```

Manual checks:

- `curl -i http://localhost:8080/api/products` returns **401** with JSON.
- After signing in, `http://localhost:5173/products` lists 10 products.
- `GET /api/users/me` returns your profile.
- The `ELECTROMART_JWT` cookie is HttpOnly (not visible in `document.cookie`).

## Troubleshooting

- **Error 401: invalid_client** ("The OAuth client was not found"): `GOOGLE_CLIENT_ID` or `GOOGLE_CLIENT_SECRET` is wrong, or still a placeholder. Restart the backend after setting real values, because a running process keeps old values.
- **Error 403: access_denied** from Google: your Google account is not a test user while the app is in Testing mode.
- **redirect_uri_mismatch** from Google: the redirect URI in the console must be exactly `http://localhost:8080/login/oauth2/code/google`.
- **Products page redirects to login**: check that `FRONTEND_URL` matches the browser origin and that `withCredentials` is enabled (it is by default in `api.js`).
- **403 on POST**: the CSRF header is missing. Confirm the `XSRF-TOKEN` cookie exists after loading the app.
- **Startup fails with JWT_SECRET error**: the secret must be at least 32 bytes.
- **Startup fails with DB errors**: verify the MySQL service is running and the credentials match.
- **Cookie not stored**: with `COOKIE_SECURE=true` the site must be served over HTTPS.

## Project layout

```
backend/src/main/java/com/electromart/
  config/      SecurityConfig, AppProperties, ProductDataSeeder
  controller/  AuthController, UserController, ProductController
  security/    JwtService, JwtAuthenticationFilter, AuthCookies, OAuth2 handlers, entry points
  service/     UserService, ProductService
  entity/      User, Product, Role, AuthProvider
  repository/  UserRepository, ProductRepository
  dto/         response records and ApiError
  exception/   GlobalExceptionHandler
frontend/src/
  services/    api.js (Axios + CSRF), authService.js, productService.js
  context/     AuthContext.jsx
  routes/      ProtectedRoute, AppLayout
  components/  Navbar, UserMenu, ProductCard, LoadingSpinner, ErrorMessage
  pages/       Login, Products, ProductDetails, Profile, AccessDenied, NotFound
  styles/      global.css, login.css
```
