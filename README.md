# 🐦 Kafka-Based Microblogging Platform

A real-time microblogging system powered by **Apache Kafka**, **FastAPI**, and **Docker**. It simulates publishing and consuming messages in real time, showcasing event-driven architecture using producers, consumers, and a RESTful API.

---

## ✨ Features

- 📤 Kafka producer for publishing micro-messages  
- 📥 Kafka consumer for real-time stream processing  
- 🌐 FastAPI gateway exposing REST endpoints  
- 🐳 Docker Compose for orchestration  
- 🔁 Decoupled, scalable message flow architecture  
- 🛠️ Modular microservice-style design

---

## 🛠️ Technologies Used

- **Language:** Python  
- **Framework:** FastAPI  
- **Streaming:** Apache Kafka, kafka-python  
- **Infrastructure:** Docker, Docker Compose, Zookeeper  
- **Testing:** Postman, curl  
- **Architecture:** Event-driven microservices

---

## 🚀 Getting Started

### ✅ Prerequisites

- Docker installed  
- Docker Compose installed

### ▶️ Run Locally

```bash
# Clone the repo
git clone https://github.com/nikhathfirdose1/Microblogging-Platform.git
cd Microblogging-Platform

# Build and start all services
docker-compose up --build
```

---

## 🔌 API Endpoints

### POST /publish

Send a message to the Kafka topic.

```bash
curl -X POST http://localhost:8000/publish \
     -H "Content-Type: application/json" \
     -d '{"user": "nikhath", "message": "Hello Kafka!"}'
```

**Sample Response:**

```json
{
  "status": "Message published"
}
```

---

### GET /messages

Retrieve all messages consumed from Kafka.

```bash
curl http://localhost:8000/messages
```

**Sample Response:**

```json
[
  {
    "user": "nikhath",
    "message": "Hello Kafka!",
    "timestamp": "2025-07-13T18:00:00Z"
  }
]
```

---

## 📂 Project Structure

```
Microblogging-Platform/
├── api-gateway/         # FastAPI app exposing endpoints
│   └── main.py
├── producer/            # Kafka producer script
│   └── producer.py
├── consumer/            # Kafka consumer script
│   └── consumer.py
├── docker-compose.yml   # Docker setup for Kafka, Zookeeper, services
└── README.md
```

---

## 🧪 Example Output

**Consumer Terminal:**

```
[Consumer] Received message from nikhath: Hello Kafka!
```

---

## 🎯 Learning Goals

- Build event-driven systems with Kafka  
- Use Docker Compose to orchestrate microservices  
- Understand producer-consumer workflows  
- Develop and expose APIs using FastAPI

---

## 📄 License

This project is for educational and demonstration purposes only.
