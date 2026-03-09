# AddressBook

A full-stack **Address Book Management Application** built with **Spring Boot** and **React**.

This project demonstrates a modern full-stack architecture with a clean separation between backend services and frontend UI. The application allows users to create, view, update, and delete contacts through a REST API and a responsive web interface.

The system is designed as a lightweight contact management platform where contact information such as name, address, phone number, and email can be stored and managed efficiently.

---

# Architecture Overview

The repository follows a **monorepo structure** containing both backend and frontend applications.

```
AddressBook
├── server/     → Spring Boot REST API
└── client/     → React Frontend Application
```

### Backend (server)

The backend is implemented using **Spring Boot** and exposes a REST API responsible for managing contacts.

Responsibilities:

* Contact CRUD operations
* REST API endpoints
* Business logic
* In-memory data storage
* Request validation
* Structured API responses

Technology Stack:

* Java 21
* Spring Boot
* Maven
* RESTful APIs

Key API Endpoints:

```
GET     /api/contacts
GET     /api/contacts/{id}
POST    /api/contacts
PUT     /api/contacts/{id}
DELETE  /api/contacts/{id}
```

The backend currently uses an **in-memory collection** to store contacts. This keeps the system simple while demonstrating the full API lifecycle.

---

### Frontend (client)

The frontend is a modern **React + TypeScript** application that consumes the backend API.

Responsibilities:

* Display contact list
* Add new contacts
* Edit existing contacts
* Delete contacts
* Provide responsive user interface

Technology Stack:

* React
* TypeScript
* Vite
* SWC
* TailwindCSS
* SCSS
* Axios

The UI communicates with the backend through the REST API.

---

# Contact Model

Each contact in the system contains the following information:

```
id
firstName
lastName
address
city
state
zip
phone
email
```

---

# Repository Workflow

Development follows a **stacked feature branch workflow** where each branch builds upon the previous one.

```
main
 └── setup
     └── utilities
         └── api
             └── react-app
                 └── finalize
```

Branch responsibilities:

| Branch    | Purpose                             |
| --------- | ----------------------------------- |
| setup     | Base project initialization         |
| utilities | Shared utilities and infrastructure |
| api       | Backend REST API implementation     |
| react-app | React UI and API integration        |
| finalize  | Polishing and production readiness  |

The final application is merged back into the **main branch**.

---

# Running the Project

## Backend

Navigate to the server directory.

```
cd server
mvn spring-boot:run
```

The API will start on:

```
http://localhost:8080
```

---

## Frontend

Navigate to the client directory.

```
cd client
npm install
npm run dev
```

The frontend will start on:

```
http://localhost:5173
```

---

# Example API Call

Fetch all contacts:

```
curl http://localhost:8080/api/contacts
```

Create a contact:

```
curl -X POST http://localhost:8080/api/contacts \
-H "Content-Type: application/json" \
-d '{
  "firstName":"John",
  "lastName":"Doe",
  "city":"New York",
  "phone":"9999999999",
  "email":"john@example.com"
}'
```

---

# Design Goals

The project was structured to demonstrate:

* Clean separation between frontend and backend
* Layered backend architecture (Controller → Service)
* RESTful API design
* Modern React application structure
* Git feature branch workflow
* Scalable project layout

---

# Future Improvements

Possible future enhancements include:

* Database integration (PostgreSQL / MySQL)
* Authentication and authorization
* Contact search and filtering
* Pagination support
* Docker containerization
* Deployment to cloud platforms
