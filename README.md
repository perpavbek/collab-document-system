# Collaborative Editing System

## Project Overview

This project is a **Collaborative Editing System**, implemented using **Spring Boot** for backend microservices and **React** for the frontend. The system allows multiple users to edit documents in real-time, maintain version history, and manage user profiles.

### Microservices Architecture

The project consists of three microservices:

1. **User Management Service**
   - User registration
   - User authentication
   - User profile management

2. **Document Editing Service**
   - Create new documents
   - Edit documents collaboratively in real-time
   - Track changes

3. **Version Control Service**
   - Maintain document version history
   - Revert to previous versions
   - Track user contributions

An **API Gateway** is used to route requests between services and expose a unified REST API to the frontend.

### Technologies Used

- **Backend:** Java, Spring Boot, Spring Cloud, Spring Data JPA, OpenFeign, Lombok, JWT
- **Frontend:** React, Vite, AntDesign, TailwindCSS, Zustand
- **Database:** PostgreSQL
- **Testing:** JUnit for unit and integration tests
- **Build & Run:** Docker Compose for containerized services

---

## Getting Started

1. Clone the repository:

```bash
git clone https://github.com/perpavbek/collab-document-system.git
cd collab-document-system
```

2. Preparing Environment

Before running the services, create a `.env` file based on the provided example:

```bash
cp .example.env .env
```
3. Build and run the services in detached mode:
```bash
docker-compose up --build -d
```

 **Note**: Services start asynchronously. After running the above command, wait 30–60 seconds to ensure all services are fully initialized before accessing the frontend or making API requests.

3. Access the frontend:
```
http://localhost:3000
```
4.  API Gateway is available at:
```
http://localhost:8080
```