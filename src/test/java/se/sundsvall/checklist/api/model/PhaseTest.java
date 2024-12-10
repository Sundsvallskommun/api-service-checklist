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
		final var sortOrder = 321;
		final var lastSavedBy = "someUser";
		final var created = OffsetDateTime.now();
		final var updated = OffsetDateTime.now().plusDays(10);
		final var tasks = List.of(Task.builder().build());
		final var permission = ADMIN;

		final var bean = Phase.builder()
			.withId(id)
			.withName(name)
			.withBodyText(bodyText)
			.withTimeToComplete(timeToComplete)
			.withSortOrder(sortOrder)
			.withPermission(permission)
			.withUpdated(updated)
			.withCreated(created)
			.withLastSavedBy(lastSavedBy)
			.withTasks(tasks)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getBodyText()).isEqualTo(bodyText);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getLastSavedBy()).isEqualTo(lastSavedBy);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getPermission()).isEqualTo(permission);
		assertThat(bean.getSortOrder()).isEqualTo(sortOrder);
		assertThat(bean.getTasks()).isEqualTo(tasks);
		assertThat(bean.getTimeToComplete()).isEqualTo(timeToComplete);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Phase.builder().build()).hasAllNullFieldsOrPropertiesExcept("sortOrder").hasFieldOrPropertyWithValue("sortOrder", 0);
		assertThat(new Phase()).hasAllNullFieldsOrPropertiesExcept("sortOrder").hasFieldOrPropertyWithValue("sortOrder", 0);
	}
}
