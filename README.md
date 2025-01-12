# Pet Sitter API

Demo Spring Boot implementation of the Pet Sitter API from _"Designing APIs with Swagger and OpenAPI"_ Ponelat, J.S., 
Rosenstock, L.L. (2022).

We assume the domain model and a slightly modified version of the OpenAPI contract described at the end of Part 2. The
focus of this implementation is software design. In particular how to use concepts from Domain Driven Design to connect
the domain model and implementation.

Note, this application is intended for demonstration purposes. It is not a production ready application, nor is it an
enterprise grade application. The implementation reflects this. For further background [see](https://www.linkedin.com/pulse/managing-complexity-contract-first-api-peter-ogunmade-whgoc "Linkedin article").

__Disclaimer:__ there is no association with the book or its authors. Any errors are entirely self-contained.

### Minimum requirements
- JDK 17 or later
- Apache Maven 3.6.3 or later

_(If Docker is preferred see containerization branch.)_

### Run
From the project root directory
```shell
mvn spring-boot:run
```

### Swagger UI
From your browser
```
localhost:8080/swagger-ui
```

Pre-installed API Users

- Alice The Admin
  - email: admin@example.com
  - password: password

- Owen The Pet Owner
  - email: pet-owner@example.com
  - password: password

- Sally The Pet Sitter
  - email: pet-sitter@example.com
  - password: password

### OpenAPI Contract 
From your browser
```
localhost:8080/api-docs.yaml
```

### H2 Console
From your browser
```
localhost:8080/h2-console
```
- Driver Class: org.h2.Driver
- JDBC URL: jdbc:h2:mem:pet-sitter-db
- User Name: sa
- Password: password

### License
This demo is released under the MIT license.
