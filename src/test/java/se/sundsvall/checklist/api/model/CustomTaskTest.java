package se.sundsvall.checklist.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

class CustomTaskTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CustomTask.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var id = "id";
		final var heading = "heading";
		final var headingReference = "headingReference";
		final var text = "text";
		final var roleType = RoleType.NEW_EMPLOYEE;
		final var questionType = QuestionType.YES_OR_NO;
		final var sortOrder = 911;
		final var lastSavedBy = "someUser";
		final var created = OffsetDateTime.now();
		final var updated = OffsetDateTime.now().plusDays(10);

		final var bean = CustomTask.builder()
			.withId(id)
			.withHeading(heading)
			.withHeadingReference(headingReference)
			.withText(text)
			.withRoleType(roleType)
			.withQuestionType(questionType)
			.withSortOrder(sortOrder)
			.withUpdated(updated)
			.withCreated(created)
			.withLastSavedBy(lastSavedBy)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getHeading()).isEqualTo(heading);
		assertThat(bean.getHeadingReference()).isEqualTo(headingReference);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getLastSavedBy()).isEqualTo(lastSavedBy);
		assertThat(bean.getQuestionType()).isEqualTo(questionType);
		assertThat(bean.getRoleType()).isEqualTo(roleType);
		assertThat(bean.getSortOrder()).isEqualTo(sortOrder);
		assertThat(bean.getText()).isEqualTo(text);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CustomTask.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CustomTask()).hasAllNullFieldsOrProperties();
	}
}
