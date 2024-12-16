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

class DelegatedEmployeeChecklistResponseTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(DelegatedEmployeeChecklistResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var employeeChecklists = List.of(EmployeeChecklist.builder().build(), EmployeeChecklist.builder().build());

		final var bean = DelegatedEmployeeChecklistResponse.builder()
			.withEmployeeChecklists(employeeChecklists)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getEmployeeChecklists()).isEqualTo(employeeChecklists);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DelegatedEmployeeChecklistResponse.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new DelegatedEmployeeChecklistResponse()).hasAllNullFieldsOrProperties();
	}
}
