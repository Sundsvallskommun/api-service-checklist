package se.sundsvall.checklist.integration.employee;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.PortalPersonData;

/**
 * Wrapper class for {@link EmployeeClient}.
 */
@Component
public class EmployeeIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(EmployeeIntegration.class);

	private final EmployeeClient employeeClient;

	public EmployeeIntegration(EmployeeClient employeeClient) {
		this.employeeClient = employeeClient;
	}

	public List<Employee> getEmployeeInformation(String filterString) {
		try {
			return employeeClient.getEmployeeInformation(filterString)
				.orElse(emptyList());
		} catch (Exception e) {
			// We don't really care.
			LOG.warn("Couldn't fetch employee information from employee integration", e);
		}

		return emptyList();
	}

	public List<Employee> getNewEmployees(String filterString) {
		try {
			return employeeClient.getNewEmployees(filterString)
				.orElse(emptyList());
		} catch (Exception e) {
			// We don't really care here either...
			LOG.warn("Couldn't fetch new employees from employee integration", e);
		}

		return emptyList();
	}

	public Optional<PortalPersonData> getEmployeeByEmail(String email) {
		try {
			return employeeClient.getEmployeeByEmail(email);
		} catch (Exception e) {
			// And not here..
			LOG.warn("Couldn't fetch employee by email from employee integration", e);
		}

		return Optional.empty();
	}
}
