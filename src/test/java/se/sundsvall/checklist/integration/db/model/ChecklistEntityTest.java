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

class ChecklistEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(ChecklistEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void preUpdateTest() {
		final var entity = ChecklistEntity.builder().build();

		entity.preUpdate();

		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getUpdated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
	}

	@Test
	void prePersistTest() {
		final var entity = ChecklistEntity.builder().build();

		entity.prePersist();

		assertThat(entity.getCreated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
		assertThat(entity.getUpdated()).isNull();
	}
}
