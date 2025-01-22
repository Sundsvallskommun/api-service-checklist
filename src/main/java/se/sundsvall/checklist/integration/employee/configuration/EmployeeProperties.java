package se.sundsvall.checklist.integration.employee.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.employee")
public record EmployeeProperties(
	int connectTimeout,
	int readTimeout) {
}
