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

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.api.model.ChecklistCreateRequest;
import se.sundsvall.checklist.api.model.ChecklistUpdateRequest;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.service.mapper.ChecklistMapper;

@Service
public class ChecklistService {

	private static final Logger LOG = LoggerFactory.getLogger(ChecklistService.class);

	private static final String CHECKLIST_NOT_FOUND = "Checklist not found";
	private static final String CHECKLIST_CANNOT_BE_DELETED = "Cannot delete checklist with lifecycle: %s";
	private static final String DEEP_COPY_ERROR = "Error creating deep copy of checklist entity";

	private final OrganizationRepository organizationRepository;
	private final ChecklistRepository checklistRepository;
	private final ObjectMapper objectMapper;

	public ChecklistService(
		OrganizationRepository organizationRepository,
		ChecklistRepository checklistRepository,
		ObjectMapper objectMapper) {

		this.organizationRepository = organizationRepository;
		this.checklistRepository = checklistRepository;
		this.objectMapper = objectMapper;
	}

	public List<Checklist> getAllChecklists() {
		return checklistRepository.findAll().stream()
			.map(ChecklistMapper::toChecklist)
			.toList();
	}

	public Checklist getChecklistById(final String id) {
		final var checklist = checklistRepository.findById(id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND));
		return toChecklist(checklist);
	}

	@Transactional
	public Checklist createChecklist(final ChecklistCreateRequest request) {
		if (checklistRepository.existsByName(request.getName())) {
			throw Problem.valueOf(BAD_REQUEST, "Checklist with name: %s already exists".formatted(request.getName()));
		}

		final var organization = organizationRepository.findByOrganizationNumber(request.getOrganizationNumber())
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "Organization with organization number: %s does not exist".formatted(request.getOrganizationNumber())));

		final var entity = checklistRepository.save(toChecklistEntity(request));
		organization.getChecklists().add(entity);
		organizationRepository.save(organization);
		return toChecklist(entity);
	}

	public Checklist createNewVersion(final String checklistId) {
		final var entity = checklistRepository.findById(checklistId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND));
		if (checklistRepository.existsByNameAndLifeCycle(entity.getName(), CREATED)) {
			throw Problem.valueOf(BAD_REQUEST,
				"Checklist already has a draft version in progress preventing another draft version from being created");
		}

		final var copy = createDeepCopy(entity);
		copy.setLifeCycle(CREATED);
		copy.setId(null);
		copy.setVersion(entity.getVersion() + 1);

		return toChecklist(checklistRepository.save(copy));
	}

	public Checklist activateChecklist(final String checklistId) {
		final var entity = checklistRepository.findById(checklistId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND));
		checklistRepository.findByNameAndLifeCycle(entity.getName(), ACTIVE)
			.ifPresent(ch -> ch.setLifeCycle(DEPRECATED));
		entity.setLifeCycle(ACTIVE);
		return toChecklist(checklistRepository.save(entity));
	}

	@Transactional
	public void deleteChecklist(final String id) {
		final var checklist = checklistRepository.findById(id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND));

		if (checklist.getLifeCycle() == ACTIVE || checklist.getLifeCycle() == DEPRECATED) {
			throw Problem.valueOf(BAD_REQUEST, CHECKLIST_CANNOT_BE_DELETED.formatted(checklist.getLifeCycle()));
		}

		// First remove checklist from organization if it has been attached to such
		organizationRepository.findByChecklistsId(id).ifPresent(organization -> {
			organization.getChecklists().removeIf(ch -> Objects.equals(id, ch.getId()));
			organizationRepository.save(organization);
		});

		checklistRepository.deleteById(id);
	}

	public Checklist updateChecklist(final String id, final ChecklistUpdateRequest request) {
		final var entity = checklistRepository.findById(id)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND));

		return toChecklist(checklistRepository.save(updateChecklistEntity(entity, request)));
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
