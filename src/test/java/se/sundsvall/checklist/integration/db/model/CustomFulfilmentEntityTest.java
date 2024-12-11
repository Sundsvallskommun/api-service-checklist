package se.sundsvall.checklist.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;

class CustomFulfilmentEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(CustomFulfilmentEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testBuilder() {

		final var completed = FulfilmentStatus.TRUE;
		final var customTask = CustomTaskEntity.builder().build();
		final var employeeChecklist = EmployeeChecklistEntity.builder().build();
		final var id = "id";
		final var lastSavedBy = "lastSavedBy";
		final var responseText = "responseText";
		final var updated = OffsetDateTime.now();

		final var bean = CustomFulfilmentEntity.builder()
			.withCompleted(completed)
			.withCustomTask(customTask)
			.withEmployeeChecklist(employeeChecklist)
			.withId(id)
			.withLastSavedBy(lastSavedBy)
			.withResponseText(responseText)
			.withUpdated(updated)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getCompleted()).isEqualTo(completed);
		assertThat(bean.getCustomTask()).isEqualTo(customTask);
		assertThat(bean.getEmployeeChecklist()).isEqualTo(employeeChecklist);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getLastSavedBy()).isEqualTo(lastSavedBy);
		assertThat(bean.getResponseText()).isEqualTo(responseText);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CustomFulfilmentEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CustomFulfilmentEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void preUpdateTest() {
		final var entity = CustomFulfilmentEntity.builder().build();

		entity.preUpdate();

		assertThat(entity.getUpdated())
			.isNotNull()
			.isCloseTo(now(systemDefault()), within(2, SECONDS));
	}
}
