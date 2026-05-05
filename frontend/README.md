# Freelancy – Freelance Management Platform

<p align="center">
  <img src="https://img.shields.io/badge/Angular-Frontend-red?logo=angular" />
  <img src="https://img.shields.io/badge/Spring%20Boot-Backend-green?logo=springboot" />
  <img src="https://img.shields.io/badge/Keycloak-Auth-blue?logo=keycloak" />
  <img src="https://img.shields.io/badge/Docker-DevOps-blue?logo=docker" />
  <img src="https://img.shields.io/badge/MySQL-Database-orange?logo=mysql" />
</p>

---

## Overview

This project was developed as part of the **PIDEV – 4th Year Engineering Program** at **Esprit School of Engineering** (Academic Year 2025–2026).

**Freelancy** is a full-stack web application built on a **microservices architecture**, designed to connect freelancers with clients.  
It provides intelligent skill-based matching, project management, task planning, technical challenges, blog publishing, subscription-based premium services, AI-powered smart contract generation, and an anti-cheat exam & quiz system.

The platform creates a collaborative ecosystem where freelancers enhance their visibility, improve their skills through challenges and exams, publish blog articles, and access premium features via subscription plans.

---

## Architecture Overview

Freelancy is built on a **microservices architecture** with the following infrastructure components:

| Component | Role |
|---|---|
| **API Gateway** | Single entry point routing requests to all microservices |
| **Eureka Server** | Service discovery and registration |
| **Keycloak** | Authentication & Authorization (JWT / OAuth2) |
| **Angular Frontend** | Unified SPA consuming all microservices via the API Gateway |
| **MySQL** | Relational database per microservice |

> All microservices register with Eureka and expose their APIs through the API Gateway. The Angular frontend communicates exclusively via the Gateway, secured with JWT tokens issued by Keycloak.

---

## Microservices

### 1. 👤 User Management
Handles user authentication, roles (Admin / Client / Freelancer), and profile data.  
Serves as the identity backbone consumed by all other microservices via `userId`.

---

### 2. 🧠 Skill Management

Manages freelancer competencies, experience levels, and availability.  
Provides the core data feed for the AI Matching engine.

**Features:**
- Add, update, and delete skills per freelancer
- Skill levels: Beginner / Intermediate / Expert
- Experience years and availability tracking
- Skill association via `userId`

**Integration:**
- Exposes skill data to the Matching Service
- Communicates with User Service via `userId`
- Available for AI profile analysis

---

### 3. 🎯 AI Matching

The intelligent core of the platform. Automatically pairs freelancers with projects using a **multi-criteria scoring algorithm**.

**Scoring Breakdown:**

| Criterion | Weight |
|---|---|
| Skills | 40% |
| Availability | 20% |
| Experience | 20% |
| Education | 10% |
| Challenges | 10% |

**Features:**
- Automatic match generation between freelancers and projects
- `scoreFinal` computation and freelancer ranking per project
- Match statuses: `PENDING` → `ACCEPTED` / `REJECTED`
- Accept / Decline controls from the Angular frontend

**Matching Process:**
1. Fetch freelancer profile (skills, experience, availability)
2. Fetch project requirements
3. Compute multi-criteria score
4. Create match with `PENDING` status
5. Freelancer or client accepts / rejects

**Integration:**
- Feign clients for: Skill Management, Project, User, Education, Challenge, and Availability services
- Eureka-registered for service discovery

---

### 4. 📁 Project Management

Enables clients to post and manage project offers.  
Consumed by the Matching Service to retrieve project requirements.

---

### 5. 📝 Smart Contract Management

Automates the full contract lifecycle using **AI generation** (Claude API) after a client accepts a freelancer proposal.

**Contract Lifecycle:**

1. **Auto-generation** — AI generates a full contract including: project description, clauses, estimated duration, scope, complexity, and milestone breakdown
2. **Smart Summary** — AI generates a plain-language summary for both parties
3. **Validation & Notification** — Client validates; freelancer receives the contract + summary by email
4. **Collaborative Editing** — Freelancer can propose modifications to milestones and clauses; each change is logged and sent to the client
5. **Client Decision:**
   - ✅ Accepted → both parties sign → contract becomes **Active**
   - ❌ Refused → contract marked as **Closed**
6. **PDF Generation** — Signed contract exported as a downloadable PDF
7. **Milestone Execution** — Project runs phase by phase; each milestone triggers payment and status update
8. **Audit Trail** — Full modification history for transparency and dispute resolution

---

### 6. 📅 Event Management

Manages standalone user events with automated email notifications and AI-based timing suggestions.

**Features:**
- Create, update, and delete personal/professional events
- Event statuses: `SUCCESS` / `LATE` / `PENDING`
- Automated email notifications on: creation, completion, and late detection
- AI-powered optimal timing suggestions

**Integration:**
- Communicates with User Service for identification
- Email delivery via SMTP
- APIs exposed for Project Service and AI modules

---

### 7. 🗓️ Planning

Manages global planners and associated tasks, displayed as a calendar with quick access via QR code.

**Features:**
- Create and manage plannings with linked tasks
- Task statuses: `DONE` / `LATE` / `TODO`
- QR code generation for instant planning access
- Email notifications for completed or overdue tasks
- AI integration for delay prediction

**Integration:**
- Exposes calendar + QR code APIs to the Angular frontend
- Communicates with User Service
- Complements the Event module (Event = one-off events; Planning = global agenda)

---

### 8. ✍️ Blog Management

Manages platform publications (posts): creation, editing, retrieval, and deletion.

