package se.sundsvall.checklist.service.util;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.CREATED;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;

@Component
public class ChecklistUtils {

	private static final Logger LOG = LoggerFactory.getLogger(ChecklistUtils.class);
	private static final String DEEP_COPY_ERROR = "Error creating clone of checklist entity";

	private final ObjectMapper objectMapper;

	ChecklistUtils(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Helper method for clearing all fields that needs to be cleared when creating a new version of a checklist. Method
	 * also sets lifecycle to CREATED and version to provided value. This method should not be used on other objects than
	 * detached entities. If used on non detached entities, the changes will be persisted in DB.
	 *
	 * @param  entity  the detached entity to set up as a new version
	 * @param  version the version to set
	 * @return         the cleared and updated entity, ready for saving as new entity
	 */
	static ChecklistEntity clearFields(ChecklistEntity entity, int version) {
		entity.setId(null);
		entity.setCreated(null);
		entity.setUpdated(null);
		entity.setLifeCycle(CREATED);
		entity.setVersion(version);
		entity.getTasks().stream()
			.forEach(task -> {
				task.setId(null);
				task.setCreated(null);
				task.setUpdated(null);
				task.setPhase(task.getPhase());
			});

		return entity;
	}

	/**
	 * Helper method for finding matching items in two checklist entities, where one is a new version of the original list
	 * and the id:s for the items is no longer equals
	 *
	 * @param  clone  the new version of the original checklist entity
	 * @param  origin the original checklist entity
	 * @return        a map where key is the id in the new version of the checklist and value is the id in the original
	 *                checklist
	 */
	public static Map<String, String> findMatchingTaskIds(ChecklistEntity clone, ChecklistEntity origin) {
		return ofNullable(clone.getTasks()).orElse(emptyList()).stream()
			.map(task -> toEntry(task, origin))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(toMap(Entry::getKey, Entry::getValue));
	}

	private static Optional<Entry<String, String>> toEntry(TaskEntity task, ChecklistEntity origin) {
		return ofNullable(origin.getTasks()).orElse(emptyList()).stream()
			.filter(possibleMatch -> compare(task, possibleMatch))
			.map(match -> Map.entry(task.getId(), match.getId()))
			.findAny();
	}

	/**
	 * Method for matching two tasks (not considering task id, as it will be different betwween the two tasks) against each
	 * other
	 *
	 * @param  a task to compare
	 * @param  b task to compare
	 * @return   true if a and b are considered equal, false otherwise
	 */
	static boolean compare(TaskEntity a, TaskEntity b) {
		if (ObjectUtils.anyNull(a, b)) {
			return Objects.equals(a, b);
		}

		return a.getSortOrder() == b.getSortOrder() &&
			a.getRoleType() == b.getRoleType() &&
			a.getQuestionType() == b.getQuestionType() &&
			Objects.equals(a.getHeading(), b.getHeading()) &&
			Objects.equals(a.getText(), b.getText()) &&
			hasSamePhase(a.getPhase(), b.getPhase());
	}

	private static boolean hasSamePhase(PhaseEntity a, PhaseEntity b) {
		if (ObjectUtils.anyNull(a, b)) {
			return Objects.equals(a, b);
		}

		return Objects.equals(a.getId(), b.getId());
	}

	/**
	 * Method for cloning a checklist entity (disconnecting it from backend repository) and returning it with a
	 * versionnumber higher than the original entity
	 *
	 * @param  entity the entity to clone
	 * @return        a clone of the entity
	 */
	public ChecklistEntity clone(final ChecklistEntity entity) {
		try {
			return clearFields(objectMapper.readValue(objectMapper.writeValueAsString(entity), ChecklistEntity.class), entity.getVersion() + 1);
		} catch (final Exception e) {
			LOG.error(DEEP_COPY_ERROR, e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, DEEP_COPY_ERROR);
		}
	}

}