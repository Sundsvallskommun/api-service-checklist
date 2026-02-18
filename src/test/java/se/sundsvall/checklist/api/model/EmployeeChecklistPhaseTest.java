package se.sundsvall.checklist.api.model;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class EmployeeChecklistPhaseTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(EmployeeChecklistPhase.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var bodyText = "bodyText";
		final var id = "id";
		final var name = "name";
		final var sortOrder = 123;
		final var tasks = List.of(EmployeeChecklistTask.builder().build());
		final var timeToComplete = "timeToComplete";

		final var bean = EmployeeChecklistPhase.builder()
			.withBodyText(bodyText)
			.withId(id)
			.withName(name)
			.withSortOrder(sortOrder)
			.withTasks(tasks)
			.withTimeToComplete(timeToComplete)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getBodyText()).isEqualTo(bodyText);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getSortOrder()).isEqualTo(sortOrder);
		assertThat(bean.getTasks()).isEqualTo(tasks);
		assertThat(bean.getTimeToComplete()).isEqualTo(timeToComplete);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(EmployeeChecklistPhase.builder().build()).hasAllNullFieldsOrPropertiesExcept("sortOrder", "tasks")
			.hasFieldOrPropertyWithValue("sortOrder", 0)
			.hasFieldOrPropertyWithValue("tasks", emptyList());

		assertThat(new EmployeeChecklistPhase()).hasAllNullFieldsOrPropertiesExcept("sortOrder", "tasks")
			.hasFieldOrPropertyWithValue("sortOrder", 0)
			.hasFieldOrPropertyWithValue("tasks", emptyList());
	}
}
