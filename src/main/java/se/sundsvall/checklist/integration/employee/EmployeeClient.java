package se.sundsvall.checklist.integration.employee;

import static se.sundsvall.checklist.configuration.CacheConfiguration.EMPLOYEE_CACHE;
import static se.sundsvall.checklist.integration.employee.configuration.EmployeeConfiguration.CLIENT_ID;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.PortalPersonData;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.checklist.integration.employee.configuration.EmployeeConfiguration;

@CircuitBreaker(name = CLIENT_ID)
@FeignClient(name = CLIENT_ID, url = "${integration.employee.url}", configuration = EmployeeConfiguration.class, dismiss404 = true)
public interface EmployeeClient {

	/**
	 * Uses the employments endpoint which has more information than the newemployments endpoint.
	 *
	 * @param  filterString with filter
	 * @return              List of employees
	 */
	@GetMapping(path = "/employments?filter={filterString}", produces = {
		MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE
	})
	Optional<List<Employee>> getEmployeeInformation(@PathVariable("filterString") String filterString);

	/**
	 * Get all new employees from the employee service.
	 *
	 * @param  filterString with filter
	 * @return              List of employees
	 */
	@GetMapping(path = "/newemployments?filter={filterString}", produces = {
		MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE
	})
	Optional<List<Employee>> getNewEmployees(@PathVariable("filterString") String filterString);

	/**
	 * Get a specific employee by email
	 *
	 * @param  email email of the employee
	 * @return       {@link Optional<PortalPersonData>} with possible information about the employee
	 */
	@Cacheable(EMPLOYEE_CACHE)
	@GetMapping(path = "/portalpersondata/{email}", produces = {
		MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE
	})
	Optional<PortalPersonData> getEmployeeByEmail(@PathVariable("email") String email);
}
