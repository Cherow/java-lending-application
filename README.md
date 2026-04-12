
# 🏦 Loan Lending Backend Application

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-brightgreen)
![Build](https://img.shields.io/badge/build-Maven-blue)
![Database](https://img.shields.io/badge/DB-MySQL%20%7C%20PostgreSQL-lightgrey)
![Architecture](https://img.shields.io/badge/Architecture-Modular%20%7C%20Event--Driven-purple)
![Status](https://img.shields.io/badge/status-Active-success)

---

## 🚀 Overview

This is a loan lending backend system built using Java Spring Boot.

It supports the full loan lifecycle, including:

- Product configuration
- Customer management
- Loan creation and lifecycle management
- Fee calculation engine
- Event-driven notifications

The system is designed with:

- Clean architecture principles
- Loose coupling between modules
- Scalability and maintainability in mind

---

## 🧠 Architecture

The application follows a microservice architecture, where each domain is separated into its own module.

### Core Modules

- **Product Module** → Defines loan products and fee configurations
- **Loan Module** → Handles loan creation and lifecycle
- **Fee Engine** → Calculates applicable loan fees
- **Customer Module** → Manages customer data
- **Notification Module** → Handles events and notifications

---

## ⚙️ Getting Started

Follow the steps below to set up and run the application locally.

---

## 📋 Prerequisites

Ensure you have the following installed:

- **Java 17+**
- **Maven 3.8+**
- **MySQL** or **PostgreSQL**
- **Apache ActiveMQ**
- **Git**
- IDE (IntelliJ IDEA recommended)

---

## 📥 Clone the Repository

```bash
git clone https://github.com/Cherow/java-lending-application.git
cd java-lending-application
````

---

## 🗄️ Database Setup

### 1. Create Database

#### MySQL

```sql
CREATE DATABASE lending_application;
```
Establish a connection to the database using this configuration
```yaml
 datasource:
    url: jdbc:mysql://localhost:3306/lending_application
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

```
---

### 2. Configure Application Properties

Update:

```text
src/main/resources/application.yml
```

### Example (MySQL + ActiveMQ)

```yaml

  datasource:
    url: jdbc:mysql://localhost:3307/lending_application
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false


settings:
  amq:
    broker:
      url: amqp://127.0.0.1:5672
    username: admin
    password: admin
    concurrency: 5
    connection:
      pool:
        cache: 10


```

---

## 📬 ActiveMQ Setup

This application uses **ActiveMQ** for event-driven messaging.
Make sure **ActiveMQ is up and running before starting the application**, otherwise queue publishing and listeners will fail.

### Default ActiveMQ Settings

* **Broker URL:** `amqp://127.0.0.1:5672`
* **Username:** `admin`
* **Password:** `admin`
* **Queue Name:** `loan-notification-queue`

### ActiveMQ Web Console

You can access the ActiveMQ web console at:

```text
http://127.0.0.1:8161
```

Default credentials:

```text
Username: admin
Password: admin
```

### Important Note

* `http://127.0.0.1:8161` is the **web console**
* `amqp://127.0.0.1:5672` is the **broker URL used by the application**

The application connects to the broker, not the web console.

---

## 📦 Install Dependencies

Download all dependencies:

```bash
mvn clean install
```

---

## ▶️ Run the Application

### Before starting the app

Make sure the following are running:

* MySQL database
* Customer service
* Product service
* ActiveMQ broker
* Notification Service
* Loans Service

### Option 1: Run using Maven

```bash
mvn spring-boot:run
```

---

## 🌐 Access the Application

Once running, the app will be available at different ports


---

## 📘 API Documentation (Postman)

To access API documentation, use the link below:

```text
https://documenter.getpostman.com/view/20677309/2sBXitD89x
```

---

## 🔐 Environment Variables (Optional)

You can externalize configs using environment variables.

### Linux / Mac

```bash
export DB_URL=jdbc:mysql://localhost:3306/lending_application
export DB_USERNAME=root
export DB_PASSWORD=your_password
export AMQ_BROKER_URL=amqp://127.0.0.1:5672
export AMQ_USERNAME=admin
export AMQ_PASSWORD=admin
```

### Windows (PowerShell)

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/lending_application"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:AMQ_BROKER_URL="amqp://127.0.0.1:5672"
$env:AMQ_USERNAME="admin"
$env:AMQ_PASSWORD="admin"
```

---

## 🧪 Running Tests

```bash
mvn test
```

---

## ⚠️ Common Issues & Fixes

### ❌ Port already in use

Change port in `application.yml`:

```yaml
server:
  port: 8082
```

---

### ❌ Database connection failed

Ensure:

* Database server is running
* Database exists
* Credentials are correct
* Port is correct

---

### ❌ ActiveMQ connection failed

Ensure:

* ActiveMQ is running before the application starts
* The broker URL is correct: `amqp://127.0.0.1:5672`
* Username and password are correct
* You are not using the web console URL as the broker URL

Correct broker URL:

```text
amqp://127.0.0.1:5672
```

Incorrect broker URL:

```text
http://127.0.0.1:8161
```

---

### ❌ Java version mismatch

Check Java version:

```bash
java -version
```

Ensure it matches the project requirement.

---

### ❌ Maven build issues

Try:

```bash
mvn clean install -U
```

---


## 🛠️ Tech Stack

* Java 17
* Spring Boot
* Spring Data JPA
* Hibernate
* Maven
* MySQL / PostgreSQL
* ActiveMQ
* Postman API Documentation

---


