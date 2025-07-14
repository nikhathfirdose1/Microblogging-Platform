# 🐦 Kafka-Based Microblogging Platform

A real-time microblogging system powered by **Apache Kafka**, **Flask**, and **Docker**. It simulates message publishing and consumption in real-time using producers, consumers, and a RESTful API gateway.

---

## ✨ Features

- 📤 Kafka producer for publishing micro-messages  
- 📥 Kafka consumer for stream-based message consumption  
- 🌐 Flask-based API gateway for sending and retrieving messages  
- 🐳 Docker Compose setup for Kafka, Zookeeper, and all services  
- 🔁 Asynchronous, event-driven system architecture  
- 🛠️ Modular structure: Producer, Consumer, and API components

---

## 🛠️ Technologies Used

- **Language:** Python  
- **Web Framework:** Flask  
- **Streaming:** Apache Kafka, kafka-python  
- **Infrastructure:** Docker, Docker Compose, Zookeeper  
- **Testing Tools:** curl, Postman  
- **Architecture:** Microservices, event-driven messaging

---

## 🚀 Getting Started

### ✅ Prerequisites

- Docker  
- Docker Compose

### ▶️ Run the System

```bash
# Clone the repo
git clone https://github.com/nikhathfirdose1/Microblogging-Platform.git
cd Microblogging-Platform

# Start all services
docker-compose up --build
```

---

## 🔌 API Endpoints

### POST /publish

Publishes a message to Kafka.

```bash
curl -X POST http://localhost:5000/publish \
     -H "Content-Type: application/json" \
     -d '{"user": "nikhath", "message": "Hello Kafka!"}'
```

**Response:**
```json
{
  "status": "Message published"
}
```

---

### GET /messages

Returns messages consumed from Kafka.

```bash
curl http://localhost:5000/messages
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
├── api_gateway/
│   └── app.py
├── producer/
│   └── producer.py
├── consumer/
│   └── consumer.py
├── docker-compose.yml
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

- Build Kafka-based real-time pipelines  
- Use Docker Compose to orchestrate services  
- Understand producer-consumer messaging systems  
- Build RESTful APIs with Flask for microservices

---

## 📄 License

This project is intended for educational and demonstration purposes only.
