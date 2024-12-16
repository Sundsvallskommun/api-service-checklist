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
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

class TaskUpdateRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(TaskUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var heading = "heading";
		final var headingReference = "headingReference";
		final var text = "text";
		final var roleType = RoleType.NEW_EMPLOYEE;
		final var questionType = QuestionType.YES_OR_NO;
		final var sortOrder = 112;
		final var updatedBy = "updatedBy";
		final var permission = SUPERADMIN;

		final var bean = TaskUpdateRequest.builder()
			.withHeading(heading)
			.withHeadingReference(headingReference)
			.withPermission(permission)
			.withQuestionType(questionType)
			.withRoleType(roleType)
			.withSortOrder(sortOrder)
			.withText(text)
			.withUpdatedBy(updatedBy)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getHeading()).isEqualTo(heading);
		assertThat(bean.getHeadingReference()).isEqualTo(headingReference);
		assertThat(bean.getPermission()).isEqualTo(permission);
		assertThat(bean.getQuestionType()).isEqualTo(questionType);
		assertThat(bean.getRoleType()).isEqualTo(roleType);
		assertThat(bean.getSortOrder()).isEqualTo(sortOrder);
		assertThat(bean.getText()).isEqualTo(text);
		assertThat(bean.getUpdatedBy()).isEqualTo(updatedBy);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(TaskUpdateRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new TaskUpdateRequest()).hasAllNullFieldsOrProperties();
	}
}
