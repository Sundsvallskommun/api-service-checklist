package se.sundsvall.checklist.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static se.sundsvall.checklist.integration.db.model.enums.Permission.ADMIN;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.checklist.integration.db.model.enums.RoleType;

class PhaseTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Phase.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var id = "id";
		final var name = "name";
		final var bodyText = "bodyText";
		final var timeToComplete = "timeToComplete";
		final var roleType = RoleType.EMPLOYEE;
		final var sortOrder = 1;

		final var phase = Phase.builder()
			.withId(id)
			.withName(name)
			.withBodyText(bodyText)
			.withTimeToComplete(timeToComplete)
			.withRoleType(roleType)
			.withSortOrder(sortOrder)
			.withPermission(ADMIN)
			.withUpdated(OffsetDateTime.now())
			.withCreated(OffsetDateTime.now())
			.withTasks(List.of())
			.build();

		assertThat(phase).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(phase.getId()).isEqualTo(id);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Phase.builder().build()).hasAllNullFieldsOrPropertiesExcept("sortOrder");
	}
}
