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
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

class CustomTaskCreateRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CustomTaskCreateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var heading = "heading";
		final var headingReference = "headingReference";
		final var text = "text";
		final var questionType = QuestionType.YES_OR_NO;
		final var roleType = RoleType.NEW_MANAGER;

		final var sortOrder = 911;
		final var createdBy = "someUser";

		final var bean = CustomTaskCreateRequest.builder()
			.withHeading(heading)
			.withHeadingReference(headingReference)
			.withText(text)
			.withQuestionType(questionType)
			.withRoleType(roleType)
			.withSortOrder(sortOrder)
			.withCreatedBy(createdBy)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getHeading()).isEqualTo(heading);
		assertThat(bean.getHeadingReference()).isEqualTo(headingReference);
		assertThat(bean.getQuestionType()).isEqualTo(questionType);
		assertThat(bean.getRoleType()).isEqualTo(roleType);
		assertThat(bean.getSortOrder()).isEqualTo(sortOrder);
		assertThat(bean.getText()).isEqualTo(text);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CustomTaskCreateRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CustomTaskCreateRequest()).hasAllNullFieldsOrProperties();
	}
}
