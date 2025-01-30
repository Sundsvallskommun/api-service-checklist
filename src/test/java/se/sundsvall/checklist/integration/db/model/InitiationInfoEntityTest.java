package se.sundsvall.checklist.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InitiationInfoEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(InitiationInfoEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testBuilder() {
		final var id = "id";
		final var logId = "logId";
		final var information = "information";
		final var status = "status";
		final var created = OffsetDateTime.now();

		final var bean = InitiationInfoEntity.builder()
			.withId(id)
			.withLogId(logId)
			.withInformation(information)
			.withStatus(status)
			.withCreated(created)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getLogId()).isEqualTo(logId);
		assertThat(bean.getInformation()).isEqualTo(information);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getCreated()).isEqualTo(created);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(InitiationInfoEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new InitiationInfoEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void prePersistTest() {
		final var initiationInfo = InitiationInfoEntity.builder().build();

		initiationInfo.prePersist();

		assertThat(initiationInfo.getCreated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
	}
}
