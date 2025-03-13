package se.sundsvall.checklist.service.util;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_ACCEPTABLE;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.service.util.StringUtils.toReadableString;

import java.util.ArrayList;
import java.util.List;
import org.zalando.problem.Problem;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.service.model.Employee;
import se.sundsvall.checklist.service.model.Employment;

public final class VerificationUtils {
	private static final String EMPLOYMENT_NOT_VALID_FOR_CHECKLIST = "the employee does not have a main employment with an %s that validates for creating an employee checklist";
	private static final String EMPLOYEE_CHECKLIST_IS_LOCKED = "Employee checklist with id %s is locked and can not be modified.";
	private static final List<String> VALID_EMPLOYMENT_FORMS_FOR_CHECKLIST = List.of("1", "2", "9", "14", "17"); // Permanent employment ("1"), temporary monthly paid employment ("2") and probationary employment ("9")
	private static final String VALID_EVENT_TYPE = "Joiner";

	private VerificationUtils() {}

	/**
	 * Method for checking if the employee has all the necessary information for creating a checklist. If any mandatory
	 * information is missing, a Problem will be thrown.
	 *
	 * @param employee the employee to verify
	 */
	public static void verifyMandatoryInformation(Employee employee) {
		final var missingInformation = new ArrayList<String>();
		final var employment = employee.getMainEmployment();

		if (isNull(employee.getPersonId())) {
			missingInformation.add("the employee does not have any personid");
		}

		if (isNull(employment)) {
			missingInformation.add("the employee does not have any main employment");
		} else {
			// Check that there is a loginname and email address connected to the account matching the same companyid as the main
			// employment has
			if (isBlank(employee.getLoginname())) {
				missingInformation.add("the employee does not have any loginname");
			}
			if (isBlank(employee.getEmailAddress())) {
				missingInformation.add("the employee does not have any email address");
			}

			missingInformation.addAll(verifyMandatoryEmploymentInformation(employment));
		}

		if (isNotEmpty(missingInformation)) {
			throw Problem.valueOf(NOT_FOUND, buildErrorString(employee, missingInformation));
		}
	}

	private static List<String> verifyMandatoryEmploymentInformation(Employment employment) {
		final var missingInformation = new ArrayList<String>();

		if (isNull(employment.getManager())) {
			missingInformation.add("the main employment for the employee lacks information about the manager");
		} else if (isNull(employment.getManager().getPersonId())) {
			missingInformation.add("the personid is missing for the manager connected to the main employment of the employee");
		}
		if (isNull(employment.getOrgId())) {
			missingInformation.add("the main employment for the employee lacks neccessary department information");
		}
		if (isNull(employment.getCompanyId())) {
			missingInformation.add("the main employment for the employee lacks neccessary company information");
		}

		return missingInformation;
	}

	/**
	 * Method for checking if employment is valid for having an employee checklist created or not. A prerequisite for
	 * calling this method is that <code>verifyMandatoryInformation</code> has been called successfully.
	 *
	 * @param employee the employee connected to the employment to be checked
	 */
	public static void verifyValidEmployment(Employee employee) {
		final var employment = employee.getMainEmployment();
		if (isNull(employment)) {
			throw Problem.valueOf(NOT_FOUND, buildErrorString(employee, List.of("the employee does not have any main employment")));
		}
		if (isNull(employment.getEventType())) {
			throw Problem.valueOf(NOT_FOUND, buildErrorString(employee, List.of("the main employment for the employee lacks event type information")));
		}
		if (!VALID_EMPLOYMENT_FORMS_FOR_CHECKLIST.contains(employment.getFormOfEmploymentId())) {
			throw Problem.valueOf(NOT_ACCEPTABLE, buildErrorString(employee, List.of(EMPLOYMENT_NOT_VALID_FOR_CHECKLIST.formatted("employment form"))));
		}
		if (!equalsIgnoreCase(VALID_EVENT_TYPE, employment.getEventType())) {
			throw Problem.valueOf(NOT_ACCEPTABLE, buildErrorString(employee, List.of(EMPLOYMENT_NOT_VALID_FOR_CHECKLIST.formatted("event type"))));
		}
	}

	/**
	 * Method for verifying that an employee checklist is not locked. If the employee checklist is locked, a Problem will be
	 * thrown.
	 *
	 * @param employeeChecklistEntity the employee checklist entity to verify
	 */
	public static void verifyUnlockedEmployeeChecklist(EmployeeChecklistEntity employeeChecklistEntity) {
		if (employeeChecklistEntity.isLocked()) {
			throw Problem.valueOf(BAD_REQUEST, EMPLOYEE_CHECKLIST_IS_LOCKED.formatted(employeeChecklistEntity.getId()));
		}
	}

	private static String buildErrorString(Employee employee, List<String> errors) {
		final var prefix = isNull(employee.getLoginname())
			? "Creation of checklist not possible for employee with personid %s as ".formatted(employee.getPersonId())
			: "Creation of checklist not possible for employee with loginname %s as ".formatted(employee.getLoginname());

		return prefix + toReadableString(errors) + ".";
	}
}
