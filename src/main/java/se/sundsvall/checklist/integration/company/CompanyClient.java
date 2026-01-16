package se.sundsvall.checklist.integration.company;

import static se.sundsvall.checklist.integration.company.configuration.CompanyConfiguration.CLIENT_ID;

import generated.se.sundsvall.company.Organization;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.checklist.integration.company.configuration.CompanyConfiguration;

@CircuitBreaker(name = CLIENT_ID)
@FeignClient(name = CLIENT_ID, url = "${integration.company.url}", configuration = CompanyConfiguration.class)
public interface CompanyClient {

	/**
	 * Get all root company items as a flat list.
	 *
	 * @param  municipalityId municipality id to fetch companies for
	 * @return                List of root organizations (a.k.a. companies)
	 */
	@GetMapping(path = "/{municipalityId}/root", produces = MediaType.APPLICATION_JSON_VALUE)
	List<Organization> getCompanies(@PathVariable String municipalityId);

	/**
	 * Get all organizations for a root company presented as a flat list.
	 *
	 * @param  municipalityId municipality id to fetch organizations for
	 * @param  companyId      containing the id of the root company to fetch organizations for
	 * @return                List of organizations
	 */
	@GetMapping(path = "/{municipalityId}/{companyId}/company", produces = MediaType.APPLICATION_JSON_VALUE)
	List<Organization> getOrganizationsForCompany(@PathVariable String municipalityId, @PathVariable int companyId);

}
