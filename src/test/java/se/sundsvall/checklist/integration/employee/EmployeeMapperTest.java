package se.sundsvall.checklist.integration.employee;

import static org.assertj.core.api.Assertions.assertThat;

import generated.se.sundsvall.employee.Account;
import generated.se.sundsvall.employee.Employeev2;
import generated.se.sundsvall.employee.Employment;
import generated.se.sundsvall.employee.Manager;
import generated.se.sundsvall.employee.NewEmployee;
import generated.se.sundsvall.employee.NewEmployment;
import generated.se.sundsvall.employee.ReferenceNumberCompany;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.checklist.service.model.Employee;

class EmployeeMapperTest {
	private static final int COMPANY_ID = 123;
	private static final String DOMAIN = "domain";
	private static final String EMAIL_ADDRESS = "emailAddress";
	private static final String LOGINNAME = "loginname";
	private static final String GIVENNAME = "givenname";
	private static final String LASTNAME = "lastname";
	private static final String MIDDLENAME = "middlename";
	private static final UUID PERSON_ID = UUID.randomUUID();
	private static final String PERSON_NUMBER = "personNumber";
	private static final String AID = "aid";
	private static final int BENEFIT_GROUP_ID = 123;
	private static final int EMPLOYMENT_TYPE = 456;
	private static final boolean IS_MAIN_EMPLOYMENT = true;
	private static final String EMP_ROW_ID = "empRowId";
	private static final Date END_DATE = new Date(RandomUtils.secure().randomLong());
	private static final String EVENT_TYPE = "eventType";
	private static final String EVENT_INFO = "eventInfo";
	private static final String FORM_OF_EMPLOYMENT_ID = "formOfEmploymentId";
	private static final int ORG_ID = 789;
	private static final String ORG_NAME = "orgName";
	private static final String PA_TEAM = "paTeam";
	private static final Date START_DATE = new Date(RandomUtils.secure().randomLong());
	private static final String TITLE = "title";
	private static final int TOP_ORG_ID = 912;
	private static final String TOP_ORG_NAME = "topOrgName";
	private static final String MANAGER_EMAIL_ADDRESS = "managerEmailAddress";
	private static final String MANAGER_GIVENNAME = "managerGivenname";
	private static final String MANAGER_LASTNAME = "managerLastname";
	private static final String MANAGER_LOGINNAME = "managerLoginname";
	private static final String MANAGER_MIDDLENAME = "managerMiddlename";
	private static final UUID MANAGER_PERSON_ID = UUID.randomUUID();
	private static final String MANAGER_REFERENCE_NUMBER = "managerReferenceNumber";
	private static final String MANAGER_CODE = "managerCode";
	private static final String REFERENCE_NUMBER = "referenceNumber";

	@Test
	void toEmployeeFromNull() {
		// Act and assert
		assertThat(EmployeeMapper.toEmployee((Employeev2) null)).isNull();
		assertThat(EmployeeMapper.toEmployee((NewEmployee) null)).isNull();
	}

