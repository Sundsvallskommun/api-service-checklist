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

class EmployeeEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays((new Random().nextInt())), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(EmployeeEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void preUpdateTest() {
		var employee = EmployeeEntity.builder().build();

		employee.preUpdate();

		assertThat(employee.getCreated()).isNull();
		assertThat(employee.getUpdated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
	}

	@Test
	void prePersistTest() {
		var employee = EmployeeEntity.builder().build();

		employee.prePersist();

		assertThat(employee.getCreated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
		assertThat(employee.getUpdated()).isNotNull().isCloseTo(now(systemDefault()), within(2, SECONDS));
	}
}
