package se.sundsvall.checklist.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseEntity;
import static se.sundsvall.checklist.TestObjectFactory.createTaskEntity;
import static se.sundsvall.checklist.integration.db.model.enums.Permission.SUPERADMIN;
import static se.sundsvall.checklist.integration.db.model.enums.QuestionType.YES_OR_NO_WITH_TEXT;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.MANAGER_FOR_NEW_EMPLOYEE;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updateChecklistEntity;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updatePhaseEntity;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updateTaskEntity;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.checklist.api.model.ChecklistCreateRequest;
import se.sundsvall.checklist.api.model.ChecklistUpdateRequest;
import se.sundsvall.checklist.api.model.PhaseCreateRequest;
import se.sundsvall.checklist.api.model.PhaseUpdateRequest;
import se.sundsvall.checklist.api.model.TaskCreateRequest;
import se.sundsvall.checklist.api.model.TaskUpdateRequest;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.model.enums.Permission;
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

class ChecklistMapperTest {

	@Test
	void toTaskEntity() {
		// Arrange
		final var phaseEntity = PhaseEntity.builder().build();
		final var heading = "heading";
		final var permission = Permission.SUPERADMIN;
		final var questionType = QuestionType.COMPLETED_OR_NOT_RELEVANT;
		final var roleType = RoleType.MANAGER_FOR_NEW_EMPLOYEE;
		final var text = "text";
		final var sortOrder = 5;
		final var createdBy = "someUser";

		final var request = TaskCreateRequest.builder()
			.withHeading(heading)
			.withPermission(permission)
			.withQuestionType(questionType)
			.withRoleType(roleType)
			.withSortOrder(sortOrder)
			.withText(text)
			.withCreatedBy(createdBy)
			.build();

		// Act
		final var entity = ChecklistMapper.toTaskEntity(request, phaseEntity);

		// Assert
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getHeading()).isEqualTo(heading);
		assertThat(entity.getId()).isNull();
		assertThat(entity.getPermission()).isEqualTo(permission);
		assertThat(entity.getQuestionType()).isEqualTo(questionType);
		assertThat(entity.getRoleType()).isEqualTo(roleType);
		assertThat(entity.getText()).isEqualTo(text);
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getSortOrder()).isEqualTo(sortOrder);
		assertThat(entity.getLastSavedBy()).isEqualTo(createdBy);
		assertThat(entity.getPhase()).isEqualTo(phaseEntity);
	}

	@Test
	void toPhaseEntity() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var bodyText = "bodyText";
		final var name = "name";
		final var permission = Permission.SUPERADMIN;
		final var sortOrder = 101;
		final var timeToComplete = "timeToComplete";
		final var createdBy = "someUser";

		final var request = PhaseCreateRequest.builder()
			.withBodyText(bodyText)
			.withName(name)
			.withPermission(permission)
			.withSortOrder(sortOrder)
			.withTimeToComplete(timeToComplete)
			.withCreatedBy(createdBy)
			.build();

		// Act
		final var entity = ChecklistMapper.toPhaseEntity(request, municipalityId);

		// Assert
		assertThat(entity.getBodyText()).isEqualTo(bodyText);
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getName()).isEqualTo(name);
		assertThat(entity.getPermission()).isEqualTo(permission);
		assertThat(entity.getSortOrder()).isEqualTo(sortOrder);
		assertThat(entity.getTimeToComplete()).isEqualTo(timeToComplete);
		assertThat(entity.getUpdated()).isNull();
		assertThat(entity.getLastSavedBy()).isEqualTo(createdBy);
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void toChecklistEntity() {
		// Arrange
		final var displayName = "displayName";
		final var name = "name";
		final var municipalityId = "municipalityId";
		final var organizationNumber = 123456;
		final var createdBy = "someUser";

		final var request = ChecklistCreateRequest.builder()
			.withDisplayName(displayName)
			.withName(name)
			.withOrganizationNumber(organizationNumber)
			.withCreatedBy(createdBy)
			.build();

		// Act
		final var entity = ChecklistMapper.toChecklistEntity(request, municipalityId);

		// Assert
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("displayName", "municipalityId", "name", "organizationNumber", "version", "lifeCycle", "tasks", "lastSavedBy");
		assertThat(entity.getDisplayName()).isEqualTo(displayName);
		assertThat(entity.getName()).isEqualTo(name);
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(entity.getVersion()).isOne();
		assertThat(entity.getTasks()).isNullOrEmpty();
		assertThat(entity.getLastSavedBy()).isEqualTo(createdBy);
	}

	@Test
	void toChecklist() {
		// Arrange
		final var created = OffsetDateTime.now().minusWeeks(1);
		final var id = "id";
		final var lifeCycle = LifeCycle.ACTIVE;
		final var name = "name";
		final var municipalityId = "municipalityId";
		final var displayName = "displayName";
		final var phase = PhaseEntity.builder().build();
		final var tasks = List.of(TaskEntity.builder().withPhase(phase).build());
		final var updated = OffsetDateTime.now();
		final var version = 123;
		final var lastSavedBy = "someUser";

		final var entity = ChecklistEntity.builder()
			.withCreated(created)
			.withId(id)
			.withLifeCycle(lifeCycle)
			.withMunicipalityId(municipalityId)
			.withName(name)
			.withDisplayName(displayName)
			.withTasks(tasks)
			.withUpdated(updated)
			.withLastSavedBy(lastSavedBy)
			.withVersion(version)
			.build();

		// Act
		final var bean = ChecklistMapper.toChecklist(entity);

		// Assert
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getLifeCycle()).isEqualTo(lifeCycle);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getDisplayName()).isEqualTo(displayName);
		assertThat(bean.getPhases()).isNullOrEmpty();
		assertThat(bean.getUpdated()).isEqualTo(updated);
		assertThat(bean.getVersion()).isEqualTo(version);
		assertThat(bean.getLastSavedBy()).isEqualTo(lastSavedBy);
	}

	@Test
	void toPhase() {
		// Arrange
		final var bodyText = "bodyText";
		final var created = OffsetDateTime.now().minusWeeks(1);
		final var id = "id";
		final var name = "name";
		final var permission = Permission.ADMIN;
		final var sortOrder = 321;
		final var timeToComplete = "timeToComplete";
		final var updated = OffsetDateTime.now();
		final var lastSavedBy = "someUser";

		final var entity = PhaseEntity.builder()
			.withBodyText(bodyText)
			.withCreated(created)
			.withId(id)
			.withName(name)
			.withPermission(permission)
			.withSortOrder(sortOrder)
			.withTimeToComplete(timeToComplete)
			.withUpdated(updated)
			.withLastSavedBy(lastSavedBy)
			.build();

		// Act
		final var bean = ChecklistMapper.toPhase(entity);

		// Assert
		assertThat(bean.getBodyText()).isEqualTo(bodyText);
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getName()).isEqualTo(name);
		assertThat(bean.getPermission()).isEqualTo(permission);
		assertThat(bean.getSortOrder()).isEqualTo(sortOrder);
		assertThat(bean.getTasks()).isNullOrEmpty();
		assertThat(bean.getTimeToComplete()).isEqualTo(timeToComplete);
		assertThat(bean.getUpdated()).isEqualTo(updated);
		assertThat(bean.getLastSavedBy()).isEqualTo(lastSavedBy);
	}

	@Test
	void updateTaskEntityTest() {
		final var entity = createTaskEntity();
		final var request = TaskUpdateRequest.builder()
			.withHeading("new heading")
			.withText("new text")
			.withRoleType(MANAGER_FOR_NEW_EMPLOYEE)
			.withPermission(SUPERADMIN)
			.withQuestionType(YES_OR_NO_WITH_TEXT)
			.withUpdatedBy("someOtherUser")
			.build();

		assertThat(entity).satisfies(e -> {
			assertThat(e.getHeading()).isNotEqualTo("new heading");
			assertThat(e.getText()).isNotEqualTo("new text");
			assertThat(e.getRoleType()).isNotEqualTo(MANAGER_FOR_NEW_EMPLOYEE);
			assertThat(e.getPermission()).isNotEqualTo(SUPERADMIN);
			assertThat(e.getQuestionType()).isNotEqualTo(YES_OR_NO_WITH_TEXT);
			assertThat(e.getLastSavedBy()).isEqualTo("someUser");
		});

		final var result = updateTaskEntity(entity, request);

		assertThat(result).isEqualTo(entity).satisfies(r -> {
			assertThat(r.getHeading()).isEqualTo("new heading");
			assertThat(r.getText()).isEqualTo("new text");
			assertThat(r.getRoleType()).isEqualTo(MANAGER_FOR_NEW_EMPLOYEE);
			assertThat(r.getPermission()).isEqualTo(SUPERADMIN);
			assertThat(r.getQuestionType()).isEqualTo(YES_OR_NO_WITH_TEXT);
			assertThat(r.getLastSavedBy()).isEqualTo("someOtherUser");
		});
	}

	@Test
	void updatePhaseEntityTest() {
		final var entity = createPhaseEntity();
		final var request = PhaseUpdateRequest.builder()
			.withName("new name")
			.withBodyText("new bodyText")
			.withTimeToComplete("P2Y")
			.withPermission(SUPERADMIN)
			.withSortOrder(2)
			.withUpdatedBy("someOtherUser")
			.build();

		assertThat(entity).satisfies(e -> {
			assertThat(e.getName()).isNotEqualTo("new name");
			assertThat(e.getBodyText()).isNotEqualTo("new bodyText");
			assertThat(e.getTimeToComplete()).isNotEqualTo("P2Y");
			assertThat(e.getPermission()).isNotEqualTo(SUPERADMIN);
			assertThat(e.getSortOrder()).isNotEqualTo(2);
			assertThat(e.getLastSavedBy()).isEqualTo("someUser");
		});

		final var result = updatePhaseEntity(entity, request);

		assertThat(result).isEqualTo(entity).satisfies(r -> {
			assertThat(r.getName()).isEqualTo("new name");
			assertThat(r.getBodyText()).isEqualTo("new bodyText");
			assertThat(r.getTimeToComplete()).isEqualTo("P2Y");
			assertThat(r.getPermission()).isEqualTo(SUPERADMIN);
			assertThat(r.getSortOrder()).isEqualTo(2);
			assertThat(r.getLastSavedBy()).isEqualTo("someOtherUser");
		});
	}

	@Test
	void updateChecklistEntityTest() {
		final var entity = ChecklistEntity.builder()
			.withVersion(123)
			.withDisplayName("displayName")
			.withLastSavedBy("someUser")
			.build();
		final var request = ChecklistUpdateRequest.builder()
			.withDisplayName("newDisplayName")
			.withUpdatedBy("someOtherUser")
			.build();

		assertThat(entity).satisfies(e -> {
			assertThat(e.getDisplayName()).isEqualTo("displayName");
			assertThat(e.getVersion()).isEqualTo(123);
			assertThat(e.getTasks()).isEmpty();
			assertThat(e.getLastSavedBy()).isEqualTo("someUser");
		});

		final var result = updateChecklistEntity(entity, request);

		assertThat(result)
			.hasAllNullFieldsOrPropertiesExcept("displayName", "version", "tasks", "lastSavedBy")
			.isEqualTo(entity)
			.satisfies(r -> {
				assertThat(r.getDisplayName()).isEqualTo("newDisplayName");
				assertThat(r.getVersion()).isEqualTo(123);
				assertThat(r.getTasks()).isNullOrEmpty();
				assertThat(r.getLastSavedBy()).isEqualTo("someOtherUser");
			});
	}

	@Test
	void toChecklistFromNull() {
		assertThat(ChecklistMapper.toChecklist(null)).isNull();
	}

	@Test
	void toPhasesFromNull() {
		assertThat(ChecklistMapper.toPhases(null)).isEmpty();
	}

	@Test
	void toPhaseFromNull() {
		assertThat(ChecklistMapper.toPhase(null)).isNull();
	}

	@Test
	void toTasksFromNull() {
		assertThat(ChecklistMapper.toTasks(null)).isEmpty();
	}

	@Test
	void toTaskFromNull() {
		assertThat(ChecklistMapper.toTask(null)).isNull();
	}

	@Test
	void toChecklistEntityFromNull() {
		assertThat(ChecklistMapper.toChecklistEntity(null, "whatever")).isNull();
	}

	@Test
	void toPhaseEntityFromNull() {
		assertThat(ChecklistMapper.toPhaseEntity(null, "randomString")).isNull();
	}

	@Test
	void toTaskEntityFromNull() {
		assertThat(ChecklistMapper.toTaskEntity(null, PhaseEntity.builder().build())).isNull();
	}
}
