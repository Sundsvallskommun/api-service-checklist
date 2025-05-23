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

class ManagerTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Manager.class, allOf(
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
		final var lastname = "lastname";
		final var loginname = "loginname";
		final var personId = "personId";

		final var bean = Manager.builder()
			.withEmailAddress(emailAddress)
			.withGivenname(givenname)
			.withLastname(lastname)
			.withLoginname(loginname)
			.withPersonId(personId)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getEmailAddress()).isEqualTo(emailAddress);
		assertThat(bean.getGivenname()).isEqualTo(givenname);
		assertThat(bean.getLastname()).isEqualTo(lastname);
		assertThat(bean.getLoginname()).isEqualTo(loginname);
		assertThat(bean.getPersonId()).isEqualTo(personId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Manager.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Manager()).hasAllNullFieldsOrProperties();
	}
}
