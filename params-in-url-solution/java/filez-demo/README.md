# Filez Demo - Document Management Integration Example Project

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)

English | [简体中文](README-zh.md)

## 📖 Overview

Filez Demo is a Spring Boot-based document management integration example project, primarily designed to demonstrate how to integrate with the Filez document platform. The project provides complete document upload, download, edit, preview, and comparison features.

**⚠️ Important Notice**: This project is intended for **testing and demonstration purposes only**. Do NOT use this integration example on your production server without proper code modifications and security enhancements!

## ✨ Core Features

### 🔐 User Authentication System
- JWT-based authentication
- User session management
- Login/logout functionality
- User profile management

### 📁 Document Management Features
- **Upload**: Single file and batch file upload
- **Download**: Document content retrieval
- **Delete**: Single and batch file deletion
- **Create**: New document creation
- **Preview**: Real-time document preview

### 📝 ZOffice Integration Features
- Online document editing
- Real-time collaboration
- Document comparison
- Version control
- Comment and mention notifications
- Multiple format support (Word, Excel, PowerPoint, PDF)

### 🔧 System Management Features
- Embedded SQLite database
- File storage management
- API documentation (Swagger/Knife4j)
- Comprehensive logging system

## Technical Architecture

### Backend Technology Stack
- **Framework**: Spring Boot 2.5.4
- **Database**: SQLite (embedded database, no additional installation required)
- **ORM**: MyBatis Plus 3.4.3.4
- **Template Engine**: FreeMarker
- **API Documentation**: Knife4j (Swagger)
- **JSON Processing**: Fastjson 1.2.83
- **JWT**: JJWT 0.9.1
- **HTTP Client**: Apache HttpClient 4.5.13

### Project Structure
```
filez-demo/
├── src/main/java/com/filez/demo/
│   ├── common/                # Common components
│   │   ├── aspect/            # AOP aspects (logging)
│   │   ├── constant/          # Constants definition
│   │   ├── context/           # Context management (user context)
│   │   ├── interceptor/       # Interceptors (login interception)
│   │   ├── listener/          # Listeners
│   │   └── utils/             # Utility classes (JWT, HMAC, etc.)
│   ├── config/                # Configuration classes
│   │   ├── DatabaseConfig.java    # Database configuration
│   │   ├── DemoConfig.java        # Business configuration
│   │   ├── SwaggerConfig.java     # API documentation configuration
│   │   └── ZOfficeConfig.java     # ZOffice integration configuration
│   ├── controller/            # Controller layer
│   │   ├── LoginController.java   # Login controller
│   │   ├── HomeController.java    # Home controller
│   │   ├── FileController.java    # File operation controller
│   │   └── ZOfficeController.java # ZOffice integration controller
│   ├── dao/                   # Data access layer
│   ├── entity/                # Entity classes
│   ├── model/                 # Data models
│   ├── service/               # Business logic layer
│   └── FilezDemoApplication.java  # Main application class
├── src/main/resources/
│   ├── application.yml        # Main configuration file
│   ├── zoffice.yml           # ZOffice integration configuration
│   ├── mapper/               # MyBatis mapping files
│   ├── sql/                  # Database scripts
│   ├── static/               # Static resources
│   └── templates/            # FreeMarker templates
├── data/                     # SQLite database files
├── local-file/              # Local file storage
└── logs/                    # Log files
```

## 🚀 Quick Start

