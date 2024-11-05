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

import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

class ChecklistTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Checklist.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var id = "id";
		final var roleType = RoleType.EMPLOYEE;
		final var name = "name";
		final var displayName = "displayName";
		final var version = 1;
		final var lifeCycle = LifeCycle.ACTIVE;
		final var lastSavedBy = "someUser";

		final var checklist = Checklist.builder()
			.withId(id)
			.withRoleType(roleType)
			.withName(name)
			.withVersion(version)
			.withLifeCycle(lifeCycle)
			.withDisplayName(displayName)
			.withUpdated(OffsetDateTime.now())
			.withCreated(OffsetDateTime.now())
			.withLastSavedBy(lastSavedBy)
			.withPhases(List.of())
			.build();

		assertThat(checklist).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(checklist.getId()).isEqualTo(id);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Checklist.builder().build()).hasAllNullFieldsOrProperties();
	}
}
