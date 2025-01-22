package se.sundsvall.checklist.service;

import static org.springframework.util.CollectionUtils.isEmpty;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.ACTIVE;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.CREATED;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.DEPRECATED;
import static se.sundsvall.checklist.service.EventService.CHECKLIST_UPDATED;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toChecklist;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.toChecklistEntity;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updateChecklistEntity;
import static se.sundsvall.checklist.service.util.ChecklistUtils.findMatchingTaskIds;
import static se.sundsvall.checklist.service.util.SortingUtils.getChecklistItemIds;

import generated.se.sundsvall.eventlog.EventType;
import generated.se.sundsvall.eventlog.PageEvent;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.data.domain.Pageable;
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
import se.sundsvall.checklist.service.util.ChecklistUtils;

@Service
public class ChecklistService {

	private static final String CHECKLIST_NOT_FOUND = "Checklist not found within municipality %s";
	private static final String CHECKLIST_NAME_ALREADY_EXIST = "Checklist with name '%s' already exists in municipality %s";
	private static final String CHECKLIST_NOT_CONNECTED_TO_ORGANIZATION = "No organization is connected to checklist with id %s";
	private static final String ORGANIZATION_NOT_FOUND = "Organization with organization number %s does not exist within municipality %s";
	private static final String CHECKLIST_PROCESS_ERROR = "Error occured when processing checklist with id %s";
	private static final String CHECKLIST_CANNOT_BE_DELETED = "Cannot delete checklist with lifecycle %s";
	private static final String CHECKLIST_DRAFT_IN_PROGRESS = "Checklist already has a draft version in progress preventing another draft version from being created";
	private static final String ORGANIZATION_HAS_EXISTING_CHECKLIST = "Organization %s already has a defined checklist and can therefor not create a new checklist";

	private final OrganizationRepository organizationRepository;
	private final ChecklistRepository checklistRepository;
	private final ChecklistBuilder checklistBuilder;
	private final ChecklistUtils checklistUtils;
	private final SortorderService sortorderService;
	private final EventService eventService;

	public ChecklistService(
		OrganizationRepository organizationRepository,
		ChecklistRepository checklistRepository,
		ChecklistBuilder checklistBuilder,
		ChecklistUtils checklistUtils,
		SortorderService sortorderService,
		EventService eventService) {

		this.organizationRepository = organizationRepository;
		this.checklistRepository = checklistRepository;
		this.checklistBuilder = checklistBuilder;
		this.checklistUtils = checklistUtils;
		this.sortorderService = sortorderService;
		this.eventService = eventService;
	}

	public List<Checklist> getChecklists(final String municipalityId) {
		return checklistRepository.findAllByMunicipalityId(municipalityId).stream()
			.map(this::toChecklistWithAppliedSortOrder)
			.toList();
	}

