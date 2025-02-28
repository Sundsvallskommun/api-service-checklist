package se.sundsvall.checklist.integration.employee;

import static java.util.Collections.emptyList;

import generated.se.sundsvall.employee.Employeev2;
import generated.se.sundsvall.employee.PortalPersonData;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

	public List<Employeev2> getEmployeeInformation(String municipalityId, String personId) {
		try {
			return employeeClient.getEmployeesByPersonId(municipalityId, personId)
				.orElse(emptyList());
		} catch (Exception e) {
			// We don't really care.
			LOG.warn("Couldn't fetch employee information from employee integration", e);
		}

		return emptyList();
	}

	public List<Employeev2> getNewEmployees(String municipalityId, LocalDate hireDateFrom) {
		try {
			return employeeClient.getNewEmployees(municipalityId, hireDateFrom)
				.orElse(emptyList());
		} catch (Exception e) {
			// We don't really care here either...
			LOG.warn("Couldn't fetch new employees from employee integration", e);
		}

		return emptyList();
	}

	public Optional<PortalPersonData> getEmployeeByEmail(String municipalityId, String email) {
		try {
			return employeeClient.getEmployeeByEmail(municipalityId, email);
		} catch (Exception e) {
			// And not here..
			LOG.warn("Couldn't fetch employee by email from employee integration", e);
		}

		return Optional.empty();
	}
}
