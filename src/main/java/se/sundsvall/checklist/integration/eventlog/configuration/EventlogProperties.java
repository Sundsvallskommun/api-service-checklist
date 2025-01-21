package se.sundsvall.checklist.integration.eventlog.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("integration.eventlog")
public record EventlogProperties(
	@DefaultValue("5") int connectTimeout,
	@DefaultValue("15") int readTimeout) {
}
