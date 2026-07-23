Disaster Response System

A multi-agent disaster management platform built with Spring Boot that simulates disaster conditions, predicts flood risk, allocates emergency resources, generates alerts, and monitors agent execution through a real-time dashboard.

Overview

The system uses multiple specialized agents coordinated by a supervisor agent. Each agent performs a specific disaster-response task, while an adaptive priority engine determines the execution order based on current disaster conditions.

Key Features

Multi-agent disaster response workflow

Weather-condition analysis

Flood-risk prediction

Emergency resource allocation

Alert and communication generation

Adaptive agent-priority calculation

Real-time dashboard updates using WebSocket

Simulation history stored in MySQL

Agent execution tracking and performance metrics

Adaptive-priority and fixed-priority benchmarking

PDF simulation report generation

Excel export for simulations and agent executions

Multi-Agent Architecture

Supervisor Agent: Coordinates the complete simulation and manages agent execution.

Weather Agent: Analyzes rainfall, river level, and other provided weather conditions.

Flood Prediction Agent: Calculates flood risk and determines the corresponding risk level.

Resource Allocation Agent: Allocates available teams, boats, and food kits based on disaster severity.

Communication Agent: Generates disaster alerts and emergency response messages.

Adaptive Priority Engine: Dynamically calculates agent priorities according to current disaster conditions.

Screenshots

Dashboard



Disaster Simulation



Simulation Results



Reports



Tech Stack

Category

Technologies

Backend

Java 17, Spring Boot, Spring Framework, Spring Data JPA, Hibernate, REST APIs, WebSocket, Maven, Lombok

Frontend

HTML5, CSS3, JavaScript, Thymeleaf

Database

MySQL

Reporting

Apache POI, Apache PDFBox

Development Tools

Git, GitHub, IntelliJ IDEA, Eclipse, VS Code, Postman

Project Structure

disaster-response-system/
├── pom.xml
├── README.md
├── screenshots/
│   ├── dashboard.png
│   ├── simulation.png
│   ├── results.png
│   └── reports.png
└── src/
    └── main/
        ├── java/
        │   └── com/disaster/
        │       ├── agent/
        │       ├── config/
        │       ├── controller/
        │       ├── engine/
        │       ├── model/
        │       ├── repository/
        │       ├── service/
        │       └── DisasterResponseApplication.java
        └── resources/
            ├── application.yml
            ├── schema.sql
            ├── templates/
            │   └── dashboard.html
            └── static/
                ├── css/
                │   └── style.css
                └── js/
                    └── dashboard.js

Prerequisites

Install the following before running the project:

Java 17 or later

Maven 3.6 or later

MySQL 8 or later

Git

Database Setup

Create the MySQL database:

CREATE DATABASE disaster_db;

The project includes a schema.sql file containing the required database structure.

Configuration

Open:

src/main/resources/application.yml

Configure the database connection using environment variables:

spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/disaster_db}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}

Set the database password before running the application.

Windows PowerShell

$env:DB_PASSWORD="your_mysql_password"

Windows Command Prompt

set DB_PASSWORD=your_mysql_password

macOS/Linux

export DB_PASSWORD=your_mysql_password

Do not commit real database passwords or other credentials to GitHub.

Run the Application

Clone the repository:

git clone https://github.com/Md-Minshaniya/disaster-response-system.git
cd disaster-response-system

Build the project:

mvn clean install

Run the application:

mvn spring-boot:run

Open the dashboard in your browser:

http://localhost:8080

Application Workflow

User Input
    ↓
Supervisor Agent
    ↓
Adaptive Priority Engine
    ↓
Weather Agent
    ↓
Flood Prediction Agent
    ↓
Resource Allocation Agent
    ↓
Communication Agent
    ↓
Database + Real-Time Dashboard + Reports

Main Modules

Simulation Management

The simulation accepts disaster-related inputs such as:

Region

Rainfall

River level

Population

Available emergency teams

Available boats

Available food kits

The system processes these inputs through the agents and stores the simulation results in MySQL.

Real-Time Monitoring

WebSocket communication updates the dashboard while agents are executing. The dashboard displays simulation results, risk level, agent status, execution time, and system metrics.

Benchmarking

The benchmark module compares:

Adaptive-priority execution

Fixed-priority execution

This comparison helps evaluate execution time and agent-processing performance.

Reporting

The system supports:

PDF reports for individual simulations

Excel exports for simulation history

Excel exports for agent execution records

Database Entities

Simulation

Stores the main disaster-simulation details:

Region

Rainfall

River level

Population

Flood risk

Risk level

Response time

Priority mode

Creation time

Agent Execution

Stores execution details for each agent:

Simulation reference

Agent name

Execution time

Priority score

Agent result

Creation time

Testing

The REST APIs can be tested using Postman.

Before pushing changes, verify the project build:

mvn clean install

Run the application and confirm that:

The application connects successfully to MySQL

The dashboard opens correctly

Disaster simulations execute successfully

Simulation history is stored in the database

WebSocket updates are displayed

PDF and Excel reports are generated

Benchmarking completes successfully
