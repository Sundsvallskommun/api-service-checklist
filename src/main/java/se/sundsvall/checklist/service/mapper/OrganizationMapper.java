package se.sundsvall.checklist.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.NO_COMMUNICATION;
import static se.sundsvall.checklist.integration.db.model.enums.EmploymentPosition.EMPLOYEE;
import static se.sundsvall.checklist.integration.db.model.enums.EmploymentPosition.MANAGER;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import generated.se.sundsvall.employee.Manager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import se.sundsvall.checklist.api.model.Organization;
import se.sundsvall.checklist.api.model.OrganizationCreateRequest;
import se.sundsvall.checklist.api.model.OrganizationUpdateRequest;
import se.sundsvall.checklist.api.model.Stakeholder;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;

public class OrganizationMapper {
	private OrganizationMapper() {}

	// -----------------------------
	// Entity mappings
	// -----------------------------

	public static EmployeeEntity toEmployeeEntity(final Employee employeeProspect) {
		return ofNullable(employeeProspect)
			.map(employee -> EmployeeEntity.builder()
				.withEmail(employee.getEmailAddress())
				.withFirstName(employee.getGivenname())
				.withLastName(employee.getLastname())
				.withId(employee.getPersonId().toString())
				.withEmploymentPosition(ofNullable(employee.getIsManager()).orElse(false) ? MANAGER : EMPLOYEE)
				.withStartDate(getStartDate(employee))
				.withTitle(getJobTitle(employee))
				.withUsername(employee.getLoginname())
				.build())
			.orElse(null);
	}

	public static void updateEmployeeEntity(final EmployeeEntity entity, final Employee employeeProspect) {
		ofNullable(employeeProspect).ifPresent(employee -> {
			entity.setEmail(employee.getEmailAddress());
			entity.setLastName(employee.getLastname());
			entity.setFirstName(employee.getGivenname());
			entity.setUsername(employee.getLoginname());
			entity.setTitle(getJobTitle(employee));
			entity.setEmploymentPosition(ofNullable(employee.getIsManager()).orElse(false) ? MANAGER : EMPLOYEE);
		});
	}

	private static String getJobTitle(final Employee employee) {
		return employee.getEmployments().stream()
			.filter(employment -> Objects.nonNull(employment.getIsMainEmployment()))
			.filter(Employment::getIsMainEmployment)
			.map(Employment::getTitle)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	private static LocalDate getStartDate(final Employee employee) {
		return employee.getEmployments().stream()
			.filter(employment -> Objects.nonNull(employment.getIsMainEmployment()))
			.filter(Employment::getIsMainEmployment)
			.map(Employment::getStartDate)
			.filter(Objects::nonNull)
			.map(Date::toInstant)
			.map(instant -> instant.atZone(ZoneId.systemDefault()))
			.map(ZonedDateTime::toLocalDate)
			.findFirst()
			.orElse(LocalDate.now());
	}

	public static OrganizationEntity toOrganizationEntity(int organizationNumber, final String organizationName, final String municipalityId) {
		return OrganizationEntity.builder()
			.withOrganizationNumber(organizationNumber)
			.withOrganizationName(organizationName)
			.withCommunicationChannels(Set.of(NO_COMMUNICATION)) // By default the notification when a new checklist has been created is disabled
			.withMunicipalityId(municipalityId)
			.build();
	}

	public static OrganizationEntity toOrganizationEntity(final OrganizationCreateRequest request, final String municipalityId) {
		return ofNullable(request)
			.map(organizationEntity -> OrganizationEntity.builder()
				.withOrganizationNumber(organizationEntity.getOrganizationNumber())
				.withOrganizationName(organizationEntity.getOrganizationName())
				.withCommunicationChannels(organizationEntity.getCommunicationChannels())
				.withMunicipalityId(municipalityId)
				.build())
			.orElse(null);
	}

	public static OrganizationEntity updateOrganizationEntity(final OrganizationEntity entity, final OrganizationUpdateRequest request) {
		ofNullable(request.getOrganizationName()).ifPresent(entity::setOrganizationName);
		ofNullable(request.getCommunicationChannels()).ifPresent(entity::setCommunicationChannels);
		return entity;
	}

	public static ManagerEntity toManagerEntity(final Manager manager) {
		return ofNullable(manager)
			.map(manager1 -> ManagerEntity.builder()
				.withEmail(manager1.getEmailAddress())
				.withFirstName(manager1.getGivenname())
				.withLastName(manager1.getLastname())
				.withPersonId(manager1.getPersonId().toString())
				.withUsername(manager1.getLoginname())
				.build())
			.orElse(null);
	}

	// -----------------------------
	// API mappings
	// -----------------------------

	public static Organization toOrganization(final OrganizationEntity entity) {
		return ofNullable(entity)
			.map(organization -> Organization.builder()
				.withId(organization.getId())
				.withOrganizationNumber(organization.getOrganizationNumber())
				.withOrganizationName(organization.getOrganizationName())
				.withCommunicationChannels(organization.getCommunicationChannels())
				.withCreated(organization.getCreated())
				.withUpdated(organization.getUpdated())
				.build())
			.orElse(null);
	}

	public static List<Organization> toOrganizations(final List<OrganizationEntity> entities) {
		return ofNullable(entities).orElse(emptyList())
			.stream()
			.map(OrganizationMapper::toOrganization)
			.toList();
	}

	public static Stakeholder toStakeholder(final EmployeeEntity employeeEntity) {
		return ofNullable(employeeEntity)
			.map(entity -> Stakeholder.builder()
				.withId(entity.getId())
				.withFirstName(entity.getFirstName())
				.withLastName(entity.getLastName())
				.withEmail(entity.getEmail())
				.withUsername(entity.getUsername())
				.withTitle(entity.getTitle())
				.build())
			.orElse(null);
	}

	public static Stakeholder toStakeholder(final ManagerEntity managerEntity) {
		return ofNullable(managerEntity)
			.map(entity -> Stakeholder.builder()
				.withId(entity.getPersonId())
				.withFirstName(entity.getFirstName())
				.withLastName(entity.getLastName())
				.withEmail(entity.getEmail())
				.withUsername(entity.getUsername())
				.build())
			.orElse(null);
	}
}
