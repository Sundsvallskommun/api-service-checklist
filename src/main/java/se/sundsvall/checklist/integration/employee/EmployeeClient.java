package se.sundsvall.checklist.integration.employee;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.checklist.configuration.CacheConfiguration.EMPLOYEE_CACHE;
import static se.sundsvall.checklist.integration.employee.configuration.EmployeeConfiguration.CLIENT_ID;

import generated.se.sundsvall.employee.Employeev2;
import generated.se.sundsvall.employee.PortalPersonData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.checklist.integration.employee.configuration.EmployeeConfiguration;

@CircuitBreaker(name = CLIENT_ID)
@FeignClient(name = CLIENT_ID, url = "${integration.employee.url}", configuration = EmployeeConfiguration.class, dismiss404 = true)
public interface EmployeeClient {

	/**
	 * Uses the employments endpoint which has more information than the newemployments endpoint.
	 *
	 * @param  municipalityId the municipalityId
	 * @param  personId       the personId
	 * @return                List of employees
	 */
	@GetMapping(path = "/{municipalityId}/employments", produces = APPLICATION_JSON_VALUE)
	Optional<List<Employeev2>> getEmployeesByPersonId(
		@PathVariable("municipalityId") String municipalityId,
		@RequestParam(name = "PersonId") String personId);

	/**
	 * Get all new employees from the employee service.
	 *
	 * @param  municipalityId the municipalityId
	 * @param  hireDateFrom   the hire date from parameter
	 * @return                List of employees
	 */
	@GetMapping(path = "/{municipalityId}/newemployments", produces = APPLICATION_JSON_VALUE)
	Optional<List<Employeev2>> getNewEmployees(
		@PathVariable("municipalityId") String municipalityId,
		@RequestParam(name = "HireDateFrom") LocalDate hireDateFrom);

	/**
	 * Get a specific employee by email
	 *
	 * @param  municipalityId the municipalityId
	 * @param  email          email of the employee
	 * @return                {@link Optional<PortalPersonData>} with possible information about the employee
	 */
	@Cacheable(EMPLOYEE_CACHE)
	@GetMapping(path = "/{municipalityId}/portalpersondata/{email}", produces = APPLICATION_JSON_VALUE)
	Optional<PortalPersonData> getEmployeeByEmail(
		@PathVariable("municipalityId") String municipalityId,
		@PathVariable("email") String email);
}
