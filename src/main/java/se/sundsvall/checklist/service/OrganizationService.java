package se.sundsvall.checklist.service;

import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.service.mapper.OrganizationMapper.toOrganizationEntity;
import static se.sundsvall.checklist.service.mapper.OrganizationMapper.updateOrganizationEntity;

import java.util.List;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.checklist.api.model.Organization;
import se.sundsvall.checklist.api.model.OrganizationCreateRequest;
import se.sundsvall.checklist.api.model.OrganizationUpdateRequest;
import se.sundsvall.checklist.integration.db.ChecklistBuilder;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.service.mapper.OrganizationMapper;

@Service
public class OrganizationService {

	private static final String ORGANIZATION_NUMBER_ALREADY_EXISTS = "Organization with organization number %s already exists in municipality %s";
	private static final String ORGANIZATION_HAS_CHECKLISTS = "Organization with id %s has non retired checklists and cannot be deleted";
	private static final String ORGANIZATION_NOT_FOUND = "Organization with id %s does not exist within municipality %s";

	private final OrganizationRepository organizationRepository;
	private final ChecklistBuilder checklistBuilder;

	public OrganizationService(final OrganizationRepository organizationRepository, final ChecklistBuilder checklistBuilder) {
		this.organizationRepository = organizationRepository;
		this.checklistBuilder = checklistBuilder;
	}

	public String createOrganization(final String municipalityId, final OrganizationCreateRequest request) {
		organizationRepository.findByOrganizationNumberAndMunicipalityId(request.getOrganizationNumber(), municipalityId)
			.ifPresent(organizationEntity -> {
				throw Problem.valueOf(CONFLICT, ORGANIZATION_NUMBER_ALREADY_EXISTS.formatted(request.getOrganizationNumber(), municipalityId));
			});

		final var entity = organizationRepository.save(toOrganizationEntity(request, municipalityId));
		return entity.getId();
	}

	public List<Organization> fetchAllOrganizations(final String municipalityId) {
		return organizationRepository
			.findAllByMunicipalityId(municipalityId).stream()
			.map(this::toOrganization)
			.toList();
	}

	public Organization fetchOrganization(final String municipalityId, final String organizationId) {
		return organizationRepository.findByIdAndMunicipalityId(organizationId, municipalityId)
			.map(this::toOrganization)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ORGANIZATION_NOT_FOUND.formatted(organizationId, municipalityId)));
	}

	private Organization toOrganization(OrganizationEntity entity) {
		final var organization = OrganizationMapper.toOrganization(entity);
		organization.setChecklists(entity.getChecklists().stream().map(checklistBuilder::buildChecklist).toList());
		return organization;
	}

	public Organization updateOrganization(final String municipalityId, final String organizationId, final OrganizationUpdateRequest request) {
		return organizationRepository.findByIdAndMunicipalityId(organizationId, municipalityId)
			.map(entity -> updateOrganizationEntity(entity, request))
			.map(organizationRepository::save)
			.map(OrganizationMapper::toOrganization)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ORGANIZATION_NOT_FOUND.formatted(organizationId, municipalityId)));
	}

	public void deleteOrganization(final String municipalityId, final String organizationId) {
		final var entity = organizationRepository.findByIdAndMunicipalityId(organizationId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ORGANIZATION_NOT_FOUND.formatted(organizationId, municipalityId)));

		if (entity.getChecklists().stream().anyMatch(checklist -> checklist.getLifeCycle() != LifeCycle.RETIRED)) {
			throw Problem.valueOf(CONFLICT, ORGANIZATION_HAS_CHECKLISTS.formatted(organizationId));
		}
		organizationRepository.delete(entity);
	}
}
