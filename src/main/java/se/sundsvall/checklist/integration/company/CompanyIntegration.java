package se.sundsvall.checklist.integration.company;

import static se.sundsvall.checklist.configuration.CacheConfiguration.COMPANY_CACHE;

import generated.se.sundsvall.company.Organization;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for {@link CompanyClient}.
 */
@Component
public class CompanyIntegration {

	private final CompanyClient companyClient;

	public CompanyIntegration(final CompanyClient companyClient) {
		this.companyClient = companyClient;
	}

	/**
	 * Get all root company items as a flat list.
	 *
	 * @param  municipalityId municipality id to fetch companies for
	 * @return                List of root organizations (a.k.a. companies)
	 */
	@Cacheable(COMPANY_CACHE)
	public List<Organization> getCompanies(final String municipalityId) {
		return companyClient.getCompanies(municipalityId);
	}

	/**
	 * Get all organizations for a root company presented as a flat list.
	 *
	 * @param  municipalityId municipality id to fetch organizations for
	 * @param  companyId      containing the id of the root company to fetch organizations for
	 * @return                List of organizations
	 */
	@Cacheable(COMPANY_CACHE)
	public List<Organization> getOrganizationsForCompany(final String municipalityId, final int companyId) {
		return companyClient.getOrganizationsForCompany(municipalityId, companyId);
	}

}
