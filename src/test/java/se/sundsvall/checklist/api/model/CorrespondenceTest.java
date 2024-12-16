package se.sundsvall.checklist.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;

class CorrespondenceTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Correspondence.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var attempts = 123;
		final var communicationChannel = CommunicationChannel.EMAIL;
		final var correspondenceStatus = CorrespondenceStatus.SENT;
		final var messageId = "messageId";
		final var recipient = "recipient";
		final var sent = OffsetDateTime.now();

		final var bean = Correspondence.builder()
			.withAttempts(attempts)
			.withCommunicationChannel(communicationChannel)
			.withCorrespondenceStatus(correspondenceStatus)
			.withMessageId(messageId)
			.withRecipient(recipient)
			.withSent(sent)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getAttempts()).isEqualTo(attempts);
		assertThat(bean.getCommunicationChannel()).isEqualTo(communicationChannel);
		assertThat(bean.getCorrespondenceStatus()).isEqualTo(correspondenceStatus);
		assertThat(bean.getMessageId()).isEqualTo(messageId);
		assertThat(bean.getRecipient()).isEqualTo(recipient);
		assertThat(bean.getSent()).isEqualTo(sent);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Correspondence.builder().build()).hasAllNullFieldsOrPropertiesExcept("attempts").hasFieldOrPropertyWithValue("attempts", 0);
		assertThat(new Correspondence()).hasAllNullFieldsOrPropertiesExcept("attempts").hasFieldOrPropertyWithValue("attempts", 0);
	}
}
