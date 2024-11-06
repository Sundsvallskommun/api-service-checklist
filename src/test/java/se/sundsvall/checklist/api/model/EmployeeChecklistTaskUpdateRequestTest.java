package se.sundsvall.checklist.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.Random;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;

class EmployeeChecklistTaskUpdateRequestTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> FulfilmentStatus.values()[new Random().nextInt(FulfilmentStatus.values().length)], FulfilmentStatus.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(EmployeeChecklistTaskUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var updatedBy = "updatedBy";
		final var fulfilmentStatus = FulfilmentStatus.TRUE;
		final var responseText = "responseText";

		final var bean = EmployeeChecklistTaskUpdateRequest.builder()
			.withUpdatedBy(updatedBy)
			.withFulfilmentStatus(fulfilmentStatus)
			.withResponseText(responseText)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getUpdatedBy()).isEqualTo(updatedBy);
		assertThat(bean.getFulfilmentStatus()).isEqualTo(fulfilmentStatus);
		assertThat(bean.getResponseText()).isEqualTo(responseText);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(EmployeeChecklistTaskUpdateRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new EmployeeChecklistTaskUpdateRequest()).hasAllNullFieldsOrProperties();
	}
}
