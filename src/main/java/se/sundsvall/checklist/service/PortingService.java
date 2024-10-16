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
import java.util.List;
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
import se.sundsvall.checklist.integration.db.model.enums.RoleType;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.service.mapper.OrganizationMapper;

/**
 * Service for exporting and importing checklists as json strings
 */
@Service
public class PortingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PortingService.class);

	private final ChecklistRepository checklistRepository;
	private final OrganizationRepository organizationRepository;
	private final ObjectMapper objectMapper;

	public PortingService(final OrganizationRepository repository, final ChecklistRepository checklistRepository) {
		this.organizationRepository = repository;
		this.checklistRepository = checklistRepository;
		this.objectMapper = new ObjectMapper()
			.findAndRegisterModules()
			.setDateFormat(new SimpleDateFormat(DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.getPattern()))
			.setSerializationInclusion(Include.NON_NULL);
	}

	/**
	 * Export a checklist matching the provided company, department and version as a json structure.
	 *
	 * @param organizationNumber The organizationNumber for the unit owning the checklist to export
	 * @param roleType           The roletype for the checklist to export
	 * @param version            Version of the checklist to export. Parameter is optional, the latest version will be
	 *                           exported if left out.
	 * @return a json string representation of the full structure for the checklist.
	 */
	public String exportChecklist(int organizationNumber, RoleType roleType, Integer version) {
		return organizationRepository.findByOrganizationNumber(organizationNumber)
			.map(OrganizationEntity::getChecklists)
			.orElse(Collections.emptyList())
			.stream()
			.filter(ch -> Objects.equals(roleType, ch.getRoleType()))
			.sorted(comparing(ChecklistEntity::getVersion).reversed())
			.filter(ch -> Objects.isNull(version) || Objects.equals(version, ch.getVersion()))
			.map(this::clearFields)
			.map(throwingFunction(objectMapper::writeValueAsString))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No checklist matching sent in parameters exist."));
	}

	/**
	 * Import a checklist to the provided company id on root level
	 *
	 * @param organizationNumber The organization number for the organizational unit where the checklist will be
	 *                           imported to.
	 * @param organizationName   The name of the company where the checklist will be imported to.
	 * @param jsonStructure      a json string representation of the full structure for the checklist to import.
	 * @param replaceVersion     signal if existing checklist shall be replaced, or if a new version of the checklist
	 *                           shall be created.
	 */
	@Transactional
	public String importChecklist(int organizationNumber, String organizationName, String jsonStructure, boolean replaceVersion) {
		try {
			LOGGER.info("Starting to import checklist");

			// Deserialize json string into checklist entity (and sub ordinates)
			final var checklist = objectMapper.readValue(jsonStructure, ChecklistEntity.class);

			// Find (or create if it does not exist) the organization entity matching sent in organizationNumber
			final var organization = organizationRepository.findByOrganizationNumber(organizationNumber)
				.orElseGet(() -> organizationRepository.save(OrganizationMapper.toOrganizationEntity(organizationNumber, organizationName)));

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
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Exception when importing checklist.");
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
			throw Problem.valueOf(CONFLICT, "The organization has an existing checklist with lifecycle status CREATED present, operation aborted.");
		}

		return createVersion(organization, checklist);
	}

	private String replaceVersion(OrganizationEntity organization, ChecklistEntity entity, LifeCycle lifecycle) {
		LOGGER.info("Replacing existing checklist with status {} with checklist based on import", lifecycle);

		// Find and delete current active checklist
		final var existingEntity = organization.getChecklists().stream()
			.filter(ch -> Objects.equals(lifecycle, ch.getLifeCycle()))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No checklist with lifecycle status %s found.".formatted(lifecycle)));

		// Update existingEntity with values from incoming structure
		updateExistingChecklist(existingEntity, entity);
		checklistRepository.save(existingEntity);

		return existingEntity.getId();
	}

	private void updateExistingChecklist(final ChecklistEntity existingEntity, final ChecklistEntity entity) {
		// Clear current tasks and phases from checklist
		existingEntity.getPhases().stream()
			.forEach(ph -> ph.getTasks().clear());
		existingEntity.getPhases().clear();

		// Update existing checklist with values from incoming structure
		existingEntity.setDisplayName(entity.getDisplayName());
		existingEntity.setRoleType(entity.getRoleType());
		existingEntity.getPhases().addAll(entity.getPhases());
	}

	private String createVersion(OrganizationEntity organization, ChecklistEntity entity) {
		LOGGER.info("Adding new version of checklist with status created based on import");

		// If organization has any existing version, that name will be used, otherwise name from incoming values will be used
		entity.setName(
			ofNullable(organization.getChecklists()).orElse(Collections.emptyList()).stream()
				.map(ChecklistEntity::getName)
				.findAny()
				.orElse(entity.getName()));

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
		checklistEntity.getPhases().stream()
			.map(PhaseEntity::getTasks)
			.flatMap(List::stream)
			.forEach(task -> {
				task.setId(null);
				task.setCreated(null);
				task.setUpdated(null);
			});

		checklistEntity.getPhases().stream()
			.forEach(phase -> {
				phase.setId(null);
				phase.setCreated(null);
				phase.setUpdated(null);
			});

		checklistEntity.setId(null);
		checklistEntity.setLifeCycle(null);
		checklistEntity.setCreated(null);
		checklistEntity.setUpdated(null);

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
