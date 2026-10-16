# CloudForge

CloudForge is a project and file management platform built with Java and Spring Boot. It demonstrates secure REST API development, relational data persistence, file versioning, and event-driven activity tracking.

## Tech Stack

- **Backend:** Java, Spring Boot, Spring Security
- **Authentication:** JWT, BCrypt
- **Database:** MySQL, Spring Data JPA, Hibernate
- **Messaging:** Apache Kafka
- **Build Tool:** Maven

## Features

- JWT-based authentication
- Role-based access control
- Project management REST APIs
- File management and versioning
- Optimistic locking to help prevent conflicting updates
- Activity event publishing and processing with Kafka
- Activity history with pagination
- Request validation and centralized exception handling

## Architecture

The application uses a layered structure with controllers, services, repositories, and database entities. Kafka is used to process selected activity events asynchronously.

## Local Setup

1. Install a compatible JDK, Maven, MySQL, and Docker if running Kafka locally.
2. Create a MySQL database named `cloudforge`.
3. Configure the required database credentials and JWT secret through environment variables.
4. Configure Kafka if you want to run the activity-event functionality.
5. Run the application using Maven.

```bash
mvn spring-boot:run
```

## Project Status

This project is under active development. More features, tests, documentation, and deployment improvements may be added over time.