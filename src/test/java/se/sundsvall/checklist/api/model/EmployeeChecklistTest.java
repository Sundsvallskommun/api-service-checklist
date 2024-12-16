package se.sundsvall.checklist.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmployeeChecklistTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
		registerValueGenerator(() -> LocalDate.now().plusDays(new Random().nextInt()), LocalDate.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(EmployeeChecklist.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var completed = true;
		final var created = OffsetDateTime.now();
		final var delegatedTo = List.of("delegatedTo");
		final var employee = Stakeholder.builder().build();
		final var endDate = LocalDate.now();
		final var expirationDate = LocalDate.now().plusMonths(6);
		final var id = "id";
		final var locked = true;
		final var manager = Stakeholder.builder().build();
		final var mentor = Mentor.builder().build();
		final var phases = List.of(EmployeeChecklistPhase.builder().build());
		final var startDate = LocalDate.now().minusMonths(6);
		final var updated = OffsetDateTime.now().plusDays(6);

		final var bean = EmployeeChecklist.builder()
			.withCompleted(completed)
			.withCreated(created)
			.withDelegatedTo(delegatedTo)
			.withEmployee(employee)
			.withEndDate(endDate)
			.withExpirationDate(expirationDate)
			.withId(id)
			.withLocked(locked)
			.withManager(manager)
			.withMentor(mentor)
			.withPhases(phases)
			.withStartDate(startDate)
			.withUpdated(updated)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getCompleted()).isEqualTo(completed);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getDelegatedTo()).isEqualTo(delegatedTo);
		assertThat(bean.getEmployee()).isEqualTo(employee);
		assertThat(bean.getEndDate()).isEqualTo(endDate);
		assertThat(bean.getExpirationDate()).isEqualTo(expirationDate);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.isLocked()).isEqualTo(locked);
		assertThat(bean.getManager()).isEqualTo(manager);
		assertThat(bean.getMentor()).isEqualTo(mentor);
		assertThat(bean.getPhases()).isEqualTo(phases);
		assertThat(bean.getStartDate()).isEqualTo(startDate);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(EmployeeChecklist.builder().build()).hasAllNullFieldsOrPropertiesExcept("locked", "phases")
			.hasFieldOrPropertyWithValue("locked", false)
			.hasFieldOrPropertyWithValue("phases", emptyList());

		assertThat(new EmployeeChecklist()).hasAllNullFieldsOrPropertiesExcept("locked", "phases")
			.hasFieldOrPropertyWithValue("locked", false)
			.hasFieldOrPropertyWithValue("phases", emptyList());
	}
}
