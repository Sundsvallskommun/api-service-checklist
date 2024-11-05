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

import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

class EmployeeChecklistTaskTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> RoleType.values()[new Random().nextInt(RoleType.values().length)], RoleType.class);
		registerValueGenerator(() -> CorrespondenceStatus.values()[new Random().nextInt(CorrespondenceStatus.values().length)], CorrespondenceStatus.class);
		registerValueGenerator(() -> CommunicationChannel.values()[new Random().nextInt(CommunicationChannel.values().length)], CommunicationChannel.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(EmployeeChecklistTask.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var customTask = true;
		final var fulfilmentStatus = FulfilmentStatus.FALSE;
		final var heading = "heading";
		final var id = "id";
		final var updatedBy = "updatedBy";
		final var questionType = QuestionType.YES_OR_NO_WITH_TEXT;
		final var responseText = "responseText";
		final var roleType = RoleType.MANAGER;
		final var sortOrder = 123;
		final var text = "text";
		final var updated = OffsetDateTime.now();

		final var bean = EmployeeChecklistTask.builder()
			.withCustomTask(customTask)
			.withFulfilmentStatus(fulfilmentStatus)
			.withHeading(heading)
			.withId(id)
			.withUpdatedBy(updatedBy)
			.withQuestionType(questionType)
			.withResponseText(responseText)
			.withRoleType(roleType)
			.withSortOrder(sortOrder)
			.withText(text)
			.withUpdated(updated)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.isCustomTask()).isEqualTo(customTask);
		assertThat(bean.getFulfilmentStatus()).isEqualTo(fulfilmentStatus);
		assertThat(bean.getHeading()).isEqualTo(heading);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getUpdatedBy()).isEqualTo(updatedBy);
		assertThat(bean.getQuestionType()).isEqualTo(questionType);
		assertThat(bean.getResponseText()).isEqualTo(responseText);
		assertThat(bean.getRoleType()).isEqualTo(roleType);
		assertThat(bean.getSortOrder()).isEqualTo(sortOrder);
		assertThat(bean.getText()).isEqualTo(text);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(EmployeeChecklistTask.builder().build()).hasAllNullFieldsOrPropertiesExcept("customTask").extracting(EmployeeChecklistTask::isCustomTask).isEqualTo(false);
		assertThat(new EmployeeChecklistTask()).hasAllNullFieldsOrPropertiesExcept("customTask").extracting(EmployeeChecklistTask::isCustomTask).isEqualTo(false);
	}
}
