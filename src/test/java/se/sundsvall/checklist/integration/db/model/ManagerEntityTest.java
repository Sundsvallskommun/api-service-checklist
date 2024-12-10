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

class ManagerEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(ManagerEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void preUpdateTest() {
		final var manager = ManagerEntity.builder().build();

		manager.preUpdate();

		assertThat(manager.getCreated()).isNull();
		assertThat(manager.getUpdated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
	}

	@Test
	void prePersistTest() {
		final var manager = ManagerEntity.builder().build();

		manager.prePersist();

		assertThat(manager.getCreated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
		assertThat(manager.getUpdated()).isNull();
	}
}
