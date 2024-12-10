package se.sundsvall.checklist.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.springframework.util.CollectionUtils.isEmpty;
import static se.sundsvall.checklist.service.mapper.SortorderMapper.toSortorderEntities;
import static se.sundsvall.checklist.service.mapper.SortorderMapper.toSortorderEntity;
import static se.sundsvall.checklist.service.mapper.SortorderMapper.toTaskItem;
import static se.sundsvall.checklist.service.util.SortingUtils.applyCustomSortorder;
import static se.sundsvall.checklist.service.util.SortingUtils.sortEmployeeChecklistPhases;
import static se.sundsvall.checklist.service.util.SortingUtils.sortPhases;

import generated.se.sundsvall.mdviewer.Organization;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.SortorderRequest;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.SortorderEntity;
import se.sundsvall.checklist.integration.db.repository.SortorderRepository;
import se.sundsvall.checklist.integration.mdviewer.MDViewerClient;

@Service
public class SortorderService {

	private final SortorderRepository sortorderRepository;
	private final MDViewerClient mdViewerClient;

	public SortorderService(final SortorderRepository sortorderRepository, final MDViewerClient mdViewerClient) {
		this.sortorderRepository = sortorderRepository;
		this.mdViewerClient = mdViewerClient;
	}

	/**
	 * Method for copying and saving custom sort order for existing checklist items to their respective new version. Method
	 * is primarily used when creating a new version of an existing checklist.
	 *
	 * @param translationMap a map consisting of key value-pair, where key is the id for the new version of the item and
	 *                       value is the id for the old version of the item
	 */
	public void copySortorderItems(final Map<String, String> translationMap) {
		translationMap.entrySet().stream()
			.map(this::copyCurrentSortorder)
			.forEach(sortorderRepository::saveAll);
	}

	private List<SortorderEntity> copyCurrentSortorder(Entry<String, String> entry) {
		return sortorderRepository.findAllByComponentId(entry.getValue())
			.stream()
			.map(currentSort -> toSortorderEntity(currentSort.getMunicipalityId(), currentSort.getOrganizationNumber(), toTaskItem(entry.getKey(), currentSort)))
			.toList();
	}

	/**
	 * Method for saving (replacing previous) custom sortorder
	 *
	 * @param municipalityId     id for municipality to which the custom sort belongs
	 * @param organizationNumber number for the organization to which the custom sort belongs
	 * @param request            the custom sortorder structure to be saved
	 */
	@Transactional
	public void saveSortorder(final String municipalityId, final Integer organizationNumber, final SortorderRequest request) {
		sortorderRepository.deleteAllInBatch(sortorderRepository.findAllByMunicipalityIdAndOrganizationNumber(municipalityId, organizationNumber));
		sortorderRepository.saveAll(toSortorderEntities(municipalityId, organizationNumber, request));
	}

	/**
	 * Method for deleting a custom sort order for an organization
	 *
	 * @param municipalityId     id for municipality to which the custom sort that shall be removed belongs
	 * @param organizationNumber number for the organization to which the custom sort that shall be removed belongs
	 */
	public void deleteSortorder(final String municipalityId, final Integer organizationNumber) {
		sortorderRepository.deleteAllInBatch(sortorderRepository.findAllByMunicipalityIdAndOrganizationNumber(municipalityId, organizationNumber));
	}

	/**
	 * Method for deleting a custom sort order for an explicit sort order item (task or phase)
	 *
	 * @param componentId id of the sort order component that will be removed
	 */
	public void deleteSortorderItem(final String componentId) {
		sortorderRepository.deleteAllInBatch(sortorderRepository.findAllByComponentId(componentId));
	}

