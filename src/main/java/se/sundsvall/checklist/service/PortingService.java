package se.sundsvall.checklist.service;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static org.zalando.fauxpas.FauxPas.throwingFunction;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.ACTIVE;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.CREATED;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;
import se.sundsvall.checklist.service.mapper.OrganizationMapper;

/**
 * Service for exporting and importing checklists as json strings
 */
@Service
public class PortingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PortingService.class);

	private static final String MESSAGE_CHECKLIST_NOT_FOUND = "No checklist matching sent in parameters exist.";
	private static final String MESSAGE_CHECKLIST_WITH_STATUS_NOT_FOUND = "No checklist with lifecycle status %s found.";
	private static final String MESSAGE_PHASE_NOT_FOUND = "Phase with id %s is not present within municipality %s";
	private static final String MESSAGE_IMPORT_ERROR = "Exception when importing checklist.";
	private static final String MESSAGE_CHECKLIST_CONFLICT = "The organization has an existing checklist with lifecycle status CREATED present, operation aborted.";

	static final String SYSTEM = "SYSTEM";

	private final ChecklistRepository checklistRepository;
	private final OrganizationRepository organizationRepository;
	private final PhaseRepository phaseRepository;
	private final ObjectMapper objectMapper;

	public PortingService(final OrganizationRepository repository, final ChecklistRepository checklistRepository, final PhaseRepository phaseRepository) {
		this.organizationRepository = repository;
		this.checklistRepository = checklistRepository;
		this.phaseRepository = phaseRepository;
		this.objectMapper = new ObjectMapper()
			.findAndRegisterModules()
			.setDateFormat(new SimpleDateFormat(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.getPattern()))
			.setSerializationInclusion(Include.NON_NULL);
	}

	/**
	 * Export a checklist matching the provided company, department and version as a json structure.
	 *
	 * @param  municipalityId     The id of the municipality to which the company belongs
	 * @param  organizationNumber The organizationNumber for the unit owning the checklist to export
	 * @param  version            Version of the checklist to export. Parameter is optional, the latest version will be
	 *                            exported if left out.
	 * @return                    a json string representation of the full structure for the checklist.
	 */
	public String exportChecklist(String municipalityId, int organizationNumber, Integer version) {
		return organizationRepository.findByOrganizationNumberAndMunicipalityId(organizationNumber, municipalityId)
			.map(OrganizationEntity::getChecklists)
			.orElse(Collections.emptyList())
			.stream()
			.sorted(comparing(ChecklistEntity::getVersion).reversed())
			.filter(ch -> Objects.isNull(version) || Objects.equals(version, ch.getVersion()))
			.map(this::clearFields)
			.map(throwingFunction(objectMapper::writeValueAsString))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_CHECKLIST_NOT_FOUND));
	}

	/**
	 * Import a checklist to the provided company id on root level
	 *
	 * @param municipalityId     The id of the municipality to which the company belongs
	 * @param organizationNumber The organization number for the organizational unit where the checklist will be
	 *                           imported to.
	 * @param organizationName   The name of the company where the checklist will be imported to.
	 * @param jsonStructure      a json string representation of the full structure for the checklist to import.
	 * @param replaceVersion     signal if existing checklist shall be replaced, or if a new version of the checklist
	 *                           shall be created.
	 */
	@Transactional
	public String importChecklist(String municipalityId, int organizationNumber, String organizationName, String jsonStructure, boolean replaceVersion) {
		try {
			LOGGER.info("Starting to import checklist");

			// Deserialize json string into checklist entity (and sub ordinates)
			final var checklist = objectMapper.readValue(jsonStructure, ChecklistEntity.class);

			// Find and replace phase-entities with the ones that exists in the DB
			checklist.getTasks().forEach(task -> {
				final var phaseInDatabase = phaseRepository.findByIdAndMunicipalityId(task.getPhase().getId(), municipalityId)
					.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_PHASE_NOT_FOUND.formatted(task.getPhase().getId(), municipalityId)));
				task.setPhase(phaseInDatabase);
			});

			// Find (or create if it does not exist) the organization entity matching sent in organizationNumber
			final var organization = organizationRepository.findByOrganizationNumberAndMunicipalityId(organizationNumber, municipalityId)
				.orElseGet(() -> organizationRepository.save(OrganizationMapper.toOrganizationEntity(organizationNumber, organizationName, municipalityId)));

			final var hasActiveVersion = Optional.ofNullable(organization.getChecklists()).orElse(Collections.emptyList()).stream()
				.map(ChecklistEntity::getLifeCycle)
				.anyMatch(ACTIVE::equals);

			final var hasCreatedVersion = Optional.ofNullable(organization.getChecklists()).orElse(Collections.emptyList()).stream()
				.map(ChecklistEntity::getLifeCycle)
				.anyMatch(CREATED::equals);

			final var createdId = replaceVersion ? replaceVersion(organization, checklist, hasActiveVersion, hasCreatedVersion) : newVersion(organization, checklist, hasCreatedVersion);

			LOGGER.info("Successfully imported checklist");
			return createdId;

		} catch (final ThrowableProblem e) {
			throw e; // Rethrow exception
		} catch (final Exception e) {
			LOGGER.error("Exception when importing checklist", e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, MESSAGE_IMPORT_ERROR);
		}
	}

	private String replaceVersion(OrganizationEntity organization, ChecklistEntity checklist, boolean hasActiveVersion, boolean hasCreatedVersion) {
		if (hasCreatedVersion) {
			return replaceVersion(organization, checklist, CREATED);
		}
		if (hasActiveVersion) {
			return replaceVersion(organization, checklist, ACTIVE);
		}

		return createVersion(organization, checklist);
	}

	private String newVersion(OrganizationEntity organization, ChecklistEntity checklist, boolean hasCreatedVersion) {
		if (hasCreatedVersion) {
			LOGGER.info("The organization has an existing checklist with lifecycle status CREATED present, operation aborted.");
			throw Problem.valueOf(CONFLICT, MESSAGE_CHECKLIST_CONFLICT);
		}

		return createVersion(organization, checklist);
	}

	private String replaceVersion(OrganizationEntity organization, ChecklistEntity entity, LifeCycle lifecycle) {
		LOGGER.info("Replacing existing checklist with status {} with checklist based on import", lifecycle);

		// Find and delete current active checklist
		final var existingEntity = organization.getChecklists().stream()
			.filter(ch -> Objects.equals(lifecycle, ch.getLifeCycle()))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, MESSAGE_CHECKLIST_WITH_STATUS_NOT_FOUND.formatted(lifecycle)));

		// Update existingEntity with values from incoming structure
		updateExistingChecklist(existingEntity, entity);
		// Update "last-saved-by" on the existing entity
		updateLastSavedBy(existingEntity);

		checklistRepository.save(existingEntity);

		return existingEntity.getId();
	}

	private void updateLastSavedBy(final ChecklistEntity checklistEntity) {
		checklistEntity.setLastSavedBy(SYSTEM);
		checklistEntity.getTasks().forEach(taskEntity -> taskEntity.setLastSavedBy(SYSTEM));
	}

	private void updateExistingChecklist(final ChecklistEntity existingEntity, final ChecklistEntity entity) {
		// Clear current tasks from checklist
		existingEntity.getTasks().clear();

		// Update existing checklist with values from incoming structure
		existingEntity.setDisplayName(entity.getDisplayName());
		existingEntity.getTasks().addAll(entity.getTasks());
	}

	private String createVersion(OrganizationEntity organization, ChecklistEntity entity) {
		LOGGER.info("Adding new version of checklist with status created based on import");

		// If organization has any existing version, that name will be used, otherwise name from incoming values will be used
		entity.setName(
			ofNullable(organization.getChecklists()).orElse(Collections.emptyList()).stream()
				.map(ChecklistEntity::getName)
				.findAny()
				.orElse(entity.getName()));

		entity.setMunicipalityId(organization.getMunicipalityId());

		// Update "last-saved-by" on the existing entity
		updateLastSavedBy(entity);

		// Create new active checklist based on incoming values
		initializeChecklist(entity, calculateVersion(organization), CREATED);
		checklistRepository.save(entity);

		// Add checklist to organization
		organization.getChecklists().add(entity);

		return entity.getId();
	}

	private void initializeChecklist(ChecklistEntity checklistEntity, int version, LifeCycle lifecycle) {
		clearFields(checklistEntity);
		checklistEntity.setVersion(version);
		checklistEntity.setLifeCycle(lifecycle);
	}

	private ChecklistEntity clearFields(ChecklistEntity checklistEntity) {
		checklistEntity.getTasks().stream()
			.forEach(task -> {
				task.setId(null);
				task.setCreated(null);
				task.setUpdated(null);
				task.setPhase(PhaseEntity.builder()
					.withId(task.getPhase().getId())
					.withSortOrder(task.getPhase().getSortOrder())
					.build());
			});

		checklistEntity.setId(null);
		checklistEntity.setLifeCycle(null);
		checklistEntity.setCreated(null);
		checklistEntity.setUpdated(null);
		checklistEntity.setMunicipalityId(null);

		return checklistEntity;
	}

	private int calculateVersion(OrganizationEntity organizationEntity) {
		// Return largest checklist version + 1 or if no checklist exists, then 1
		return Optional.ofNullable(organizationEntity.getChecklists()).orElse(Collections.emptyList()).stream()
			.sorted(comparing(ChecklistEntity::getVersion).reversed())
			.map(ChecklistEntity::getVersion)
			.map(version -> version + 1)
			.findFirst()
			.orElse(1);
	}
}
