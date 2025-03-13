package se.sundsvall.checklist.integration.employee;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import generated.se.sundsvall.employee.Employeev2;
import generated.se.sundsvall.employee.NewEmployee;
import generated.se.sundsvall.employee.NewEmployment;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import se.sundsvall.checklist.service.model.Employee;
import se.sundsvall.checklist.service.model.Employment;
import se.sundsvall.checklist.service.model.Manager;

public class EmployeeMapper {

	private EmployeeMapper() {}

	/**
	 * Method for mapping Employeev2 objects to an internal Employee model objects
	 *
	 * @param  employee the Employeev2 object to map
	 * @return          an internal Employee model object representing the provided Employeev2 object
	 */
	public static Employee toEmployee(Employeev2 employee) {
		return ofNullable(employee)
			.map(e -> Employee.builder()
				.withEmailAddress(extractEmailAddressFromMainEmployment(e))
				.withLoginname(extractLoginnameFromMainEmployment(e))
				.withMainEmployment(getMainEmployment(e)
					.map(EmployeeMapper::toEmployment)
					.orElse(null))
				.withGivenname(e.getGivenname())
				.withIsClassified(e.getIsClassified())
				.withLastname(e.getLastname())
				.withMiddlename(e.getMiddlename())
				.withPersonId(ofNullable(e.getPersonId()).map(UUID::toString).orElse(null))
				.withLegalId(e.getPersonNumber())
				.build())
			.orElse(null);
	}

	private static Employment toEmployment(generated.se.sundsvall.employee.Employment employment) {
		return ofNullable(employment)
			.map(e -> Employment.builder()
				.withBenefitGroupId(e.getBenefitGroupId())
				.withCompanyId(e.getCompanyId())
				.withEmploymentType(e.getEmploymentType())
				.withEndDate(e.getEndDate())
				.withFormOfEmploymentId(e.getFormOfEmploymentId())
				.withIsMainEmployment(e.getIsMainEmployment())
				.withIsManager(e.getIsManager())
				.withIsManual(e.getIsManual())
				.withManager(toManager(e.getManager()))
				.withManagerCode(e.getManagerCode())
				.withOrgId(e.getOrgId())
				.withOrgName(e.getOrgName())
				.withStartDate(e.getStartDate())
				.withTitle(e.getTitle())
				.withTopOrgId(e.getTopOrgId())
				.withTopOrgName(e.getTopOrgName())
				.build())
			.orElse(null);
	}

	private static String extractEmailAddressFromMainEmployment(generated.se.sundsvall.employee.Employeev2 employee) {
		return getMainEmployment(employee)
			.map(me -> ofNullable(employee.getAccounts()).orElse(emptyList())
				.stream()
				.filter(a -> Objects.equals(me.getCompanyId(), a.getCompanyId()))
				.map(generated.se.sundsvall.employee.Account::getEmailAddress)
				.findFirst()
				.orElse(null))
			.orElse(null);
	}

	private static String extractLoginnameFromMainEmployment(generated.se.sundsvall.employee.Employeev2 employee) {
		return getMainEmployment(employee)
			.map(me -> ofNullable(employee.getAccounts()).orElse(emptyList())
				.stream()
				.filter(a -> Objects.equals(me.getCompanyId(), a.getCompanyId()))
				.map(generated.se.sundsvall.employee.Account::getLoginname)
				.findFirst()
				.orElse(null))
			.orElse(null);
	}

	private static Optional<generated.se.sundsvall.employee.Employment> getMainEmployment(generated.se.sundsvall.employee.Employeev2 employee) {
		return ofNullable(employee.getEmployments()).orElse(emptyList()).stream()
			.filter(generated.se.sundsvall.employee.Employment::getIsMainEmployment)
			.findFirst();
	}

	/**
	 * Method for mapping NewEmployee objects to an internal Employee model objects
	 *
	 * @param  employee the NewEmployee object to map
	 * @return          an internal Employee model object representing the provided NewEmployee object
	 */
	public static Employee toEmployee(NewEmployee employee) {
		return ofNullable(employee)
			.map(e -> Employee.builder()
				.withEmailAddress(extractEmailAddressFromMainEmployment(e))
				.withLoginname(extractLoginnameFromMainEmployment(e))
				.withMainEmployment(getMainEmployment(e)
					.map(EmployeeMapper::toEmployment)
					.orElse(null))
				.withGivenname(e.getGivenname())
				.withIsClassified(e.getIsClassified())
				.withLastname(e.getLastname())
				.withMiddlename(e.getMiddlename())
				.withPersonId(ofNullable(e.getPersonId()).map(UUID::toString).orElse(null))
				.withLegalId(e.getPersonNumber())
				.build())
			.orElse(null);
	}

	private static Employment toEmployment(NewEmployment employment) {
		return ofNullable(employment)
			.map(e -> Employment.builder()
				.withBenefitGroupId(e.getBenefitGroupId())
				.withCompanyId(e.getCompanyId())
				.withEmploymentType(e.getEmploymentType())
				.withEndDate(e.getEndDate())
				.withEventType(e.getEventType())
				.withFormOfEmploymentId(e.getFormOfEmploymentId())
				.withIsMainEmployment(e.getIsMainEmployment())
				.withIsManager(e.getIsManager())
				.withIsManual(e.getIsManual())
				.withManager(toManager(e.getManager()))
				.withManagerCode(e.getManagerCode())
				.withOrgId(e.getOrgId())
				.withOrgName(e.getOrgName())
				.withStartDate(e.getStartDate())
				.withTitle(e.getTitle())
				.withTopOrgId(e.getTopOrgId())
				.withTopOrgName(e.getTopOrgName())
				.build())
			.orElse(null);
	}

	private static String extractEmailAddressFromMainEmployment(generated.se.sundsvall.employee.NewEmployee employee) {
		return getMainEmployment(employee)
			.map(me -> ofNullable(employee.getAccounts()).orElse(emptyList())
				.stream()
				.filter(a -> Objects.equals(me.getCompanyId(), a.getCompanyId()))
				.map(generated.se.sundsvall.employee.Account::getEmailAddress)
				.findFirst()
				.orElse(null))
			.orElse(null);
	}

	private static String extractLoginnameFromMainEmployment(generated.se.sundsvall.employee.NewEmployee employee) {
		return getMainEmployment(employee)
			.map(me -> ofNullable(employee.getAccounts()).orElse(emptyList())
				.stream()
				.filter(a -> Objects.equals(me.getCompanyId(), a.getCompanyId()))
				.map(generated.se.sundsvall.employee.Account::getLoginname)
				.findFirst()
				.orElse(null))
			.orElse(null);
	}

	private static Optional<generated.se.sundsvall.employee.NewEmployment> getMainEmployment(generated.se.sundsvall.employee.NewEmployee employee) {
		return ofNullable(employee.getEmployments()).orElse(emptyList()).stream()
			.filter(generated.se.sundsvall.employee.NewEmployment::getIsMainEmployment)
			.findFirst();
	}

	// Common method used by both Employeev2 and NewEmployee responses
	private static Manager toManager(generated.se.sundsvall.employee.Manager manager) {
		return ofNullable(manager)
			.map(m -> Manager.builder()
				.withEmailAddress(m.getEmailAddress())
				.withGivenname(m.getGivenname())
				.withLastname(m.getLastname())
				.withLoginname(m.getLoginname())
				.withPersonId(m.getPersonId().toString())
				.build())
			.orElse(null);
	}
}
