package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aop.framework.AopProxyUtils.getSingletonTarget;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.checklist.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class CommunicationServicePropertyTest {

	@Autowired
	private CommunicationService service;

	@Test
	void verifyProperties() throws Exception {
		final var privateField = Class.forName("se.sundsvall.checklist.service.CommunicationService").getDeclaredField("emailTemplate");
		privateField.setAccessible(true);

		// Need to use AoP utils here as service is wrapped in a proxy by Spring when using the @transaction annotation
		assertThat(privateField.get(getSingletonTarget(service))).isEqualTo("manager-email-template");
	}
}