	public Checklist getChecklist(final String municipalityId, final String id) {
		return checklistRepository.findByIdAndMunicipalityId(id, municipalityId)
			.map(this::toChecklistWithAppliedSortOrder)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId)));
	}

	@Transactional
	public Checklist createChecklist(final String municipalityId, final ChecklistCreateRequest request) {
		if (checklistRepository.existsByNameAndMunicipalityId(request.getName(), municipalityId)) {
			throw Problem.valueOf(BAD_REQUEST, CHECKLIST_NAME_ALREADY_EXIST.formatted(request.getName(), municipalityId));
		}

		final var organization = organizationRepository.findByOrganizationNumberAndMunicipalityId(request.getOrganizationNumber(), municipalityId)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, ORGANIZATION_NOT_FOUND.formatted(request.getOrganizationNumber(), municipalityId)));

		// A new checklist is only possible to create if the organization does not have any checklist (in any lifecyclestatus)
		// connected to it. If it has one or more versions of a checklist connected, a new version of the existing checklist
		// should be made instead, as each organizational unit can only have one checklist (but 1 to n versions of it)
		if (!isEmpty(organization.getChecklists())) {
			throw Problem.valueOf(BAD_REQUEST, ORGANIZATION_HAS_EXISTING_CHECKLIST.formatted(organization.getOrganizationNumber()));
		}

		final var checklistEntity = checklistRepository.save(toChecklistEntity(request, municipalityId));
		organization.getChecklists().add(checklistEntity);
		organizationRepository.save(organization);
		final var checklist = toChecklist(checklistEntity);
		eventService.createChecklistEvent(EventType.CREATE, EventService.CHECKLIST_CREATED.formatted(checklistEntity.getId()), checklistEntity, request.getCreatedBy());
		return checklist;
	}

	@Transactional
	public Checklist createNewVersion(final String municipalityId, final String checklistId) {
		final var origin = checklistRepository.findByIdAndMunicipalityId(checklistId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId)));
		final var organization = organizationRepository.findByChecklistsIdAndChecklistsMunicipalityId(checklistId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_CONNECTED_TO_ORGANIZATION.formatted(checklistId)));

		if (checklistRepository.existsByNameAndMunicipalityIdAndLifeCycle(origin.getName(), municipalityId, CREATED)) {
			throw Problem.valueOf(BAD_REQUEST, CHECKLIST_DRAFT_IN_PROGRESS);
		}

		// Prepare by disconnecting origin from the organization (needed to not have circular references when cloning)
		origin.setOrganization(null);
		// Clone and save new version in database
		final var copy = checklistRepository.save(checklistUtils.clone(origin));
		// Reconnect the organization to the original checklist and also connect it to the new version
		origin.setOrganization(organization);
		copy.setOrganization(organization);
		// Connect new version to organization
		organization.getChecklists().add(copy);

		// Copy possible custom sort order that exists for tasks in the original checklist to new version of the checklist
		final var translationMap = findMatchingTaskIds(copy, origin);
		sortorderService.copySortorderItems(translationMap);

		return toChecklistWithAppliedSortOrder(copy);
	}

	@Transactional
	public Checklist activateChecklist(final String municipalityId, final String checklistId) {
		final var entity = checklistRepository.findByIdAndMunicipalityId(checklistId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId)));
		checklistRepository.findByNameAndMunicipalityIdAndLifeCycle(entity.getName(), municipalityId, ACTIVE)
			.ifPresent(ch -> ch.setLifeCycle(DEPRECATED));
		entity.setLifeCycle(ACTIVE);

		return toChecklistWithAppliedSortOrder(checklistRepository.saveAndFlush(entity));
	}

	@Transactional
	public void deleteChecklist(final String municipalityId, final String id, final String user) {
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

		// Remove custom sort options for components in the checklist
		getChecklistItemIds(checklist).forEach(sortorderService::deleteSortorderItem);

		// Remove checklist
		checklistRepository.delete(checklist);
		eventService.createChecklistEvent(EventType.DELETE, EventService.CHECKLIST_DELETED.formatted(checklist.getId()), checklist, user);
	}

	public Checklist updateChecklist(final String municipalityId, final String id, final ChecklistUpdateRequest request) {
		final var checklistEntity = checklistRepository.findByIdAndMunicipalityId(id, municipalityId)
			.map(e -> checklistRepository.save(updateChecklistEntity(e, request)))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId)));

		final var checklist = toChecklistWithAppliedSortOrder(checklistEntity);
		eventService.createChecklistEvent(EventType.UPDATE, CHECKLIST_UPDATED.formatted(checklistEntity.getId()), checklistEntity, request.getUpdatedBy());
		return checklist;
	}

	private Checklist toChecklistWithAppliedSortOrder(final ChecklistEntity entity) {
		return Stream.of(entity)
			.map(checklistBuilder::buildChecklist)
			.map(checklist -> sortorderService.applySortingToChecklist(entity.getOrganization().getMunicipalityId(), entity.getOrganization().getOrganizationNumber(), checklist))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, CHECKLIST_PROCESS_ERROR.formatted(entity.getId())));
	}

	public PageEvent getEvents(final String municipalityId, final String checklistId, final Pageable pageable) {
		if (!checklistRepository.existsByMunicipalityIdAndId(municipalityId, checklistId)) {
			throw Problem.valueOf(NOT_FOUND, CHECKLIST_NOT_FOUND.formatted(municipalityId));
		}

		return eventService.getChecklistEvents(municipalityId, checklistId, pageable);
	}
}