**Features:**
- Create posts with title, content, and author info
- Full CRUD on posts
- Author association via `authorId`
- Post metadata: creation date, title, content

**Integration:**
- Communicates with User Service to resolve author details
- APIs exposed through API Gateway
- Registered in Eureka

---

### 9. 📊 Blog Analytics

Tracks and measures the performance of blog posts through real-time metrics.

**Tracked Metrics:**
- `total_posts`
- `views`
- `likes`
- Custom indicators

**Features:**
- Upsert metrics on user actions (post viewed, liked, etc.)
- Retrieve all statistics or a specific metric
- Real-time availability via API

**Integration:**
- Consumes Blog Management Service data
- Exposed via API Gateway and registered in Eureka

---

### 10. 🏆 Challenge Management

Hosts technical challenges for freelancers to improve their skills and gain visibility on the platform.  
Challenge scores contribute to the AI Matching evaluation (10% weight).

---

### 11. 📋 ExamQuiz Service — Port `8150`

A dedicated Spring Boot microservice responsible for managing the **full exam and quiz lifecycle**, including real-time proctoring and anti-cheat enforcement.

**Exam Management**
- Create, update, and manage exams with configurable settings: duration, passing score, maximum attempts, scheduling, and exam type

**Question & Answer Handling**
- Supports multiple question types
- Manages answer submissions per attempt

**Attempt Tracking**
- Records candidate exam attempts, submitted answers, and session statuses throughout the exam lifecycle

**Scoring & Results**
- Automatically evaluates submissions and generates detailed result reports per candidate

**Anti-Cheating & Proctoring**
- Built-in proctoring system detecting violations: tab switching, phone detection, looking away
- Configurable violation thresholds per violation type
- Cheating events logged and stored
- Auto-submission enforced upon threshold breach

**Admin Live Monitoring**
- Real-time dashboard for administrators to track active exam sessions and candidate behavior

**Exam Participation Management**
- Controls which candidates can access specific exams
- Tracks participation status per candidate

**Technical Details:**

| Property | Value |
|---|---|
| Framework | Spring Boot |
| Database | MySQL (`exam_quiz_db`) |
| Port | `8150` |
| Service Discovery | Eureka Client |
| API Docs | Swagger UI (`/swagger-ui.html`) |
| Inter-service | Feign Client → User Service |

---

### 12. 💳 Subscription

Controls user access levels and monetization via subscription plans.

**Subscription Types:** `FREE` / `VIP`  
**Statuses:** `ACTIVE` / `EXPIRED`

**Lifecycle:**
1. Subscription creation
2. Activation after successful payment
3. Automatic expiration based on `endDate`
4. Admin-managed modification or deletion

**Integration:**
- Verifies users with User Service
- Triggered by Payment Service upon successful transaction
- Provides VIP access check APIs to Project, Matching, and AI modules

---

### 13. 💰 Payment

Handles all payment operations for subscription upgrades (FREE → VIP).

**Features:**
- Payment processing and transaction recording
- Payment statuses: `SUCCESS` / `FAILED`
- User association via `userId`

**Payment Flow:**
1. User requests VIP upgrade
2. Payment processed
3. On success: Payment Service calls Subscription Service → VIP activated
4. Confirmation email sent with: amount paid, subscription type, expiration date

**Integration:**
- Communicates with Subscription Service
- Uses Keycloak JWT for user identification
- Email delivery via SMTP

---

## Tech Stack

### Frontend
- Angular · TypeScript · HTML5 / CSS3
- Bootstrap · Tailwind CSS

### Backend
- Spring Boot · Java
- Spring Security · JPA / Hibernate
- MySQL · Keycloak (OAuth2 / JWT)
- OpenFeign (inter-service communication)

### Infrastructure
- Eureka Server (Service Discovery)
- API Gateway
- Docker · Docker Compose
- GitHub Actions (CI/CD)
- Nginx (Reverse Proxy)

---

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+ / Angular CLI
- Docker & Docker Compose
- Keycloak instance running

### Clone the Repository
```bash
git clone https://github.com/username/Esprit-PIDEV-4SAE3-2026-Freelancy.git
cd Esprit-PIDEV-4SAE3-2026-Freelancy
```

### Run with Docker Compose
```bash
docker-compose up --build
```

### Backend (individual microservice)
```bash
cd <microservice-folder>
mvn clean install
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
ng serve
```

### Application URLs

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Keycloak Admin | http://localhost:8180 |
| Skill Management | http://localhost:8086 |
| ExamQuiz Service | http://localhost:8150 |
| ExamQuiz Swagger | http://localhost:8150/swagger-ui.html |

---

## Contributors

| Name | Modules |
|---|---|
| Arij Achach | Skill Management · AI Matching |
| Sirine Bouden | Project Management · Smart Contract Management |
| Ameni Benzaghdene | Challenge Management · ExamQuiz Service |
| Mohamed Jaffel | Event Management · Planning |
| Malek Ben Said | User Management · Payment · Subscription |
| Mohamed Wahebi | Blog Management · Blog Analytics |

**Supervisors:**  
Ms. Leila Bendhief – PIDEV Module  
Ms. Nadine Maazoun – PIDEV Module

---

## Academic Context

> Developed at **Esprit School of Engineering – Tunisia**  
> Module: **PIDEV** | Level: 4th Year Engineering | Academic Year: **2025–2026**

This project applies full-stack development, microservices architecture, DevOps practices, secure authentication, AI integration, and modern software engineering methodologies.

---

*Esprit School of Engineering – PIDEV 4SAE3 | 2025–2026*
