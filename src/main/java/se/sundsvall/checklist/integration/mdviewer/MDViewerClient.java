package se.sundsvall.checklist.integration.mdviewer;

import static se.sundsvall.checklist.configuration.CacheConfiguration.MDVIEWER_CACHE;
import static se.sundsvall.checklist.integration.mdviewer.configuration.MDViewerConfiguration.CLIENT_ID;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import generated.se.sundsvall.mdviewer.Organization;
import se.sundsvall.checklist.integration.mdviewer.configuration.MDViewerConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.mdviewer.url}", configuration = MDViewerConfiguration.class)
public interface MDViewerClient {

	/**
	 * Get all root company items as a flat list.
	 *
	 * @return List of root organizations (a.k.a. companies)
	 */
	@Cacheable(MDVIEWER_CACHE)
	@GetMapping(path = "/root", produces = MediaType.APPLICATION_JSON_VALUE)
	List<Organization> getCompanies();

	/**
	 * Get all organizations for a root company presented as a flat list.
	 *
	 * @param  companyId containing the id of the root company to fetch organizations for
	 * @return           List of organizations
	 */
	@Cacheable(MDVIEWER_CACHE)
	@GetMapping(path = "/{companyId}/company", produces = MediaType.APPLICATION_JSON_VALUE)
	List<Organization> getOrganizationsForCompany(@PathVariable("companyId") int companyId);

}
