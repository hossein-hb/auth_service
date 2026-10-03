# 🔐 Auth Service

<div align="center">

**A secure authentication service built with Spring Boot, Spring Security, JWT, PostgreSQL, and Redis.**

<br>

<img src="https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk" alt="Java 25">
<img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot">
<img src="https://img.shields.io/badge/Spring%20Security-6.x-green?style=for-the-badge&logo=springsecurity" alt="Spring Security">
<img src="https://img.shields.io/badge/PostgreSQL-database-blue?style=for-the-badge&logo=postgresql" alt="PostgreSQL">
<img src="https://img.shields.io/badge/Redis-cache-red?style=for-the-badge&logo=redis" alt="Redis">

</div>

---

## 📌 Overview

`auth_service` is a backend authentication system with a React frontend.

It provides:

* 🔑 JWT-based authentication
* 🔄 Access & refresh tokens
* 🍪 HttpOnly refresh-token cookies
* 📧 Email OTP verification
* 🔐 Password recovery
* 👥 Role-based authentication
* 🛡️ Redis-backed rate limiting
* 📱 Multi-device session management
* 🔒 BCrypt password hashing

---

## 🏗️ Architecture

```text
┌──────────────────────┐
│      React App       │
│   TypeScript + Vite  │
└──────────┬───────────┘
           │
           │ HTTP / JSON
           ▼
┌──────────────────────┐
│   Spring Boot API    │
├──────────────────────┤
│ Controllers          │
│ Services             │
│ Spring Security      │
│ JWT                  │
└───────┬────────┬─────┘
        │        │
        ▼        ▼
┌───────────┐ ┌───────────┐
│ PostgreSQL│ │   Redis   │
│   Users   │ │ OTP       │
│   Staff   │ │ Sessions  │
│           │ │ Rate Limit│
└───────────┘ └───────────┘
```

---

## ⚡ Tech Stack

| Layer            | Technology            |
| ---------------- | --------------------- |
| Backend          | Java 25 + Spring Boot |
| Security         | Spring Security + JWT |
| Database         | PostgreSQL            |
| Cache / State    | Redis                 |
| Email            | Spring Mail           |
| Password Hashing | BCrypt                |
| Frontend         | React + TypeScript    |
| Build            | Maven + Vite          |

---

## 🔑 Authentication

The service uses two JWTs:

### Access Token

Short-lived JWT used to access protected endpoints.

```http
Authorization: Bearer <access-token>
```

### Refresh Token

Long-lived JWT stored in an **HttpOnly cookie** and registered in Redis.

```text
Access Token
     │
     ├── expires
     │
     ▼
POST /api/auth/refresh
     │
     ▼
Refresh Token Cookie
     │
     ▼
Redis Session Check
     │
     ▼
New Access Token
```

Refresh sessions contain information such as:

* User ID
* JWT ID (`jti`)
* Device
* Browser
* Operating system
* IP address
* Creation time

---

## 👤 Roles

The application supports:

```text
USER
SUPPORT
ADMIN
```

Roles are included in the authentication process and JWT claims.

---

## 📧 Registration

User registration uses email OTP verification:

```text
Email
  ↓
Send OTP
  ↓
Verify OTP
  ↓
Temporary Signup Token
  ↓
Complete Registration
  ↓
Access + Refresh Tokens
```

Main endpoints:

```http
POST /api/auth/signup/send-code
POST /api/auth/signup/verify_code
POST /api/auth/user/complete_signup
POST /api/auth/staff/complete_signup
```

---

## 🔐 Password Recovery

Password recovery uses a short-lived OTP and temporary token:

```text
Email
  ↓
OTP
  ↓
Verify OTP
  ↓
Password Reset Token
  ↓
New Password
```

Endpoints:

```http
POST /forget-password/send-code
POST /forget-password/verify_code
POST /forget-password/set-password
```

---

## 🛡️ Security

The project uses several layers of protection:

<div align="center">

| Protection                | Implementation  |
| ------------------------- | --------------- |
| Passwords                 | BCrypt          |
| Authentication            | JWT             |
| Refresh Sessions          | Redis           |
| OTP Storage               | Redis           |
| OTP Expiration            | Redis TTL       |
| Login Rate Limit          | IP + Username   |
| Registration Rate Limit   | IP              |
| Password Reset Rate Limit | IP              |
| Refresh Token             | HttpOnly Cookie |

</div>

### OTP Protection

Password recovery currently uses:

```text
OTP lifetime:       2 minutes
Maximum attempts:   5
Failed-attempt lock: 15 minutes
Token lifetime:     10 minutes
```

---

## 📁 Project Structure

```text
auth_service/
│
├── src/
│   └── main/
│       ├── java/com/traazu/auth_service/
│       │
│       └── resources/
│
├── frontend/
│   └── src/
│       ├── pages/
│       ├── lib/
│       └── App.tsx
│
├── pom.xml
└── .example.env
```

Important backend packages:

```text
configs/
controllers/
domain/
mappers/
redis/
repositories/
security/
services/
```

---

## ⚙️ Configuration

Create your environment configuration from:

```bash
cp .example.env .env
```

Typical configuration includes:

```env
DB_URL=
DB_USERNAME=
DB_PASSWORD=

REDIS_HOST=
REDIS_PORT=
REDIS_PASSWORD=

MAIL_USERNAME=
MAIL_PASSWORD=

JWT_SECRET=
```

> ⚠️ Never commit real credentials, SMTP passwords, database passwords, or JWT secrets.

---

## 🚀 Run Backend

Clone the repository:

```bash
git clone https://github.com/hossein-hb/auth_service.git
cd auth_service
```

Build:

```bash
./mvnw clean install
```

Run:

```bash
./mvnw spring-boot:run
```

Default backend port:

```text
http://localhost:8081
```

Required services:

```text
PostgreSQL
Redis
SMTP Server
```

---

## 💻 Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Production build:

```bash
npm run build
```

---

## 🔄 Basic Request Flow

```text
             ┌────────────┐
             │   Client   │
             └─────┬──────┘
                   │
                   ▼
             ┌────────────┐
             │   Login    │
             └─────┬──────┘
                   │
          ┌────────┴────────┐
          ▼                 ▼
    Access Token       Refresh Token
          │                 │
          │                 ▼
          │           HttpOnly Cookie
          │                 │
          ▼                 ▼
   Protected APIs      /auth/refresh
                              │
                              ▼
                       New Access Token
```

---

## 🧪 Development

Backend tests:

```bash
./mvnw test
```

Frontend development:

```bash
cd frontend
npm run dev
```

Check changes before committing:

```bash
git status
git diff
```

---

## 📜 License

This project is licensed under the **MIT License**.

See the [LICENSE](LICENSE) file for the full license text.

---

<div align="center">
  <sub>Released under the MIT License © 2026 Hossein HB</sub>
</div>
