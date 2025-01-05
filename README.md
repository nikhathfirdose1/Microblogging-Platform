# Microblogging Platform

A distributed microblogging platform implemented using Java, Spring Boot, Kafka, and Zookeeper, designed for fault-tolerant data replication, secure communication, and efficient message processing.

## Features

- **Distributed Architecture:** Built using Kafka and Zookeeper for efficient, fault-tolerant message replication across multiple nodes.
- **Secure Communication:** Integrated RSA-based authentication with OpenSSL, including a simulated PKI for certificate management and secure user validation.
- **High Performance:** Capable of handling over 5,000+ posts with dynamic server scalability and efficient message delivery.
- **RESTful API:** Exposed REST APIs for creating, retrieving, and managing microblog posts.

## Tech Stack

- **Backend:** Java, Spring Boot
- **Messaging & Coordination:** Kafka, Zookeeper
- **Security:** RSA, OpenSSL, SSL/TLS
- **API Design:** REST APIs

## Architecture

The platform follows a distributed architecture where:
- **Kafka** handles message brokering and event distribution.
- **Zookeeper** manages node coordination and leader election.
- **Spring Boot** provides the RESTful API for client interaction.
- **RSA and OpenSSL** secure communications and user authentication.

## Setup Instructions

### Prerequisites
- Java 11+
- Apache Kafka & Zookeeper
- OpenSSL

### Steps
1. **Clone the repository:**
   ```bash
   git clone https://github.com/github-school/microblog-platform.git
   cd microblog-platform
   ```
2. **Start Kafka and Zookeeper:**
   ```bash
   zookeeper-server-start.sh config/zookeeper.properties
   kafka-server-start.sh config/server.properties
   ```
3. **Generate RSA Keys (for OpenSSL):**
   ```bash
   openssl genpkey -algorithm RSA -out private_key.pem
   openssl req -new -x509 -key private_key.pem -out cert.pem -days 365
   ```
4. **Build the Project:**
   ```bash
   mvn clean install
   ```
5. **Run the Application:**
   ```bash
   java -jar target/microblog-platform.jar
   ```

## Usage
- **Create a Post:** `POST /api/posts`
- **Retrieve Posts:** `GET /api/posts`
