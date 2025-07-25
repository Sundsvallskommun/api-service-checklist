package se.sundsvall.checklist.integration.employee;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static se.sundsvall.checklist.configuration.CacheConfiguration.EMPLOYEE_CACHE;

import generated.se.sundsvall.employee.PortalPersonData;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import se.sundsvall.checklist.service.model.Employee;

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

	public List<Employee> getEmployeeInformation(String municipalityId, String personId) {
		try {
			return employeeClient.getEmployeesByPersonId(municipalityId, personId).orElse(emptyList())
				.stream()
				.map(EmployeeMapper::toEmployee)
				.toList();
		} catch (final Exception e) {
			// We don't really care.
			LOG.warn("Couldn't fetch employee information from employee integration", e);
		}

		return emptyList();
	}

	public List<Employee> getNewEmployees(String municipalityId, LocalDate hireDateFrom) {
		try {
			return employeeClient.getNewEmployees(municipalityId, ofNullable(hireDateFrom).map(d -> d.format(ISO_LOCAL_DATE)).orElse(null)).orElse(emptyList())
				.stream()
				.map(EmployeeMapper::toEmployee)
				.toList();
		} catch (final Exception e) {
			// We don't really care here either...
			LOG.warn("Couldn't fetch new employees from employee integration", e);
		}

		return emptyList();
	}

	@Cacheable(EMPLOYEE_CACHE)
	public Optional<PortalPersonData> getEmployeeByEmail(String municipalityId, String email) {
		try {
			return employeeClient.getEmployeeByEmail(municipalityId, email);
		} catch (final Exception e) {
			// And not here..
			LOG.warn("Couldn't fetch employee by email from employee integration", e);
		}

		return Optional.empty();
	}
}
