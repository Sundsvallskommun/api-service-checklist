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

class StakeholderTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Stakeholder.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var email = "email";
		final var firstName = "firstName";
		final var id = "id";
		final var lastName = "lastName";
		final var title = "title";
		final var username = "username";

		final var bean = Stakeholder.builder()
			.withEmail(email)
			.withFirstName(firstName)
			.withId(id)
			.withLastName(lastName)
			.withTitle(title)
			.withUsername(username)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getEmail()).isEqualTo(email);
		assertThat(bean.getFirstName()).isEqualTo(firstName);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getLastName()).isEqualTo(lastName);
		assertThat(bean.getTitle()).isEqualTo(title);
		assertThat(bean.getUsername()).isEqualTo(username);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Stakeholder.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Stakeholder()).hasAllNullFieldsOrProperties();
	}
}
