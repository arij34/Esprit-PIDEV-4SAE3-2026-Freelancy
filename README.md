 
# Freelancy – Freelance Management Platform

## Overview

This project was developed as part of the PIDEV – 4th Year Engineering Program at **Esprit School of Engineering** (Academic Year 2025–2026).

Freelancy is a full-stack web application designed to connect freelancers with clients.  
It enables project posting, intelligent skill-based matching, task management, participation in technical challenges, blog content sharing, and subscription-based premium services.

The platform aims to create a collaborative ecosystem where freelancers can enhance their visibility, improve their skills through challenges, publish blog articles, and access premium features via subscription plans.

---

## Features

### Core Platform
- User authentication and role management (Admin / Client / Freelancer)
- Project posting and management
- Skill-based freelancer matching
- Task tracking and progress monitoring

### Engagement System
- Technical challenges for freelancers
- Blog module for publishing and sharing articles

### Monetization
- Subscription system with premium features

### Security
- Secure authentication using Keycloak
- Role-based access control

---

## Tech Stack

### Frontend
- Angular
- TypeScript
- HTML5 / CSS3
- Bootstrap
- Tailwind CSS

### Backend
- Spring Boot
- Java
- Spring Security
- JPA / Hibernate
- MySQL
- Keycloak (Authentication & Authorization)

### DevOps
- Docker
- Docker Compose
- GitHub Actions (CI/CD)
- Nginx (Reverse Proxy)
- Environment-based configuration

---

## Architecture

The application follows a layered and modular architecture:

- Controller Layer – REST API endpoints
- Service Layer – Business logic
- Repository Layer – Data access
- Database Layer – MySQL
- Authentication Server – Keycloak

The frontend communicates with the backend via REST APIs secured using JWT tokens.

---

## Deployment (Optional)

The application can be deployed using:

- GitHub Actions (CI/CD Pipeline)
- Docker containers
- Cloud platforms such as Vercel, Render, Railway or DigitalOcean

---

## Contributors
- Arij Achach
- Sirine Bouden
- Ameni Benzaghdene
- Mohamed Jaffel
- Malek Ben Said
- Mohamed Wahebi


Supervisors:  
Ms Leila Bendhief – PIDEV Module  
Ms Nadine Maazoun – PIDEV Module  

---

## Academic Context

Developed at **Esprit School of Engineering – Tunisia**

Module: PIDEV  
Level: 4th Year Engineering  
Academic Year: 2025–2026  

This project applies full-stack development, DevOps practices, secure authentication, and modern software engineering methodologies.

---

## Getting Started

### Backend Setup

```bash
git clone https://github.com/username/Esprit-PIDEV-4SAE3-2026-Freelancy.git
cd backend
mvn clean install
mvn spring-boot:run
Frontend Setup
cd frontend
npm install
ng serve

Application URL:

http://localhost:4200

Developed at **Esprit School of Engineering – Tunisia** 
PIDEV – 4SAE3 | 2025–2026
