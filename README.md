
# Scala ZIO2 HTTP Microservice

Scalable Scala Microservice with ZIO 2, http4s, and GraphQL

This microservice harnesses the power of ZIO 2, a next-generation Scala library for type-safe, asynchronous, and concurrent programming. Built on a composable effect system, ZIO simplifies complex workflows while delivering robust error handling, resource safety, and high performance—making it ideal for resilient, scalable backend systems.

The HTTP layer is powered by http4s, a purely functional, modular HTTP toolkit for Scala. Seamlessly integrated with ZIO, http4s offers elegant routing, middleware support, and efficient streaming capabilities, enabling high-performance API development with minimal boilerplate.

For flexible and optimized data querying, the service leverages Caliban, a functional GraphQL library for Scala. With GraphQL, clients can precisely request the data they need—such as filtered product listings with pagination or nested product details—reducing over-fetching and improving responsiveness.

Combined, ZIO 2, http4s, and Caliban form a cutting-edge stack for building maintainable, type-safe microservices that excel at handling intricate business logic, concurrent operations, and evolving data requirements.

A production-ready microservice template built with:
- Scala 3.7.1
- ZIO 2.0.10
- HTTP4S 0.23.10
- Caliban 2.0.2 (GraphQL)
- Doobie 1.0.0-RC2 (Database)

## Features

- Healthcheck API endpoint
- Product GraphQL API
- PostgreSQL database integration
- Configuration management
- Structured logging
- Database migrations (Flyway)

## Getting Started

### Prerequisites

- JDK 21+
- sbt 1.11.3+
- PostgreSQL 12+

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/vinodjagwani/scala-zio2-https2-microservice.git
   cd scala-zio2-https2-microservice
   ```

2. Configure the application:

   ```bash
   cp src/main/resources/application.conf
   ```

## Running the Service

### Start the server:

```bash
sbt run
```

The service will be available at:

- HTTP: http://localhost:8080
- Healthcheck: http://localhost:8080/health
- GraphQL: http://localhost:8080/graphql
- GraphQL Playground: http://localhost:8080/graphiql

## API Documentation

### Healthcheck

- `GET /health` - Service health status - http://127.0.0.1:8080/health

### GraphQL API

- `POST /graphql` - GraphQL endpoint


#### ✅ 1. Delete Product

```json
{
  "query": "mutation($id: Long!) { deleteProduct(id: $id) }",
  "variables": {
    "id": 1
  }
}
```

#### ✅ 2. Get Product by ID

```json
{
  "query": "query($id: Long!) { productById(id: $id) { id name description } }",
  "variables": {
    "id": 1
  }
}
```

#### ✅ 3. Get All Products with Pagination (Optional)

```json
{
  "query": "query($offset: Int, $limit: Int) { allProducts(offset: $offset, limit: $limit) { id name description } }",
  "variables": {
    "offset": 0,
    "limit": 10
  }
}
```

## Project Structure

```
src/
├── main/
│   ├── resources/             # Configuration files
│   ├── scala/
│   │   ├── api/                # HTTP routes
│   │   ├── config/             # Configuration 
│   │   ├── db/                 # Database repository and Domain models
│   │   ├── errors/             # Error handling
│   │   ├── service/            # Business Logic
│   │   ├── utils/              # Helper class and methods
│   │   └── Application.scala/  # Main application class for running application
```

Thank you for checking out this zio2 http4s based apis!