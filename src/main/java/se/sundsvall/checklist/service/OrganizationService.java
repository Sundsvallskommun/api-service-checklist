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
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.service.mapper.OrganizationMapper;

@Service
public class OrganizationService {

	private static final String ORGANIZATION_NUMBER_ALREADY_EXISTS = "Organization with organization number %s already exists";
	private static final String ORGANIZATION_HAS_CHECKLISTS = "Organization with id %s has non retired checklists and cannot be deleted";
	private static final String ORGANIZATION_NOT_FOUND = "Organization with id %s does not exist";

	private final OrganizationRepository organizationRepository;

	public OrganizationService(final OrganizationRepository organizationRepository) {
		this.organizationRepository = organizationRepository;
	}

	public String createOrganization(final OrganizationCreateRequest request) {
		organizationRepository.findByOrganizationNumber(request.getOrganizationNumber())
			.ifPresent(organizationEntity -> {
				throw Problem.valueOf(CONFLICT, ORGANIZATION_NUMBER_ALREADY_EXISTS.formatted(request.getOrganizationNumber()));
			});

		var entity = organizationRepository.save(toOrganizationEntity(request));
		return entity.getId();
	}

	public List<Organization> fetchAllOrganizations() {
		return organizationRepository.findAll().stream()
			.map(OrganizationMapper::toOrganization)
			.toList();
	}

	public Organization fetchOrganizationById(final String organizationId) {
		return organizationRepository.findById(organizationId)
			.map(OrganizationMapper::toOrganization)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ORGANIZATION_NOT_FOUND.formatted(organizationId)));
	}

	public Organization updateOrganization(final String organizationId, final OrganizationUpdateRequest request) {
		return organizationRepository.findById(organizationId)
			.map(entity -> updateOrganizationEntity(entity, request))
			.map(organizationRepository::save)
			.map(OrganizationMapper::toOrganization)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ORGANIZATION_NOT_FOUND.formatted(organizationId)));
	}

	public void deleteOrganization(final String organizationId) {
		var entity = organizationRepository.findById(organizationId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ORGANIZATION_NOT_FOUND.formatted(organizationId)));

		if (entity.getChecklists().stream().anyMatch(checklist -> checklist.getLifeCycle() != LifeCycle.RETIRED)) {
			throw Problem.valueOf(CONFLICT, ORGANIZATION_HAS_CHECKLISTS.formatted(organizationId));
		}
		organizationRepository.delete(entity);
	}

}
