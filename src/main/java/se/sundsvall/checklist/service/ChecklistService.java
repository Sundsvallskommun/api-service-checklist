package se.sundsvall.checklist.service;

import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.ACTIVE;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.CREATED;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.DEPRECATED;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toChecklist;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toChecklistEntity;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updateChecklistEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.api.model.ChecklistCreateRequest;
import se.sundsvall.checklist.api.model.ChecklistUpdateRequest;
import se.sundsvall.checklist.integration.db.ChecklistBuilder;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;

@Service
public class ChecklistService {

	private static final Logger LOG = LoggerFactory.getLogger(ChecklistService.class);

	private static final String CHECKLIST_NOT_FOUND = "Checklist not found within municipality %s";
	private static final String CHECKLIST_CANNOT_BE_DELETED = "Cannot delete checklist with lifecycle %s";
	private static final String DEEP_COPY_ERROR = "Error creating deep copy of checklist entity";

	private final OrganizationRepository organizationRepository;
	private final ChecklistRepository checklistRepository;
	private final ChecklistBuilder checklistBuilder;
	private final ObjectMapper objectMapper;

	public ChecklistService(
		OrganizationRepository organizationRepository,
		ChecklistRepository checklistRepository,
		ChecklistBuilder checklistBuilder,
		ObjectMapper objectMapper) {

		this.organizationRepository = organizationRepository;
		this.checklistRepository = checklistRepository;
		this.checklistBuilder = checklistBuilder;
		this.objectMapper = objectMapper;
	}

	public List<Checklist> getChecklists(final String municipalityId) {
		return checklistRepository.findAllByMunicipalityId(municipalityId).stream()
			.map(checklistBuilder::buildChecklist)
			.toList();
	}

	public Checklist getChecklist(final String municipalityId, final String id) {
		return checklistRepository.findByIdAndMunicipalityId(id, municipalityId)
			.map(checklistBuilder::buildChecklist)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId)));
	}

	@Transactional
	public Checklist createChecklist(final String municipalityId, final ChecklistCreateRequest request) {
		if (checklistRepository.existsByNameAndMunicipalityId(request.getName(), municipalityId)) {
			throw Problem.valueOf(BAD_REQUEST, "Checklist with name '%s' already exists in municipality %s".formatted(request.getName(), municipalityId));
		}

		final var organization = organizationRepository.findByOrganizationNumberAndMunicipalityId(request.getOrganizationNumber(), municipalityId)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "Organization with organization number %s does not exist within municipality %s.".formatted(request.getOrganizationNumber(), municipalityId)));

		final var entity = checklistRepository.save(toChecklistEntity(request, municipalityId));
		organization.getChecklists().add(entity);
		organizationRepository.save(organization);
		return toChecklist(entity);
	}

	public Checklist createNewVersion(final String municipalityId, final String checklistId) {
		final var entity = checklistRepository.findByIdAndMunicipalityId(checklistId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId)));
		if (checklistRepository.existsByNameAndMunicipalityIdAndLifeCycle(entity.getName(), municipalityId, CREATED)) {
			throw Problem.valueOf(BAD_REQUEST,
				"Checklist already has a draft version in progress preventing another draft version from being created");
		}

		final var copy = createDeepCopy(entity);
		copy.setLifeCycle(CREATED);
		copy.setId(null);
		copy.setVersion(entity.getVersion() + 1);

		return toChecklist(checklistRepository.save(copy));
	}

	public Checklist activateChecklist(final String municipalityId, final String checklistId) {
		final var entity = checklistRepository.findByIdAndMunicipalityId(checklistId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId)));
		checklistRepository.findByNameAndMunicipalityIdAndLifeCycle(entity.getName(), municipalityId, ACTIVE)
			.ifPresent(ch -> ch.setLifeCycle(DEPRECATED));
		entity.setLifeCycle(ACTIVE);
		return toChecklist(checklistRepository.save(entity));
	}

	@Transactional
	public void deleteChecklist(final String municipalityId, final String id) {
		final var checklist = checklistRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId)));

		if (checklist.getLifeCycle() == ACTIVE || checklist.getLifeCycle() == DEPRECATED) {
			throw Problem.valueOf(BAD_REQUEST, CHECKLIST_CANNOT_BE_DELETED.formatted(checklist.getLifeCycle()));
		}

		// Remove checklist from organization
		organizationRepository.findByChecklistsIdAndChecklistsMunicipalityId(id, municipalityId).ifPresent(organization -> {
			organization.getChecklists().removeIf(ch -> Objects.equals(id, ch.getId()));
			organizationRepository.save(organization);
		});

		// Remove checklist
		checklistRepository.delete(checklist);
	}

	public Checklist updateChecklist(final String municipalityId, final String id, final ChecklistUpdateRequest request) {
		return checklistRepository.findByIdAndMunicipalityId(id, municipalityId)
			.map(entity -> checklistBuilder.buildChecklist(checklistRepository.save(updateChecklistEntity(entity, request))))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId)));
	}

	ChecklistEntity createDeepCopy(final ChecklistEntity entity) {
		try {
			return objectMapper.readValue(objectMapper.writeValueAsString(entity), ChecklistEntity.class);
		} catch (final Exception e) {
			LOG.error(DEEP_COPY_ERROR, e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, DEEP_COPY_ERROR);
		}
	}
}
