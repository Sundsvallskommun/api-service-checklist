package se.sundsvall.checklist.integration.company.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.company")
public record CompanyProperties(
	int connectTimeout,
	int readTimeout) {
}
