package se.sundsvall.checklist.api.model;

import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class OrganizationUpdateRequestTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(OrganizationUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanToString(),
			hasValidBeanEquals(),
			hasValidBeanHashCode()));
	}

	@Test
	void testBuilderMethods() {
		final var communicationChannels = Set.of(CommunicationChannel.EMAIL);
		final var organizationName = "organizationName";

		final var bean = OrganizationUpdateRequest.builder()
			.withCommunicationChannels(communicationChannels)
			.withOrganizationName(organizationName)
			.build();

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getCommunicationChannels()).isEqualTo(communicationChannels);
		assertThat(bean.getOrganizationName()).isEqualTo(organizationName);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(OrganizationUpdateRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new OrganizationUpdateRequest()).hasAllNullFieldsOrProperties();
	}
}
