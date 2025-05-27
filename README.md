# Spring Data Queryable Encryption

This repository demonstrates how to integrate MongoDB Queryable Encryption with Spring Data MongoDB. It covers essential practices for creating encrypted collections and querying encrypted data efficiently using MongoDB’s Queryable Encryption capabilities.
The project includes sample code for:
- Creating encrypted collections and data encryption keys (DEKs).
- Running range and equality queries on encrypted fields.
- Working with MongoRepository and MongoTemplate in a secure, encrypted context.

## Getting Started

### Prerequisites

- Java 17 or higher (tested with Corretto 21)
- Maven
- MongoDB 8.0+ with Queryable Encryption support
- MongoDB crypt_shared library -> [`Automatic Encryption Lib`](https://www.mongodb.com/docs/v6.0/core/queryable-encryption/reference/shared-library/#download-the-automatic-encryption-shared-library)

## Running the Application

You can run the application in two ways:

### 1. Using IntelliJ or your IDE

- Open the project.
- Run the `SpringDataQueryableEncryptionApplication.java` class.

### 2. Using terminal Gradle

```bash
export MONGODB_URI="<YOUR_CONNECTION_STRING>" CRYPT_PATH="<PATH/TO/LIB/>"
mvn spring-boot:run
```

## Swagger UI (API Documentation)
Once the application is running, access:

```bash
http://localhost:8080/swagger-ui.html
```
Or (for some versions):

```bash
http://localhost:8080/swagger-ui/index.html
```
It includes endpoints for:

- /employees → Manage employees records

## Example Endpoints
Create a Employee
```bash
POST /employees
```

Find All employees

```bash
GET /employees
```

Find employees with age greater than a specified value
```bash
GET /employees/ages/greater-than?age=50
```

## Running the API with .http Files
This project includes ready-to-use .http files to test all API endpoints easily from your IDE (such as IntelliJ IDEA, Rider, or VSCode with REST Client plugin).

### Files included:
- employee_requests.http → Test all endpoints under /employees

### How to use:
1. Open the .http file in IntelliJ.
2. Click on the green "Run" icon next to any request.
3. You’ll see the response in a built-in HTTP client tab.
