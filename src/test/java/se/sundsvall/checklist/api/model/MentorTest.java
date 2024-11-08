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

class MentorTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(Mentor.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var userId = "someUserId";
		final var name = "someName";

		final var mentor = Mentor.builder()
			.withUserId(userId)
			.withName(name)
			.build();

		assertThat(mentor).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(mentor.getUserId()).isEqualTo(userId);
		assertThat(mentor.getName()).isEqualTo(name);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Mentor.builder().build()).hasAllNullFieldsOrProperties();
	}
}
