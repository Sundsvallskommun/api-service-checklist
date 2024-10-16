package se.sundsvall.checklist.integration.messaging.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.messaging")
public record MessagingProperties(int connectTimeout, int readTimeout, Email managerEmail) {

	public record Email(String subject, Sender sender) {
	}

	public record Sender(String address, String name, String replyTo) {
	}

}
