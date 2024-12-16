package se.sundsvall.checklist.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static se.sundsvall.checklist.integration.db.model.enums.Permission.SUPERADMIN;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class PhaseUpdateRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(PhaseUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var bodyText = "bodyText";
		final var name = "name";
		final var sortOrder = 911;
		final var permission = SUPERADMIN;
		final var timeToComplete = "timeToComplete";
		final var updatedBy = "updatedBy";

		final var bean = PhaseUpdateRequest.builder()
			.withBodyText(bodyText)
			.withName(name)
			.withPermission(permission)
			.withSortOrder(sortOrder)
			.withTimeToComplete(timeToComplete)
			.withUpdatedBy(updatedBy)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getBodyText()).isEqualTo(bodyText);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getPermission()).isEqualTo(permission);
		assertThat(bean.getSortOrder()).isEqualTo(sortOrder);
		assertThat(bean.getTimeToComplete()).isEqualTo(timeToComplete);
		assertThat(bean.getUpdatedBy()).isEqualTo(updatedBy);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(PhaseUpdateRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new PhaseUpdateRequest()).hasAllNullFieldsOrProperties();
	}
}
