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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmployeeChecklistEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays((new Random().nextInt())), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(EmployeeChecklistEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void preUpdateTest() {
		var entity = EmployeeChecklistEntity.builder().build();

		entity.preUpdate();

		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getUpdated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
	}

	@Test
	void prePersistTest() {
		var entity = EmployeeChecklistEntity.builder().build();

		entity.prePersist();

		assertThat(entity.getCreated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
		assertThat(entity.getUpdated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
	}
}