	@Test
	void toEmployeeFromEmptyObject() {
		// Act and assert
		assertThat(EmployeeMapper.toEmployee(new Employeev2())).hasAllNullFieldsOrProperties();
		assertThat(EmployeeMapper.toEmployee(new NewEmployee())).hasAllNullFieldsOrProperties();
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void toEmployeeFromFullEmployeev2Object(boolean value) {
		// Arrange
		final var input = new Employeev2()
			.accounts(List.of(new Account()
				.companyId(COMPANY_ID)
				.domain(DOMAIN)
				.emailAddress(EMAIL_ADDRESS)
				.loginname(LOGINNAME)))
			.employments(List.of(new Employment()
				.aid(AID)
				.benefitGroupId(BENEFIT_GROUP_ID)
				.companyId(COMPANY_ID)
				.employmentType(EMPLOYMENT_TYPE)
				.isMainEmployment(IS_MAIN_EMPLOYMENT)
				.isManager(value)
				.isManual(value)
				.empRowId(EMP_ROW_ID)
				.endDate(END_DATE)
				.formOfEmploymentId(FORM_OF_EMPLOYMENT_ID)
				.manager(new Manager()
					.emailAddress(MANAGER_EMAIL_ADDRESS)
					.givenname(MANAGER_GIVENNAME)
					.lastname(MANAGER_LASTNAME)
					.loginname(MANAGER_LOGINNAME)
					.middlename(MANAGER_MIDDLENAME)
					.personId(MANAGER_PERSON_ID)
					.referenceNumber(MANAGER_REFERENCE_NUMBER))
				.managerCode(MANAGER_CODE)
				.orgId(ORG_ID)
				.orgName(ORG_NAME)
				.paTeam(PA_TEAM)
				.startDate(START_DATE)
				.title(TITLE)
				.topOrgId(TOP_ORG_ID)
				.topOrgName(TOP_ORG_NAME)))
			.givenname(GIVENNAME)
			.isClassified(value)
			.lastname(LASTNAME)
			.middlename(MIDDLENAME)
			.personId(PERSON_ID)
			.personNumber(PERSON_NUMBER)
			.referenceNumbers(List.of(new ReferenceNumberCompany()
				.companyId(COMPANY_ID)
				.referenceNumber(REFERENCE_NUMBER)));

		// Act
		final var result = EmployeeMapper.toEmployee(input);

		assertThat(result).hasNoNullFieldsOrPropertiesExcept("mainEmployment.eventType");

		// Assert employee
		assertEmployee(value, result);

		// Assert main employment
		assertThat(result.getMainEmployment().getEventType()).isNull(); // Not evaulated in the common assert method
		assertMainEmployment(value, result.getMainEmployment());

		// Assert main employment manager
		assertMainEmploymentManager(result.getMainEmployment().getManager());
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void toEmployeeFromFullNewEmployeeObject(boolean value) {
		// Arrange
		final var input = new NewEmployee()
			.accounts(List.of(new Account()
				.companyId(COMPANY_ID)
				.domain(DOMAIN)
				.emailAddress(EMAIL_ADDRESS)
				.loginname(LOGINNAME)))
			.employments(List.of(new NewEmployment()
				.aid(AID)
				.benefitGroupId(BENEFIT_GROUP_ID)
				.companyId(COMPANY_ID)
				.employmentType(EMPLOYMENT_TYPE)
				.eventInfo(EVENT_INFO)
				.eventType(EVENT_TYPE)
				.isMainEmployment(IS_MAIN_EMPLOYMENT)
				.isManager(value)
				.isManual(value)
				.empRowId(EMP_ROW_ID)
				.endDate(END_DATE)
				.formOfEmploymentId(FORM_OF_EMPLOYMENT_ID)
				.manager(new Manager()
					.emailAddress(MANAGER_EMAIL_ADDRESS)
					.givenname(MANAGER_GIVENNAME)
					.lastname(MANAGER_LASTNAME)
					.loginname(MANAGER_LOGINNAME)
					.middlename(MANAGER_MIDDLENAME)
					.personId(MANAGER_PERSON_ID)
					.referenceNumber(MANAGER_REFERENCE_NUMBER))
				.managerCode(MANAGER_CODE)
				.orgId(ORG_ID)
				.orgName(ORG_NAME)
				.paTeam(PA_TEAM)
				.startDate(START_DATE)
				.title(TITLE)
				.topOrgId(TOP_ORG_ID)
				.topOrgName(TOP_ORG_NAME)))
			.givenname(GIVENNAME)
			.isClassified(value)
			.lastname(LASTNAME)
			.middlename(MIDDLENAME)
			.personId(PERSON_ID)
			.personNumber(PERSON_NUMBER)
			.referenceNumbers(List.of(new ReferenceNumberCompany()
				.companyId(COMPANY_ID)
				.referenceNumber(REFERENCE_NUMBER)));

		// Act
		final var result = EmployeeMapper.toEmployee(input);

		assertThat(result).hasNoNullFieldsOrProperties();

		// Assert employee
		assertEmployee(value, result);

		// Assert main employment
		assertThat(result.getMainEmployment().getEventType()).isEqualTo(EVENT_TYPE); // Not evaulated in the common assert method
		assertMainEmployment(value, result.getMainEmployment());

		// Assert main employment manager
		assertMainEmploymentManager(result.getMainEmployment().getManager());
	}

	@Test
	void toEmployeeFromEmployeev2WhenNoAccountMatchingMainEmployment() {
		final var input = new Employeev2()
			.accounts(List.of(new Account()
				.companyId(COMPANY_ID)
				.domain(DOMAIN)
				.emailAddress(EMAIL_ADDRESS)
				.loginname(LOGINNAME)))
			.employments(List.of(new Employment()
				.companyId(COMPANY_ID + 1)
				.isMainEmployment(true)));

		// Act
		final var result = EmployeeMapper.toEmployee(input);

		// Assert
		assertThat(result.getMainEmployment()).isNotNull();
		assertThat(result.getEmailAddress()).isNull();
		assertThat(result.getLoginname()).isNull();
	}

	@Test
	void toEmployeeFromNewEmployeeWhenNoAccountMatchingMainEmployment() {
		final var input = new NewEmployee()
			.accounts(List.of(new Account()
				.companyId(COMPANY_ID)
				.domain(DOMAIN)
				.emailAddress(EMAIL_ADDRESS)
				.loginname(LOGINNAME)))
			.employments(List.of(new NewEmployment()
				.companyId(COMPANY_ID + 1)
				.isMainEmployment(true)));

		// Act
		final var result = EmployeeMapper.toEmployee(input);

		// Assert
		assertThat(result.getMainEmployment()).isNotNull();
		assertThat(result.getEmailAddress()).isNull();
		assertThat(result.getLoginname()).isNull();
	}

	@Test
	void toEmployeeFromEmployeev2WhenNoMainEmployment() {
		final var input = new Employeev2()
			.accounts(List.of(new Account()
				.companyId(COMPANY_ID)
				.domain(DOMAIN)
				.emailAddress(EMAIL_ADDRESS)
				.loginname(LOGINNAME)))
			.employments(List.of(new Employment()
				.companyId(COMPANY_ID)
				.isMainEmployment(false)));

		// Act
		final var result = EmployeeMapper.toEmployee(input);

		// Assert
		assertThat(result.getMainEmployment()).isNull();
		assertThat(result.getEmailAddress()).isNull();
		assertThat(result.getLoginname()).isNull();
	}

	@Test
	void toEmployeeFromNewEmployeeWhenNoMainEmployment() {
		final var input = new NewEmployee()
			.accounts(List.of(new Account()
				.companyId(COMPANY_ID)
				.domain(DOMAIN)
				.emailAddress(EMAIL_ADDRESS)
				.loginname(LOGINNAME)))
			.employments(List.of(new NewEmployment()
				.companyId(COMPANY_ID)
				.isMainEmployment(false)));

		// Act
		final var result = EmployeeMapper.toEmployee(input);

		// Assert
		assertThat(result.getMainEmployment()).isNull();
		assertThat(result.getEmailAddress()).isNull();
		assertThat(result.getLoginname()).isNull();
	}

	private static void assertMainEmploymentManager(final se.sundsvall.checklist.service.model.Manager bean) {
		assertThat(bean.getEmailAddress()).isEqualTo(MANAGER_EMAIL_ADDRESS);
		assertThat(bean.getGivenname()).isEqualTo(MANAGER_GIVENNAME);
		assertThat(bean.getLastname()).isEqualTo(MANAGER_LASTNAME);
		assertThat(bean.getLoginname()).isEqualTo(MANAGER_LOGINNAME);
		assertThat(bean.getPersonId()).isEqualTo(MANAGER_PERSON_ID.toString());
	}

	private static void assertMainEmployment(boolean value, final se.sundsvall.checklist.service.model.Employment bean) {
		assertThat(bean.getBenefitGroupId()).isEqualTo(BENEFIT_GROUP_ID);
		assertThat(bean.getCompanyId()).isEqualTo(COMPANY_ID);
		assertThat(bean.getEmploymentType()).isEqualTo(EMPLOYMENT_TYPE);
		assertThat(bean.getEndDate()).isEqualTo(END_DATE);
		assertThat(bean.getFormOfEmploymentId()).isEqualTo(FORM_OF_EMPLOYMENT_ID);
		assertThat(bean.getIsMainEmployment()).isEqualTo(IS_MAIN_EMPLOYMENT);
		assertThat(bean.getIsManager()).isEqualTo(value);
		assertThat(bean.getIsManual()).isEqualTo(value);
		assertThat(bean.getManagerCode()).isEqualTo(MANAGER_CODE);
		assertThat(bean.getOrgId()).isEqualTo(ORG_ID);
		assertThat(bean.getOrgName()).isEqualTo(ORG_NAME);
		assertThat(bean.getStartDate()).isEqualTo(START_DATE);
		assertThat(bean.getTitle()).isEqualTo(TITLE);
		assertThat(bean.getTopOrgId()).isEqualTo(TOP_ORG_ID);
		assertThat(bean.getTopOrgName()).isEqualTo(TOP_ORG_NAME);
	}

	private static void assertEmployee(boolean value, final Employee bean) {
		assertThat(bean.getEmailAddress()).isEqualTo(EMAIL_ADDRESS);
		assertThat(bean.getGivenname()).isEqualTo(GIVENNAME);
		assertThat(bean.getIsClassified()).isEqualTo(value);
		assertThat(bean.getLastname()).isEqualTo(LASTNAME);
		assertThat(bean.getLegalId()).isEqualTo(PERSON_NUMBER);
		assertThat(bean.getLoginname()).isEqualTo(LOGINNAME);
		assertThat(bean.getMiddlename()).isEqualTo(MIDDLENAME);
		assertThat(bean.getPersonId()).isEqualTo(PERSON_ID.toString());
	}
}
