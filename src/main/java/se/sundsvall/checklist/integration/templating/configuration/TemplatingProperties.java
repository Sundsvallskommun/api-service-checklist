package se.sundsvall.checklist.integration.templating.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.templating")
public record TemplatingProperties(
	int connectTimeout,
	int readTimeout) {
}
