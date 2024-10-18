package se.sundsvall.checklist.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.EMPLOYEE;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.MANAGER;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;

@ExtendWith(MockitoExtension.class)
class OrganizationMapperTest {

	@Mock
	private EmployeeEntity employeeEntityMock;

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void toEmployeeEntity(boolean manager) {
		// Arrange
		final var emailAddress = "emailAddress";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var uuid = UUID.randomUUID();
		final var loginName = "loginName";
		final var startDate = Date.from(LocalDate.of(2023, 12, 4).atStartOfDay(ZoneId.systemDefault()).toInstant());
		final var title = "title";

		final var employee = new Employee()
			.emailAddress(emailAddress)
			.givenname(firstName)
			.lastname(lastName)
			.personId(uuid)
			.isManager(manager)
			.loginname(loginName)
			.addEmploymentsItem(new Employment().isMainEmployment(false).startDate(startDate).title("this should not be selected"))
			.addEmploymentsItem(new Employment().startDate(startDate).title("this should not be selected"))
			.addEmploymentsItem(new Employment().isMainEmployment(true).startDate(startDate).title(title));

		// Act
		final var entity = OrganizationMapper.toEmployeeEntity(employee);

		// Assert
		assertThat(entity.getCompany()).isNull();
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getDepartment()).isNull();
		assertThat(entity.getEmail()).isEqualTo(emailAddress);
		assertThat(entity.getFirstName()).isEqualTo(firstName);
		assertThat(entity.getId()).isEqualTo(uuid.toString());
		assertThat(entity.getLastName()).isEqualTo(lastName);
		assertThat(entity.getManager()).isNull();
		assertThat(entity.getEmployeeChecklist()).isNull();
		assertThat(entity.getRoleType()).isEqualTo(manager ? MANAGER : EMPLOYEE);
		assertThat(entity.getTitle()).isEqualTo(title);
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getUsername()).isEqualTo(loginName);
	}

	@Test
	void toEmployeeEntityWhenNoManagerStartDateOrTitleInformation() {
		// Arrange
		final var emailAddress = "emailAddress";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var uuid = UUID.randomUUID();
		final var loginName = "loginName";

		final var employee = new Employee()
			.emailAddress(emailAddress)
			.givenname(firstName)
			.lastname(lastName)
			.personId(uuid)
			.loginname(loginName)
			.addEmploymentsItem(new Employment().isMainEmployment(true));

		// Act
		final var entity = OrganizationMapper.toEmployeeEntity(employee);

		// Assert
		assertThat(entity.getCompany()).isNull();
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getDepartment()).isNull();
		assertThat(entity.getEmail()).isEqualTo(emailAddress);
		assertThat(entity.getFirstName()).isEqualTo(firstName);
		assertThat(entity.getId()).isEqualTo(uuid.toString());
		assertThat(entity.getLastName()).isEqualTo(lastName);
		assertThat(entity.getManager()).isNull();
		assertThat(entity.getEmployeeChecklist()).isNull();
		assertThat(entity.getRoleType()).isEqualTo(EMPLOYEE);
		assertThat(entity.getStartDate()).isToday();
		assertThat(entity.getTitle()).isNull();
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getUsername()).isEqualTo(loginName);
	}

	@Test
	void toEmployeeEntityFromNull() {
		assertThat(OrganizationMapper.toEmployeeEntity(null)).isNull();
	}

	@Test
	void toManagerEntityFromNull() {
		assertThat(OrganizationMapper.toManagerEntity(null)).isNull();
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void updateEmployee(boolean manager) {
		// Arrange
		final var emailAddress = "emailAddress";
		final var firstName = "firstName";
		final var lastName = "lastName";
		final var uuid = UUID.randomUUID();
		final var loginName = "loginName";
		final var startDate = Date.from(LocalDate.of(2023, 12, 4).atStartOfDay(ZoneId.systemDefault()).toInstant());
		final var title = "title";

		final var employee = new Employee()
			.emailAddress(emailAddress)
			.givenname(firstName)
			.lastname(lastName)
			.personId(UUID.randomUUID())
			.isManager(manager)
			.loginname(loginName)
			.addEmploymentsItem(new Employment().isMainEmployment(false).startDate(startDate).title("this should not be selected"))
			.addEmploymentsItem(new Employment().startDate(startDate).title("this should not be selected"))
			.addEmploymentsItem(new Employment().isMainEmployment(true).startDate(startDate).title(title));

		final var entity = EmployeeEntity.builder().withId(uuid.toString()).build();

		// Act
		OrganizationMapper.updateEmployeeEntity(entity, employee);

		// Assert
		assertThat(entity.getCompany()).isNull();
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getDepartment()).isNull();
		assertThat(entity.getEmail()).isEqualTo(emailAddress);
		assertThat(entity.getFirstName()).isEqualTo(firstName);
		assertThat(entity.getId()).isEqualTo(uuid.toString());
		assertThat(entity.getLastName()).isEqualTo(lastName);
		assertThat(entity.getManager()).isNull();
		assertThat(entity.getEmployeeChecklist()).isNull();
		assertThat(entity.getRoleType()).isEqualTo(manager ? MANAGER : EMPLOYEE);
		assertThat(entity.getTitle()).isEqualTo(title);
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getUsername()).isEqualTo(loginName);
	}

	@Test
	void updateEmployeeFromNull() {
		// Act
		OrganizationMapper.updateEmployeeEntity(employeeEntityMock, null);

		// Assert
		verifyNoInteractions(employeeEntityMock);
	}

	@Test
	void toOrganizationEntityWithChecklist() {
		// Arrange
		final var organizationName = "organizationName";
		final var organizationNumber = 12345;
		final var checklistId = UUID.randomUUID().toString();

		// Act
		final var entity = OrganizationMapper.toOrganizationEntity(organizationNumber, organizationName);
		entity.getChecklists().add(ChecklistEntity.builder().withId(checklistId).build());

		// Assert
		assertThat(entity.getChecklists()).hasSize(1).allSatisfy(ch -> Objects.equals(checklistId, ch.getId()));
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getOrganizationName()).isEqualTo(organizationName);
		assertThat(entity.getOrganizationNumber()).isEqualTo(organizationNumber);
		assertThat(entity.getUpdated()).isNull();
	}

	@Test
	void toOrganizationEntityWithoutChecklist() {
		// Arrange
		final var organizationName = "organizationName";
		final var organizationNumber = 12345;

		// Act
		final var entity = OrganizationMapper.toOrganizationEntity(organizationNumber, organizationName);

		// Assert
		assertThat(entity.getChecklists()).isEmpty();
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getOrganizationName()).isEqualTo(organizationName);
		assertThat(entity.getOrganizationNumber()).isEqualTo(organizationNumber);
		assertThat(entity.getUpdated()).isNull();
	}

	@Test
	void toStakeholderFromEmployee() {
		// Arrange
		final var email = "email";
		final var firstName = "firstName";
		final var id = UUID.randomUUID().toString();
		final var lastName = "lastName";
		final var username = "username";

		final var entity = EmployeeEntity.builder()
			.withEmail(email)
			.withFirstName(firstName)
			.withId(id)
			.withLastName(lastName)
			.withUsername(username)
			.build();

		// Act
		final var stakeholder = OrganizationMapper.toStakeholder(entity);

		// Assert
		assertThat(stakeholder.getEmail()).isEqualTo(email);
		assertThat(stakeholder.getFirstName()).isEqualTo(firstName);
		assertThat(stakeholder.getId()).isEqualTo(id);
		assertThat(stakeholder.getLastName()).isEqualTo(lastName);
		assertThat(stakeholder.getUsername()).isEqualTo(username);
	}

	@Test
	void toStakeholderFromManager() {
		// Arrange
		final var email = "email";
		final var firstName = "firstName";
		final var id = UUID.randomUUID().toString();
		final var lastName = "lastName";
		final var username = "username";

		final var entity = ManagerEntity.builder()
			.withEmail(email)
			.withFirstName(firstName)
			.withPersonId(id)
			.withLastName(lastName)
			.withUsername(username)
			.build();

		// Act
		final var stakeholder = OrganizationMapper.toStakeholder(entity);

		// Assert
		assertThat(stakeholder.getEmail()).isEqualTo(email);
		assertThat(stakeholder.getFirstName()).isEqualTo(firstName);
		assertThat(stakeholder.getId()).isEqualTo(id);
		assertThat(stakeholder.getLastName()).isEqualTo(lastName);
		assertThat(stakeholder.getUsername()).isEqualTo(username);
	}

	@Test
	void toStakeholderEmployeeNullTest() {
		assertThat(OrganizationMapper.toStakeholder((EmployeeEntity) null)).isNull();
	}

	@Test
	void toStakeholderManagerNullTest() {
		assertThat(OrganizationMapper.toStakeholder((ManagerEntity) null)).isNull();
	}

	@Test
	void toOrganization() {
		final var entity = OrganizationEntity.builder()
			.withCommunicationChannels(Set.of(CommunicationChannel.NO_COMMUNICATION))
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withId(UUID.randomUUID().toString())
			.withOrganizationName("organizationName")
			.withOrganizationNumber(123456)
			.withUpdated(OffsetDateTime.now())
			.build();

		final var organization = OrganizationMapper.toOrganization(entity);

		assertThat(organization.getCommunicationChannels()).containsExactly(CommunicationChannel.NO_COMMUNICATION);
		assertThat(organization.getCreated()).isEqualTo(entity.getCreated());
		assertThat(organization.getId()).isEqualTo(entity.getId());
		assertThat(organization.getOrganizationName()).isEqualTo(entity.getOrganizationName());
		assertThat(organization.getOrganizationNumber()).isEqualTo(entity.getOrganizationNumber());
		assertThat(organization.getUpdated()).isEqualTo(entity.getUpdated());
	}

	@Test
	void toOrganizations() {
		final var entity = OrganizationEntity.builder()
			.withCommunicationChannels(Set.of(CommunicationChannel.NO_COMMUNICATION))
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withId(UUID.randomUUID().toString())
			.withOrganizationName("organizationName")
			.withOrganizationNumber(123456)
			.withUpdated(OffsetDateTime.now())
			.build();

		final var organizations = OrganizationMapper.toOrganizations(List.of(entity));

		assertThat(organizations).containsExactly(OrganizationMapper.toOrganization(entity));
	}

	@Test
	void toOrganizationsFromNull() {
		assertThat(OrganizationMapper.toOrganizations(null)).isEmpty();
	}
}
