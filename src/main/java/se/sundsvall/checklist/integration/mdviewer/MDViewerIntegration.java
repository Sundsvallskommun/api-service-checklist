package se.sundsvall.checklist.integration.mdviewer;

import static se.sundsvall.checklist.configuration.CacheConfiguration.MDVIEWER_CACHE;

import generated.se.sundsvall.mdviewer.Organization;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for {@link MDViewerClient}.
 */
@Component
public class MDViewerIntegration {

	private final MDViewerClient mdViewerClient;

	public MDViewerIntegration(MDViewerClient mdViewerClient) {
		this.mdViewerClient = mdViewerClient;
	}

	/**
	 * Get all root company items as a flat list.
	 *
	 * @return List of root organizations (a.k.a. companies)
	 */
	@Cacheable(MDVIEWER_CACHE)
	public List<Organization> getCompanies() {
		return mdViewerClient.getCompanies();
	}

	/**
	 * Get all organizations for a root company presented as a flat list.
	 *
	 * @param  companyId containing the id of the root company to fetch organizations for
	 * @return           List of organizations
	 */
	@Cacheable(MDVIEWER_CACHE)
	public List<Organization> getOrganizationsForCompany(int companyId) {
		return mdViewerClient.getOrganizationsForCompany(companyId);
	}

}
