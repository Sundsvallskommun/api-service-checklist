package se.sundsvall.checklist.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Status;

class EmployeeChecklistResponseTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(EmployeeChecklistResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanEquals(),
			hasValidBeanHashCode(),
			hasValidBeanToString()));

		MatcherAssert.assertThat(EmployeeChecklistResponse.Detail.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanEquals(),
			hasValidBeanHashCode(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		// Implement
		final var summary = "summary";
		final var information = "information";
		final var status = Status.I_AM_A_TEAPOT;
		final var detailBean = EmployeeChecklistResponse.Detail.builder()
			.withInformation(information)
			.withStatus(status)
			.build();

		final var bean = EmployeeChecklistResponse.builder()
			.withDetails(List.of(detailBean))
			.withSummary(summary)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getDetails()).isEqualTo(List.of(detailBean));
		assertThat(bean.getSummary()).isEqualTo(summary);
		assertThat(detailBean).hasNoNullFieldsOrProperties();
		assertThat(detailBean.getInformation()).isEqualTo(information);
		assertThat(detailBean.getStatus()).isEqualTo(status);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(EmployeeChecklistResponse.builder().build()).hasAllNullFieldsOrPropertiesExcept("details").hasFieldOrPropertyWithValue("details", emptyList());
		assertThat(new EmployeeChecklistResponse()).hasAllNullFieldsOrPropertiesExcept("details").hasFieldOrPropertyWithValue("details", emptyList());
		assertThat(EmployeeChecklistResponse.Detail.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new EmployeeChecklistResponse.Detail()).hasAllNullFieldsOrProperties();
	}
}
