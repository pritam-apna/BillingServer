# Enterprise Retail System: Microservices Roadmap

This document outlines the master architecture and phases for building a fully distributed, scalable retail and billing management ecosystem.

## 🏢 The Microservices Architecture

Rather than building a monolithic application, this ecosystem is distributed across specialized, loosely-coupled services communicating securely.

### 1. Authentication Module (OAuth2 / OIDC)
*   **Tech Stack**: Spring Authorization Server, Spring Security.
*   **Role**: The centralized Identity Provider (IdP). It manages all users, roles, and issues JWT tokens using the Authorization Code Flow. It acts as the gatekeeper for all other downstream microservices.

### 2. Billing Module (Current Focus)
*   **Tech Stack**: Spring Boot, Thymeleaf, Javascript API clients.
*   **Role**: Handles fast point-of-sale invoice generation, localized dynamic tax calculations, and PDF generation.

### 3. Inventory Management Module
*   **Role**: The source of truth for stock quantities. Tracks warehouse logic, low-stock alerts, procurement, and supplier management. Updates the Billing module using Event-Driven Architecture (Kafka/RabbitMQ) when new products arrive.

### 4. Sales Audit Module
*   **Role**: Collects historical transaction data across regions for deep-dive reporting, financial reconciliation, employee tracking, and tax exports.

### 5. Sales Forecasting Module
*   **Role**: Leverages the heavy datasets housed in the Sales Audit DB. Uses statistical algorithms or ML to predict future stock needs and recommend intelligent price adjustments.

---

## 🚀 Execution Phases

### Phase 1: The Billing Core MVP ✅ 
*   Scaffolded Spring Boot environment with REST APIs and DTOs.
*   Interactive frontend for real-time invoice calculation and PDF printing.
*(Status: Complete)*

### Phase 2: Billing Admin Dashboard (Immediate Next Step)
*   Build an open `/admin` HTML dashboard.
*   Setup database-driven application settings (e.g., dynamic tax rates) replacing hardcoded values.
*   Build UI management tables to Create, Read, Update, and Delete (CRUD) Products locally.
*   Provide a historic table view of prior generated Invoices.
*(Status: Ready to Execute)*

### Phase 3: Centralized Authentication
*   Spin up the distinct **Authentication Module** repository.
*   Hook the Billing Server to this module as an OAuth2 Resource Server / Client.
*   Lock down the `/admin` paths using the established centralized flow.

### Phase 4: Distributed Inventory
*   Initialize the **Inventory Management** microservice.
*   Migrate heavy product/stock management to this module.
*   Establish secure inter-service API communication to sync valid products down to the Billing Module.

### Phase 5: Auditing & Analytics
*   Deploy **Sales Audit** and **Forecasting** modules, feeding entirely off asynchronous billing events.
