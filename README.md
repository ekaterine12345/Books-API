# Books API

## Overview
Books API is a backend application built with Java and Spring Boot that allows users to browse, purchase, and download books in PDF format.
The system implements authentication and role-based authorization using JWT, supporting both administrators and standard users with different levels of access.

## Features
* User registration and authentication (JWT-based)
* Role-based access control (ADMIN, USER)
* Book management (CRUD operations)
* Shopping cart functionality
* Book purchasing system
* Secure PDF download for purchased books
* API documentation via Swagger

## User Roles: 
### ADMIN:
Administrators have full control over book management:

* Add new books (including PDF upload)
* Update book details and files
* Delete books
* View all books

### USER:
Standard users can:
* Register and log in
* Browse all available books
* Add books to cart
* Remove books from cart
* Clear the entire cart
* Purchase a single book
* Purchase all books in the cart
* Download purchased books as PDF files

## Tech Stack
* Java 21
* Spring Boot 3.2.5
* Maven
* Spring Web
* Spring Data JPA
* Hibernate
* MySQL
* Spring Security
* JWT (jjwt)
* Lombok
* MapStruct
* Bean Validation
* Swagger / OpenAPI (springdoc)

## API Documentation
Swagger UI is available for testing endpoints:
``` 
http://localhost:8080/swagger-ui.html
```

## Getting Started
### Prerequisites
1) Java 21
2) Maven
3) MySQL

### Run the Application

```
mvn clean install
mvn spring-boot:run
```


## Future Improvements
* Payment integration
* Docker support