	/**
	 * Method for applying custom sorting to checklist "templates"
	 *
	 * @param  municipalityId     id for municipality where to find checklist and custom sort
	 * @param  organizationNumber number for the organization that the custom sort should be based on
	 * @param  checklists         the checklists that will be sorted
	 * @return                    list of checklists where custom sortorder has been applied
	 */
	public List<Checklist> applySorting(final String municipalityId, final Integer organizationNumber, final List<Checklist> checklists) {
		ofNullable(findOrganization(mdViewerClient.getCompanies().iterator(), organizationNumber))
			.map(org -> findClosestCustomSort(municipalityId, org))
			.ifPresent(customSort -> recalculateSortorder(checklists, customSort));

		return checklists;
	}

	private void recalculateSortorder(final List<Checklist> checklists, List<SortorderEntity> customSort) {
		ofNullable(checklists).orElse(emptyList()).forEach(checklist -> {
			applyCustomSortorder(checklist, customSort);
			checklist.setPhases(sortPhases(checklist.getPhases()));
		});
	}

	private Organization findOrganization(final Iterator<Organization> companyIterator, final Integer organizationNumber) {
		return mdViewerClient.getOrganizationsForCompany(companyIterator.next().getCompanyId()).stream()
			.filter(org -> org.getOrgId().equals(organizationNumber))
			.findAny()
			.orElse(companyIterator.hasNext() ? findOrganization(companyIterator, organizationNumber) : null);
	}

	private List<SortorderEntity> findClosestCustomSort(final String municipalityId, final Organization organization) {
		final var organizations = ofNullable(findOrganization(mdViewerClient.getCompanies().iterator(), organization.getOrgId()))
			.map(Organization::getCompanyId)
			.map(mdViewerClient::getOrganizationsForCompany)
			.orElse(emptyList());

		return organizations.stream()
			.filter(item -> item.getOrgId().equals(organization.getOrgId()))
			.findAny()
			.map(org -> findCustomSort(municipalityId, org, organizations))
			.orElse(emptyList());
	}

	/**
	 * Method for applying custom sorting to a employee checklist
	 *
	 * @param  employeeChecklistEntity the employee checklist entity holding organizational information need when finding
	 *                                 closest custom sortorder
	 * @param  employeeChecklist       the employee checklist that will be sorted
	 * @return                         employee checklist where custom sortorder has been applied
	 */
	public EmployeeChecklist applySorting(final Optional<EmployeeChecklistEntity> employeeChecklistEntity, final EmployeeChecklist employeeChecklist) {
		employeeChecklistEntity
			.map(EmployeeChecklistEntity::getEmployee)
			.map(this::findClosestCustomSort)
			.ifPresent(customSort -> recalculateSortorder(employeeChecklist, customSort));

		return employeeChecklist;
	}

	private void recalculateSortorder(final EmployeeChecklist employeeChecklist, List<SortorderEntity> customSort) {
		applyCustomSortorder(employeeChecklist, customSort);
		employeeChecklist.setPhases(sortEmployeeChecklistPhases(employeeChecklist.getPhases()));
	}

	private List<SortorderEntity> findClosestCustomSort(final EmployeeEntity employee) {
		final var organizations = ofNullable(employee)
			.map(EmployeeEntity::getCompany)
			.map(OrganizationEntity::getOrganizationNumber)
			.map(mdViewerClient::getOrganizationsForCompany)
			.orElse(emptyList());

		return organizations.stream()
			.filter(organization -> organization.getOrgId().equals(employee.getDepartment().getOrganizationNumber()))
			.findAny()
			.map(employeeHome -> findCustomSort(employee.getDepartment().getMunicipalityId(), employeeHome, organizations))
			.orElse(emptyList());
	}

	private List<SortorderEntity> findCustomSort(final String municipalityId, final Organization organization, final List<Organization> organizations) {
		final var customSort = sortorderRepository.findAllByMunicipalityIdAndOrganizationNumber(municipalityId, organization.getOrgId());

		if (isEmpty(customSort) && nonNull(organization.getParentId())) {

			return organizations.stream()
				.filter(parent -> parent.getOrgId().equals(organization.getParentId()))
				.findAny()
				.map(parent -> findCustomSort(municipalityId, parent, organizations))
				.orElse(emptyList());
		}

		return customSort;
	}
}
