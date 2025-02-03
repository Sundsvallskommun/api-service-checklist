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
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InitiationInformationTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(InitiationInformation.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));

		MatcherAssert.assertThat(InitiationInformation.Detail.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var executed = OffsetDateTime.now();
		final var logId = "logId";
		final var summary = "summaryinformation";
		final var detailinformation = "detailinformation";
		final var status = 1337;

		final var detail = InitiationInformation.Detail.builder()
			.withInformation(detailinformation)
			.withStatus(status)
			.build();

		final var information = InitiationInformation.builder()
			.withDetails(List.of(detail))
			.withExecuted(executed)
			.withLogId(logId)
			.withSummary(summary)
			.build();

		assertThat(detail).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(detail.getInformation()).isEqualTo(detailinformation);
		assertThat(detail.getStatus()).isEqualTo(status);

		assertThat(information).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(information.getDetails()).hasSize(1).containsExactly(detail);
		assertThat(information.getExecuted()).isEqualTo(executed);
		assertThat(information.getLogId()).isEqualTo(logId);
		assertThat(information.getSummary()).isEqualTo(summary);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(InitiationInformation.Detail.builder()).hasAllNullFieldsOrPropertiesExcept("status").hasFieldOrPropertyWithValue("status", 0);
		assertThat(new InitiationInformation.Detail()).hasAllNullFieldsOrPropertiesExcept("status").hasFieldOrPropertyWithValue("status", 0);

		assertThat(InitiationInformation.builder()).hasAllNullFieldsOrProperties();
		assertThat(new InitiationInformation()).hasAllNullFieldsOrProperties();
	}
}
