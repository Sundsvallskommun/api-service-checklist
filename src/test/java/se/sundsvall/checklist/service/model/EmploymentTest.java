package se.sundsvall.checklist.service.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.util.Date;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmploymentTest {
	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> new Date(new Random().nextLong()), Date.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(Employment.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var benefitGroupId = 112;
		final var companyId = 911;
		final var employmentType = 123;
		final var endDate = new Date(new Random().nextLong());
		final var eventType = "eventType";
		final var formOfEmploymentId = "formOfEmploymentId";
		final var isMainEmployment = true;
		final var isManager = false;
		final var isManual = true;
		final var manager = Manager.builder().build();
		final var managerCode = "managerCode";
		final var orgId = 321;
		final var orgName = "orgName";
		final var startDate = new Date(new Random().nextLong());
		final var title = "title";
		final var topOrgId = 313;
		final var topOrgName = "topOrgName";

		final var bean = Employment.builder()
			.withBenefitGroupId(benefitGroupId)
			.withCompanyId(companyId)
			.withEmploymentType(employmentType)
			.withEndDate(endDate)
			.withEventType(eventType)
			.withFormOfEmploymentId(formOfEmploymentId)
			.withIsMainEmployment(isMainEmployment)
			.withIsManager(isManager)
			.withIsManual(isManual)
			.withManager(manager)
			.withManagerCode(managerCode)
			.withOrgId(orgId)
			.withOrgName(orgName)
			.withStartDate(startDate)
			.withTitle(title)
			.withTopOrgId(topOrgId)
			.withTopOrgName(topOrgName)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getBenefitGroupId()).isEqualTo(benefitGroupId);
		assertThat(bean.getCompanyId()).isEqualTo(companyId);
		assertThat(bean.getEmploymentType()).isEqualTo(employmentType);
		assertThat(bean.getEndDate()).isEqualTo(endDate);
		assertThat(bean.getEventType()).isEqualTo(eventType);
		assertThat(bean.getFormOfEmploymentId()).isEqualTo(formOfEmploymentId);
		assertThat(bean.getIsMainEmployment()).isEqualTo(isMainEmployment);
		assertThat(bean.getIsManager()).isEqualTo(isManager);
		assertThat(bean.getIsManual()).isEqualTo(isManual);
		assertThat(bean.getManager()).isEqualTo(manager);
		assertThat(bean.getManagerCode()).isEqualTo(managerCode);
		assertThat(bean.getOrgId()).isEqualTo(orgId);
		assertThat(bean.getOrgName()).isEqualTo(orgName);
		assertThat(bean.getStartDate()).isEqualTo(startDate);
		assertThat(bean.getTitle()).isEqualTo(title);
		assertThat(bean.getTopOrgId()).isEqualTo(topOrgId);
		assertThat(bean.getTopOrgName()).isEqualTo(topOrgName);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Employment.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new Employment()).hasAllNullFieldsOrProperties();
	}
}
