package se.sundsvall.checklist.integration.eventlog.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.eventlog")
public record EventlogProperties(
	int connectTimeout,
	int readTimeout) {
}
