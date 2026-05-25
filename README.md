# Notebook

AI-native notebook and planning workspace built with React, Spring Boot, PostgreSQL, Docker, and DDD.

## Apps

- `apps/web` - React + TypeScript + Vite web app.
- `apps/api` - Spring Boot API with DDD module boundaries.

## Local Development

1. Copy environment defaults:

```powershell
Copy-Item .env.example .env
```

2. Start local infrastructure:

```powershell
docker compose up -d postgres
```

3. Start the web app:

```powershell
npm --prefix apps/web install
npm --prefix apps/web run dev
```

4. Start the API with Java 21:

```powershell
Set-Location apps/api
.\mvnw.cmd spring-boot:run
```

## Architecture

The backend follows DDD module boundaries. Each domain has:

- `application`
- `domain`
- `infrastructure`

Domains are independent and communicate through events.
