package se.sundsvall.checklist.integration.mdviewer.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.mdviewer")
public record MDViewerProperties(
	int connectTimeout,
	int readTimeout) {
}
