# Split Bill API

A RESTful Split Bill application built with Java and Spring Boot. This application allows users to create bill groups, manage expenses, split expenses among participants, and calculate settlement transfers.

## Tech Stack

- Java 17
- Spring Boot 4.0.10
- Spring Data JPA / Hibernate
- MySQL 8.4
- Maven
- Docker & Docker Compose
- JUnit 5 & Mockito

## Features

- Create a bill group with multiple participants.
- Retrieve all bill groups.
- Retrieve bill group details.
- Add expenses to a group.
- Split expenses equally among selected participants.
- Retrieve group expenses.
- Calculate settlement transfers between participants.
- Calculate service charge based on GitHub username.

## Getting Started

### Prerequisites

- Java 17+
- Maven
- MySQL 8+
- Docker and Docker Compose (for containerized execution)

### Run Locally

1. Create a MySQL database:

```sql
CREATE DATABASE split_bill;
```

2. Configure database credentials in `src/main/resources/application.properties`:

```properties
server.port=4110
spring.datasource.url=jdbc:mysql://localhost:3306/split_bill
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

3. Run the application:

```bash
mvn spring-boot:run
```

The API will be available at:

`http://localhost:4110`

## Run with Docker

Build and start the application and MySQL database:

```bash
docker compose up --build
```

The API will be available at:

`http://localhost:4110`

Stop the containers:

```bash
docker compose down
```

The MySQL data is persisted using a Docker named volume.

## API Documentation

Base URL: `http://localhost:4110`

### 1. Create Group

**POST** `/api/groups`

Example:

```bash
curl http://localhost:4110/api/groups
```

Request:

```json
{
  "name": "Bali Trip",
  "participants": [
    "Kevin",
    "Andi",
    "Budi"
  ]
}
```

### 2. Get All Groups

**GET** `/api/groups`

Example:

```bash
curl http://localhost:4110/api/groups
```

### 3. Get Group Details

**GET** `/api/groups/{groupId}`

Example:

```bash
curl http://localhost:4110/api/groups/1
```

### 4. Add Expense

**POST** `/api/groups/{groupId}/expenses`

Example:

```bash
curl http://localhost:4110/api/groups/1/expenses
```

Request:

```json
{
  "description": "Dinner",
  "amount": 300000,
  "paidByParticipantId": 1,
  "splitParticipantIds": [1, 2, 3]
}
```

The expense amount is divided equally among the selected participants.

### 5. Get Group Expenses

**GET** `/api/groups/{groupId}/expenses`

Example:

```bash
curl http://localhost:4110/api/groups/1/expenses
```

### 6. Get Settlement

**GET** `/api/groups/{groupId}/settlement`

Example:

```bash
curl http://localhost:4110/api/groups/1/settlement
```

The response includes total expenses, service charge percentage, service charge amount, and settlement transfers.

## Service Charge Calculation

GitHub Username: `kevinaxeln`

The service charge percentage is calculated dynamically using the sum of the ASCII values of the lowercase GitHub username characters, modulo 10.

```text
service_charge_pct = sum(username character values) % 10
```

For `kevinaxeln`:

```text
ASCII sum = 1077
Service charge percentage = 1077 % 10 = 7%
```

The service charge amount is calculated as:

```text
service_charge_amount = total_group_expenses * service_charge_pct / 100
```

All monetary calculations use `BigDecimal` to maintain precision.

## Testing

Run the tests using:

```bash
mvn test
```

The settlement calculation is tested using JUnit 5 and Mockito.

## Submission Question

The hardest design decision was deciding whether a bill group should contain one combined expense or multiple individual expenses. I chose to allow multiple expenses per group, with each expense tracking who paid and which participants share the cost. This makes the application more flexible for real-life scenarios where different people pay for different activities or purchases. The trade-off was a more complex database structure with separate expense and expense split entities, but it provides better transaction tracking and more accurate settlement calculations.
## Author

Kevin Axel

GitHub: [KevinAxelN](https://github.com/KevinAxelN)
