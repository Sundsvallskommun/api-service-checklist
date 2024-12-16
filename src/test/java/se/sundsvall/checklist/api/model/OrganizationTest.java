package se.sundsvall.checklist.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;

class OrganizationTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> OffsetDateTime.now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Organization.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToStringExcluding("communicationChannel"),
			hasValidBeanEqualsExcluding("communicationChannel"),
			hasValidBeanHashCodeExcluding("communicationChannel")));
	}

	@Test
	void testBuilderMethods() {
		final var checklists = List.of(Checklist.builder().build());
		final var communicationChannels = Set.of(CommunicationChannel.EMAIL);
		final var created = OffsetDateTime.now();
		final var id = "id";
		final var organizationName = "organizationName";
		final var organizationNumber = 3321;
		final var updated = OffsetDateTime.now().plusWeeks(4);

		final var bean = Organization.builder()
			.withChecklists(checklists)
			.withCommunicationChannels(communicationChannels)
			.withCreated(created)
			.withId(id)
			.withOrganizationName(organizationName)
			.withOrganizationNumber(organizationNumber)
			.withUpdated(updated)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getChecklists()).isEqualTo(checklists);
		assertThat(bean.getCommunicationChannels()).isEqualTo(communicationChannels);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getOrganizationName()).isEqualTo(organizationName);
		assertThat(bean.getOrganizationNumber()).isEqualTo(organizationNumber);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Organization.builder().build()).hasAllNullFieldsOrPropertiesExcept("organizationNumber").hasFieldOrPropertyWithValue("organizationNumber", 0);
		assertThat(new Organization()).hasAllNullFieldsOrPropertiesExcept("organizationNumber").hasFieldOrPropertyWithValue("organizationNumber", 0);
	}
}
