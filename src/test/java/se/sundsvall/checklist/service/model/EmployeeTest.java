package se.sundsvall.checklist.service.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class EmployeeTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Employee.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var emailAddress = "emailAddress";
		final var givenname = "givenname";
		final var isClassified = true;
		final var lastname = "lastname";
		final var legalId = "legalId";
		final var loginname = "loginname";
		final var mainEmployment = Employment.builder().build();
		final var middlename = "middlename";
		final var personId = "personId";

		final var bean = Employee.builder()
			.withEmailAddress(emailAddress)
			.withGivenname(givenname)
			.withIsClassified(isClassified)
			.withLastname(lastname)
			.withLegalId(legalId)
			.withLoginname(loginname)
			.withMainEmployment(mainEmployment)
			.withMiddlename(middlename)
			.withPersonId(personId)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getEmailAddress()).isEqualTo(emailAddress);
		assertThat(bean.getGivenname()).isEqualTo(givenname);
		assertThat(bean.getIsClassified()).isEqualTo(isClassified);
		assertThat(bean.getLastname()).isEqualTo(lastname);
		assertThat(bean.getLegalId()).isEqualTo(legalId);
		assertThat(bean.getLoginname()).isEqualTo(loginname);
		assertThat(bean.getMainEmployment()).isEqualTo(mainEmployment);
		assertThat(bean.getMiddlename()).isEqualTo(middlename);
		assertThat(bean.getPersonId()).isEqualTo(personId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Employee.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Employee()).hasAllNullFieldsOrProperties();
	}
}
