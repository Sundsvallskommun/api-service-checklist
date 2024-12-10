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
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;

class ChecklistTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Checklist.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var id = "id";
		final var name = "name";
		final var displayName = "displayName";
		final var version = 123;
		final var lifeCycle = LifeCycle.ACTIVE;
		final var lastSavedBy = "someUser";
		final var created = OffsetDateTime.now();
		final var updated = OffsetDateTime.now().plusDays(10);
		final var phases = List.of(Phase.builder().build());

		final var bean = Checklist.builder()
			.withId(id)
			.withName(name)
			.withVersion(version)
			.withLifeCycle(lifeCycle)
			.withDisplayName(displayName)
			.withUpdated(updated)
			.withCreated(created)
			.withLastSavedBy(lastSavedBy)
			.withPhases(phases)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getDisplayName()).isEqualTo(displayName);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getLastSavedBy()).isEqualTo(lastSavedBy);
		assertThat(bean.getLifeCycle()).isEqualByComparingTo(lifeCycle);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getPhases()).isEqualTo(phases);
		assertThat(bean.getUpdated()).isEqualTo(updated);
		assertThat(bean.getVersion()).isEqualTo(version);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Checklist.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Checklist()).hasAllNullFieldsOrProperties();
	}
}
