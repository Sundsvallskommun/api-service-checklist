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

class PhaseCreateRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(PhaseCreateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var bodyText = "bodyText";
		final var createdBy = "createdBy";
		final var name = "name";
		final var sortOrder = 911;
		final var permission = SUPERADMIN;
		final var timeToComplete = "timeToComplete";

		final var bean = PhaseCreateRequest.builder()
			.withBodyText(bodyText)
			.withCreatedBy(createdBy)
			.withName(name)
			.withPermission(permission)
			.withSortOrder(sortOrder)
			.withTimeToComplete(timeToComplete)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getBodyText()).isEqualTo(bodyText);
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getPermission()).isEqualTo(permission);
		assertThat(bean.getSortOrder()).isEqualTo(sortOrder);
		assertThat(bean.getTimeToComplete()).isEqualTo(timeToComplete);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(PhaseCreateRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new PhaseCreateRequest()).hasAllNullFieldsOrProperties();
	}
}
