# Checklist

_The service provides administration of checklist tasks that new employees and their corresponding manager shall undergo when a person is employed. The service also keeps track of the checklist progress of each individual._

## Getting Started

### Prerequisites

- **Java 21 or higher**
- **Maven**
- **MariaDB**
- **Git**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

```bash
git clone https://github.com/Sundsvallskommun/api-service-checklist.git
cd api-service-checklist
```

2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible. See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   - Using Maven:

```bash
mvn spring-boot:run
```

- Using Gradle:

```bash
gradle bootRun
```

## Dependencies

This microservice depends on the following services:

- **Employee**
  - **Purpose:** Used for reading employee information.
- **MDViewer**
  - **Purpose:** Used for reading organizational structure information.
- **Messaging**
  - **Purpose:** Used for sending emails to employees and managers.
  - **Repository:** [https://github.com/Sundsvallskommun/api-service-messaging](https://github.com/Sundsvallskommun/api-service-messaging)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.
- **Templating**
  - **Purpose:** Provides html templates when sending emails.
  - **Repository:** [https://github.com/Sundsvallskommun/api-service-templating](https://github.com/Sundsvallskommun/api-service-templating)
  - **Setup Instructions:** See documentation in repository above for installation and configuration steps.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## Usage

### API Endpoints

See the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X GET http://localhost:8080/2281/employee-checklists/employee/username
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in `application.yml`.

### Key Configuration Parameters

- **Server Port:**

```yaml
server:
  port: 8080
```

- **Database Settings**

```yaml
config:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_db_username
    password: your_db_password
  sql:
    init:
      mode: sql-init-mode (should be set to  'never' in production env)
  jpa:
    ddl-auto: auto-setting (should be set to 'validate' in production env)
    defer-datasource-initialization: false
```

- **External Service URLs**

```yaml
  config:
    common:
      token-url: http://dependecy_token_url
      client-id: some-client-id
      client-secret: some-client-secret

    integration:
      employee:
        url: http://employee_service_url
        token-url: ${config.common.token-url}
        client-id: ${config.common.client-id}
        client-secret: ${config.common.client-secret}
      mdviewer:
        url: http://mdviewer_service_url
        token-url: ${config.common.token-url}
        client-id: ${config.common.client-id}
        client-secret: ${config.common.client-secret}
      messaging:
        url: http://messaging_service_url
        token-url: ${config.common.token-url}
        client-id: ${config.common.client-id}
        client-secret: ${config.common.client-secret}
      templating:
        url: http://templating_service_url
        token-url: ${config.common.token-url}
        client-id: ${config.common.client-id}
        client-secret: ${config.common.client-secret}

```

- **Configuration of schedulers**

```yaml
  config:
    schedulers:
      new-employees:
        cron: cron expression when scheduler should run (or "-" to disable it)
        fetch-on-startup: true/false, states if new employees shall be fetched or not on service start up
        delay-on-startup: ISO861 format for delay before new employees are fetched after service start up
      manager-email:
        cron: cron expression when scheduler should run (or "-" to disable it)
      lock-employee-checklists:
        cron: cron expression when scheduler should run (or "-" to disable it)

    manager-email:
      template: template_name
      subject: subject-string
      sender:
        reply-to: reply-to-address
        address: sender-address
        name: sender-name
```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is disabled by default so you will have to enable it to automatically populate the database schema upon application startup.

```yaml
config:
  flyway:
    enabled: true
```

- **No additional setup is required** for database initialization, as long as the database connection settings are correctly configured.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-checklist&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-checklist)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-checklist&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-checklist)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-checklist&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-checklist)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-checklist&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-checklist)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-checklist&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-checklist)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-checklist&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-checklist)

---

&copy; 2024 Sundsvalls kommun
