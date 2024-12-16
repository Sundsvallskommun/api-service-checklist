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

class TaskCreateRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(TaskCreateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var createdBy = "createdBy";
		final var heading = "heading";
		final var headingReference = "headingReference";
		final var text = "text";
		final var roleType = RoleType.NEW_EMPLOYEE;
		final var questionType = QuestionType.YES_OR_NO;
		final var sortOrder = 911;
		final var permission = SUPERADMIN;

		final var bean = TaskCreateRequest.builder()
			.withCreatedBy(createdBy)
			.withHeading(heading)
			.withHeadingReference(headingReference)
			.withPermission(permission)
			.withQuestionType(questionType)
			.withRoleType(roleType)
			.withSortOrder(sortOrder)
			.withText(text)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getHeading()).isEqualTo(heading);
		assertThat(bean.getHeadingReference()).isEqualTo(headingReference);
		assertThat(bean.getPermission()).isEqualTo(permission);
		assertThat(bean.getQuestionType()).isEqualTo(questionType);
		assertThat(bean.getRoleType()).isEqualTo(roleType);
		assertThat(bean.getSortOrder()).isEqualTo(sortOrder);
		assertThat(bean.getText()).isEqualTo(text);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(TaskCreateRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new TaskCreateRequest()).hasAllNullFieldsOrProperties();
	}
}
