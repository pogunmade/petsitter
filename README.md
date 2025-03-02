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
- Docker 27.4.1 or later

### Start
From the project root directory
```shell
sudo docker compose up
```

### Swagger UI
From your browser
```
localhost:9090
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

### Postgres pgAdmin
From your browser
```
localhost:5050
```

Login
- Email Address: admin@example.com
- Password: password

Add database
- Right-click "Servers" in top left corner and select "Register" -> "Server"
- Name the Server
- Select the "Connection" tab and add the following:
  - Host name/address: db
  - Port: 5432
  - Maintenance database: pet-sitter
  - Username: pet-sitter-backend
  - Password: password

### Stop
From the project root directory
```shell
sudo docker compose down -v
```

### License
This demo is released under the MIT license.
