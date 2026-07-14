# AssetFlow – Enterprise Asset & Resource Management System

Enterprise Asset & Resource Management System developed for **Odoo Hackathon 2026** to streamline asset lifecycle, resource booking, maintenance, audits, notifications, dashboards, and reporting.

---

# Requirements

- Java 21
- Maven 3.9+
- Node.js 20+
- npm 10+
- Git
- MySQL-Compatible Database (TiDB Cloud)  connected (sometime slow due to server asleep)

---

# Backend

Navigate to backend

```bash
cd backend
```

Build project

```bash
mvn clean install
```

Run application

```bash
mvn spring-boot:run
```

Swagger

```
http://localhost:8080/swagger-ui/index.html
```

---

# Frontend

Navigate to frontend

```bash
cd frontend
```

Install dependencies

```bash
npm install
```

Run application

```bash
npm run dev
```

Production build

```bash
npm run build
```

---

# About

AssetFlow is a full-stack Enterprise Asset & Resource Management System built for **Odoo Hackathon 2026**. It enables secure role-based management of organizational assets, resource booking, maintenance, audits, dashboards, notifications, and reporting through an intuitive web application.

---

# Features

- Authentication & Role-Based Access Control
- Department & Employee Management
- Asset Registration & Lifecycle Management
- Asset Allocation, Transfer & Return
- Resource Booking
- Maintenance Workflow
- Audit Management
- Dashboard & Analytics
- Reports
- Notifications & Activity Logs
