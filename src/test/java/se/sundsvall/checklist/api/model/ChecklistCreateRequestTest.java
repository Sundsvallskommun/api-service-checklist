package se.sundsvall.checklist.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class ChecklistCreateRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(ChecklistCreateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var createdBy = "someUser";
		final var displayName = "displayName";
		final var name = "name";
		final var organizationNumber = 1337;

		final var bean = ChecklistCreateRequest.builder()
			.withCreatedBy(createdBy)
			.withDisplayName(displayName)
			.withName(name)
			.withOrganizationNumber(organizationNumber)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getDisplayName()).isEqualTo(displayName);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getOrganizationNumber()).isEqualTo(organizationNumber);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ChecklistCreateRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ChecklistCreateRequest()).hasAllNullFieldsOrProperties();
	}
}