### Prerequisites
Before you begin, ensure you have the following installed:
- **Java**: JDK 8 or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Maven**: 3.6 or higher ([Download](https://maven.apache.org/download.cgi))
- **Git**: For cloning the repository

### Installation Steps

#### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd filez-demo
```

#### Step 2: Build the Project
```bash
# Clean and build
mvn clean package

# Skip tests (optional)
mvn clean package -DskipTests
```

After successful build, you will find `filez-demo-1.0.0.RELEASE.jar` in the `target/` directory.

#### Step 3: Configure the Application

The project supports multiple configuration methods. Choose the appropriate method based on your deployment environment:

##### Option 1: Using Built-in Configuration (Recommended for Development)
Use the project's built-in SQLite database and default configuration:

```bash
java -jar target/filez-demo-1.0.0.RELEASE.jar
```

##### Option 2: Using External Configuration File
Create an `application-external.yml` file in the same directory as the JAR:

```yaml
server:
  port: 8000

zoffice:
  service:
    host: 172.16.34.165    # Your ZOffice server host
    port: 8001              # Your ZOffice server port

demo:
  host: 172.16.34.165      # Your application host
  context: /v2/context
  repoId: 3rd-party
```

Then run with:
```bash
java -jar target/filez-demo-1.0.0.RELEASE.jar --spring.profiles.active=external
```

##### Option 3: Using Command Line Parameters
```bash
java -jar target/filez-demo-1.0.0.RELEASE.jar \
  --server.port=8000 \
  --zoffice.service.host=172.16.34.165 \
  --zoffice.service.port=8001 \
  --demo.host=172.16.34.165
```

##### Option 4: Using Environment Variables
```bash
export SERVER_PORT=8000
export ZOFFICE_SERVICE_HOST=172.16.34.165
export ZOFFICE_SERVICE_PORT=8001
export DEMO_HOST=172.16.34.165
java -jar target/filez-demo-1.0.0.RELEASE.jar
```

#### Step 4: Run the Application

##### For Development (Foreground)
```bash
java -jar target/filez-demo-1.0.0.RELEASE.jar
```

##### For Production (Background)
```bash
# Linux/macOS
nohup java -jar target/filez-demo-1.0.0.RELEASE.jar --spring.profiles.active=external > logs/app.log 2>&1 &

# Windows (using PowerShell)
Start-Process java -ArgumentList "-jar","target/filez-demo-1.0.0.RELEASE.jar" -WindowStyle Hidden
```

#### Step 5: Verify Installation

After successful startup, verify the application is running:

```bash
# Check if the application is responding
curl http://localhost:8000

# Or open in browser
# http://localhost:8000
```

### 🛑 Stopping the Service

#### Find the Process
```bash
# Linux/macOS - Check port usage
sudo netstat -tunlp | grep ':8000'
# Or
lsof -i :8000

# Windows - Check port usage
netstat -ano | findstr :8000

# Check Java processes
# Linux/macOS
ps aux | grep filez-demo

# Windows
tasklist | findstr java
```

#### Stop the Process
```bash
# Linux/macOS - Graceful shutdown (recommended)
kill <PID>

# Linux/macOS - Force stop (use with caution)
kill -9 <PID>

# Linux/macOS - Stop all related processes
pkill -f filez-demo

# Windows - Stop by PID
taskkill /PID <PID> /F
```

## 🌐 Accessing the Application

After successful startup, you can access the application through the following URLs:

| Service | URL | Description |
|---------|-----|-------------|
| **Home Page** | http://localhost:8000 | Main application entry |
| **Login Page** | http://localhost:8000/login | User authentication |
| **API Documentation** | http://localhost:8000/doc.html | Swagger/Knife4j API docs |
| **File Management** | http://localhost:8000/home/local | Document management interface |

### Default Credentials

For testing purposes, use the following default credentials:

| Field | Value |
|-------|-------|
| **Username** | `admin` |
| **Password** | `zOffice` |

⚠️ **Security Warning**: Change these default credentials before deploying to production!

## 📚 API Documentation

The application provides a comprehensive REST API. For detailed API documentation, visit the Swagger UI at http://localhost:8000/doc.html after starting the application.

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/login` | Display login page |
| `POST` | `/login` | Authenticate user |
| `GET` | `/logout` | Logout current user |

### File Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/v2/context/file/upload` | Upload single file |
| `POST` | `/v2/context/file/batchOp/upload` | Batch upload files |
| `DELETE` | `/v2/context/file/delete/{docId}` | Delete file by ID |
| `POST` | `/v2/context/file/batchOp/delete` | Batch delete files |
| `POST` | `/v2/context/file/new` | Create new document |

### ZOffice Integration Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/v2/context/driver-cb` | Get frontend integration URL |
| `GET` | `/v2/context/{docId}/content` | Download document content |
| `POST` | `/v2/context/{docId}/content` | Upload document content |
| `GET` | `/v2/context/{docId}/meta` | Get document metadata |
| `GET` | `/v2/context/profiles` | Get user profile information |
| `POST` | `/v2/context/{docId}/notify` | Document status notification callback |
| `POST` | `/v2/context/{docId}/mention` | Document mention notification |
| `GET` | `/v2/context/compareDoc` | Compare two documents |

### Web Page Routes

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/home/` | Application home page |
| `GET` | `/home/local` | File list and management |
| `GET` | `/home/user` | User profile page |
| `GET` | `/home/compare` | Document comparison interface |

## 🔍 Monitoring and Troubleshooting

### Log Files

Application logs are stored in the `logs/` directory:

```bash
# Linux/macOS - View real-time logs
tail -f logs/filezDemo.log

# Linux/macOS - View error logs
grep -i error logs/filezDemo.log

# Linux/macOS - View recent logs
tail -n 100 logs/filezDemo.log

# Windows - View real-time logs
Get-Content logs/filezDemo.log -Wait -Tail 50

# Windows - Search for errors
Select-String -Path logs/filezDemo.log -Pattern "error" -CaseSensitive:$false
```

### Common Issues

#### Port Already in Use
```bash
# Change the port in configuration
--server.port=8080
```

#### Database Connection Issues
- Ensure the `data/` directory has read/write permissions
- Check if SQLite database file exists and is not corrupted

#### ZOffice Integration Issues
- Verify ZOffice service is running and accessible
- Check `zoffice.service.host` and `zoffice.service.port` configuration
- Ensure network connectivity between services

## 👨‍💻 Development Guide

### Setting Up Development Environment

1. **Install Prerequisites**
   - Java JDK 8 or higher
   - Maven 3.6 or higher
   - IDE (IntelliJ IDEA recommended)

2. **Clone and Import Project**
   ```bash
   git clone <repository-url>
   cd filez-demo
   ```

3. **Import into IDE**
   - Open IntelliJ IDEA
   - File → Open → Select `filez-demo` directory
   - Wait for Maven to download dependencies

4. **Run Application**
   - Locate `FilezDemoApplication.java`
   - Right-click → Run 'FilezDemoApplication.main()'
   - Or use Maven: `mvn spring-boot:run`

### Project Structure Explained

```
src/main/java/com/filez/demo/
├── common/              # Shared components
│   ├── aspect/         # AOP for logging and cross-cutting concerns
│   ├── constant/       # Application constants
│   ├── context/        # Request context management
│   ├── interceptor/    # HTTP interceptors (authentication, etc.)
│   └── utils/          # Utility classes (JWT, HMAC, etc.)
├── config/             # Spring configuration classes
├── controller/         # REST API controllers
├── dao/                # Data access layer (MyBatis)
├── entity/             # Database entities
├── model/              # DTOs and request/response models
└── service/            # Business logic layer
```

### Building for Production

```bash
# Build with tests
mvn clean package

# Build without tests (faster)
mvn clean package -DskipTests

# Build with specific profile
mvn clean package -Pproduction
```

## 🔒 Important Security Considerations

**⚠️ This is a demonstration project. Before deploying to production, consider the following:**

1. **Authentication & Authorization**
   - Implement proper user authentication
   - Add role-based access control (RBAC)
   - Use strong password policies
   - Enable HTTPS/TLS encryption

2. **File Storage Security**
   - Validate file types and sizes
   - Implement virus scanning
   - Restrict file access permissions
   - Use secure file storage locations

3. **API Security**
   - Enable JWT token validation
   - Implement rate limiting
   - Add request validation and sanitization
   - Configure CORS properly

4. **Database Security**
   - Use production-grade database (PostgreSQL, MySQL)
   - Implement proper backup strategy
   - Encrypt sensitive data
   - Use parameterized queries (already implemented with MyBatis)

5. **Network Security**
   - Deploy behind a reverse proxy (Nginx, Apache)
   - Configure firewall rules
   - Use private networks for service communication
   - Implement network segmentation

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## 📞 Support

If you encounter any issues or have questions:

- **Issues**: Submit an issue on GitHub
- **Documentation**: Check the API documentation at `/doc.html`
- **Email**: Contact technical support team

## 🙏 Acknowledgments

- Built with [Spring Boot](https://spring.io/projects/spring-boot)
- Integrated with ZOffice document service

---

**Disclaimer**: This project is an integration example for demonstration and testing purposes. Please make appropriate modifications, security enhancements, and optimizations according to your specific business requirements before using in production environments.
