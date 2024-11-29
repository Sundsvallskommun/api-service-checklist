package se.sundsvall.checklist.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import se.sundsvall.checklist.api.model.SortorderRequest.PhaseItem;
import se.sundsvall.checklist.api.model.SortorderRequest.TaskItem;

class SortorderRequestTest {

	@Test
	void testBean() {
		assertValid(SortorderRequest.class);
		assertValid(PhaseItem.class);
		assertValid(TaskItem.class);
	}

	private void assertValid(Class<?> clazz) {
		MatcherAssert.assertThat(clazz, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testSortorderRequestBuilderMethods() {
		final List<PhaseItem> phaseOrder = List.of(PhaseItem.builder().build());

		final var bean = SortorderRequest.builder()
			.withPhaseOrder(phaseOrder)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getPhaseOrder()).isEqualTo(phaseOrder);
	}

	@Test
	void testPhaseItemBuilderMethods() {
		final var id = "id";
		final var position = 112;
		final List<TaskItem> taskOrder = List.of(TaskItem.builder().build());

		final var bean = PhaseItem.builder()
			.withId(id)
			.withPosition(position)
			.withTaskOrder(taskOrder)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getPosition()).isEqualTo(position);
		assertThat(bean.getTaskOrder()).isEqualTo(taskOrder);
	}

	@Test
	void testTaskItemBuilderMethods() {
		final var id = "id";
		final var position = 212;

		final var bean = TaskItem.builder()
			.withId(id)
			.withPosition(position)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getPosition()).isEqualTo(position);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SortorderRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new SortorderRequest()).hasAllNullFieldsOrProperties();

		assertThat(PhaseItem.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new PhaseItem()).hasAllNullFieldsOrProperties();

		assertThat(TaskItem.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new TaskItem()).hasAllNullFieldsOrProperties();
	}
}
