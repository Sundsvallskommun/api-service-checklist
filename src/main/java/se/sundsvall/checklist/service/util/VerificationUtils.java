package se.sundsvall.checklist.service.util;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.service.util.StringUtils.toReadableString;

import java.util.ArrayList;
import java.util.List;

import org.zalando.problem.Problem;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;

public final class VerificationUtils {
	private static final String EMPLOYEE_CHECKLIST_IS_LOCKED = "Employee checklist with id %s is locked and can not be modified.";

	private VerificationUtils() {}

	public static void verifyMandatoryInformation(Employee employee) {
		final var missingInformation = new ArrayList<String>();

		if (isNull(employee.getPersonId())) {
			missingInformation.add("the employee does not have any personid");
		}
		if (isBlank(employee.getLoginname())) {
			missingInformation.add("the employee does not have any loginname");
		}
		if (isBlank(employee.getEmailAddress())) {
			missingInformation.add("the employee does not have any email address");
		}

		final var employment = getMainEmployment(employee);
		if (isNull(employment)) {
			missingInformation.add("the employee does not have any main employment");
		} else {
			missingInformation.addAll(verifyMandatoryInformation(employment));
		}

		if (isNotEmpty(missingInformation)) {
			throw Problem.valueOf(NOT_FOUND, buildErrorString(employee, missingInformation));
		}
	}

	private static List<String> verifyMandatoryInformation(Employment employment) {
		final var missingInformation = new ArrayList<String>();

		if (isNull(employment.getManager())) {
			missingInformation.add("the main employment for the employee lacks information about the manager");
		} else if (isNull(employment.getManager().getPersonId())) {
			missingInformation.add("the personid is missing for the manager connected to the main employment of the employee");
		}
		if (isBlank(employment.getFormOfEmploymentId())) {
			missingInformation.add("the main employment for the employee lacks information about the employment form");
		}
		if (isNull(employment.getOrgId())) {
			missingInformation.add("the main employment for the employee lacks neccessary department information");
		}
		if (isNull(employment.getCompanyId())) {
			missingInformation.add("the main employment for the employee lacks neccessary company information");
		}

		return missingInformation;
	}

	private static String buildErrorString(Employee employee, List<String> errors) {
		final var prefix = isBlank(employee.getLoginname()) ? "Creation of checklist not possible for employee with personid %s as ".formatted(employee.getPersonId())
			: "Creation of checklist not possible for employee with loginname %s as "
				.formatted(employee
					.getLoginname());

		return prefix + toReadableString(errors) + ".";
	}

	private static Employment getMainEmployment(Employee employee) {
		return ofNullable(employee.getEmployments()).orElse(emptyList()).stream()
			.filter(Employment::getIsMainEmployment)
			.findFirst()
			.orElse(null);
	}

	public static void verifyUnlockedEmployeeChecklist(EmployeeChecklistEntity employeeChecklistEntity) {
		if (employeeChecklistEntity.isLocked()) {
			throw Problem.valueOf(BAD_REQUEST, EMPLOYEE_CHECKLIST_IS_LOCKED.formatted(employeeChecklistEntity.getId()));
		}
	}
}
