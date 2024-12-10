package se.sundsvall.checklist.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.checklist.integration.db.model.enums.ComponentType;

class SortorderEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(SortorderEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testBuilder() {

		final var componentId = "componentId";
		final var componentType = ComponentType.TASK;
		final var id = "id";
		final var municipalityId = "municipalityId";
		final var organizationNumber = 123;
		final var position = 321;

		final var bean = SortorderEntity.builder()
			.withComponentId(componentId)
			.withComponentType(componentType)
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withOrganizationNumber(organizationNumber)
			.withPosition(position)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getComponentId()).isEqualTo(componentId);
		assertThat(bean.getComponentType()).isEqualTo(componentType);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getOrganizationNumber()).isEqualTo(organizationNumber);
		assertThat(bean.getPosition()).isEqualTo(position);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SortorderEntity.builder().build())
			.hasAllNullFieldsOrPropertiesExcept("organizationNumber", "position")
			.hasFieldOrPropertyWithValue("organizationNumber", 0)
			.hasFieldOrPropertyWithValue("position", 0);
		assertThat(new SortorderEntity())
			.hasAllNullFieldsOrPropertiesExcept("organizationNumber", "position")
			.hasFieldOrPropertyWithValue("organizationNumber", 0)
			.hasFieldOrPropertyWithValue("position", 0);

	}
}
