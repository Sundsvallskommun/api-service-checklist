package se.sundsvall.checklist.integration.messaging.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.checklist.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class MessagingPropertiesTest {

	@Autowired
	private MessagingProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.connectTimeout()).isEqualTo(5);
		assertThat(properties.readTimeout()).isEqualTo(60);
		assertThat(properties.managerEmail().subject()).isEqualTo("subject");
		assertThat(properties.managerEmail().sender().address()).isEqualTo("address");
		assertThat(properties.managerEmail().sender().name()).isEqualTo("name");
		assertThat(properties.managerEmail().sender().replyTo()).isEqualTo("reply_to");
	}

}
