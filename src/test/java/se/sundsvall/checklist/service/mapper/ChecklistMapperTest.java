package se.sundsvall.checklist.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseEntity;
import static se.sundsvall.checklist.TestObjectFactory.createTaskEntity;
import static se.sundsvall.checklist.integration.db.model.enums.Permission.SUPERADMIN;
import static se.sundsvall.checklist.integration.db.model.enums.QuestionType.YES_OR_NO_WITH_TEXT;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.MANAGER;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updateChecklistEntity;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updatePhaseEntity;
import static se.sundsvall.checklist.service.mapper.ChecklistMapper.updateTaskEntity;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

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
		final var heading = "heading";
		final var permission = Permission.SUPERADMIN;
		final var questionType = QuestionType.COMPLETED_OR_NOT_RELEVANT;
		final var roleType = RoleType.MANAGER;
		final var text = "text";
		final var sortOrder = 5;

		final var request = TaskCreateRequest.builder()
			.withHeading(heading)
			.withPermission(permission)
			.withQuestionType(questionType)
			.withRoleType(roleType)
			.withSortOrder(sortOrder)
			.withText(text)
			.build();

		// Act
		final var entity = ChecklistMapper.toTaskEntity(request);

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
	}

	@Test
	void toPhaseEntity() {
		// Arrange
		final var bodyText = "bodyText";
		final var name = "name";
		final var permission = Permission.SUPERADMIN;
		final var sortOrder = 101;
		final var roleType = RoleType.MANAGER;
		final var timeToComplete = "timeToComplete";

		final var request = PhaseCreateRequest.builder()
			.withBodyText(bodyText)
			.withName(name)
			.withPermission(permission)
			.withSortOrder(sortOrder)
			.withRoleType(roleType)
			.withTimeToComplete(timeToComplete)
			.build();

		// Act
		final var entity = ChecklistMapper.toPhaseEntity(request);

		// Assert
		assertThat(entity.getBodyText()).isEqualTo(bodyText);
		assertThat(entity.getCreated()).isNull();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getName()).isEqualTo(name);
		assertThat(entity.getPermission()).isEqualTo(permission);
		assertThat(entity.getSortOrder()).isEqualTo(sortOrder);
		assertThat(entity.getRoleType()).isEqualTo(roleType);
		assertThat(entity.getTasks()).isNullOrEmpty();
		assertThat(entity.getTimeToComplete()).isEqualTo(timeToComplete);
		assertThat(entity.getUpdated()).isNull();
	}

	@Test
	void toChecklists() {
		// Arrange
		final var created = OffsetDateTime.now().minusWeeks(1);
		final var id = "id";
		final var lifeCycle = LifeCycle.ACTIVE;
		final var name = "name";
		final var displayName = "displayName";
		final var phases = List.of(PhaseEntity.builder().build());
		final var roleType = RoleType.MANAGER;
		final var updated = OffsetDateTime.now();
		final var version = 123;
		final var entity = ChecklistEntity.builder()
			.withCreated(created)
			.withId(id)
			.withLifeCycle(lifeCycle)
			.withName(name)
			.withDisplayName(displayName)
			.withPhases(phases)
			.withRoleType(roleType)
			.withUpdated(updated)
			.withVersion(version)
			.build();

		// Act
		final var list = ChecklistMapper.toChecklists(List.of(entity));

		// Assert
		assertThat(list).hasSize(1).allSatisfy(bean -> {
			assertThat(bean.getCreated()).isEqualTo(created);
			assertThat(bean.getId()).isEqualTo(id);
			assertThat(bean.getLifeCycle()).isEqualTo(lifeCycle);
			assertThat(bean.getName()).isEqualTo(name);
			assertThat(bean.getDisplayName()).isEqualTo(displayName);
			assertThat(bean.getPhases()).isNotEmpty().containsExactly(ChecklistMapper.toPhase(PhaseEntity.builder().build()));
			assertThat(bean.getRoleType()).isEqualTo(roleType);
			assertThat(bean.getUpdated()).isEqualTo(updated);
			assertThat(bean.getVersion()).isEqualTo(version);
		});
	}

	@Test
	void toChecklist() {
		// Arrange
		final var created = OffsetDateTime.now().minusWeeks(1);
		final var id = "id";
		final var lifeCycle = LifeCycle.ACTIVE;
		final var name = "name";
		final var displayName = "displayName";
		final var phases = List.of(PhaseEntity.builder().build());
		final var roleType = RoleType.MANAGER;
		final var updated = OffsetDateTime.now();
		final var version = 123;
		final var entity = ChecklistEntity.builder()
			.withCreated(created)
			.withId(id)
			.withLifeCycle(lifeCycle)
			.withName(name)
			.withDisplayName(displayName)
			.withPhases(phases)
			.withRoleType(roleType)
			.withUpdated(updated)
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
		assertThat(bean.getPhases()).isNotEmpty().containsExactly(ChecklistMapper.toPhase(PhaseEntity.builder().build()));
		assertThat(bean.getRoleType()).isEqualTo(roleType);
		assertThat(bean.getUpdated()).isEqualTo(updated);
		assertThat(bean.getVersion()).isEqualTo(version);
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
		final var roleType = RoleType.EMPLOYEE;
		final var tasks = List.of(TaskEntity.builder().build());
		final var timeToComplete = "timeToComplete";
		final var updated = OffsetDateTime.now();
		final var entity = PhaseEntity.builder()
			.withBodyText(bodyText)
			.withCreated(created)
			.withId(id)
			.withName(name)
			.withPermission(permission)
			.withSortOrder(sortOrder)
			.withRoleType(roleType)
			.withTasks(tasks)
			.withTimeToComplete(timeToComplete)
			.withUpdated(updated)
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
		assertThat(bean.getRoleType()).isEqualTo(roleType);
		assertThat(bean.getTasks()).isNotEmpty().containsExactly(ChecklistMapper.toTask(TaskEntity.builder().build()));
		assertThat(bean.getTimeToComplete()).isEqualTo(timeToComplete);
		assertThat(bean.getUpdated()).isEqualTo(updated);
	}

	@Test
	void updateTaskEntityTest() {
		final var entity = createTaskEntity();
		final var request = TaskUpdateRequest.builder()
			.withHeading("new heading")
			.withText("new text")
			.withRoleType(MANAGER)
			.withPermission(SUPERADMIN)
			.withQuestionType(YES_OR_NO_WITH_TEXT)
			.build();

		assertThat(entity).satisfies(e -> {
			assertThat(e.getHeading()).isNotEqualTo("new heading");
			assertThat(e.getText()).isNotEqualTo("new text");
			assertThat(e.getRoleType()).isNotEqualTo(MANAGER);
			assertThat(e.getPermission()).isNotEqualTo(SUPERADMIN);
			assertThat(e.getQuestionType()).isNotEqualTo(YES_OR_NO_WITH_TEXT);
		});

		final var result = updateTaskEntity(entity, request);

		assertThat(result).isEqualTo(entity).satisfies(r -> {
			assertThat(r.getHeading()).isEqualTo("new heading");
			assertThat(r.getText()).isEqualTo("new text");
			assertThat(r.getRoleType()).isEqualTo(MANAGER);
			assertThat(r.getPermission()).isEqualTo(SUPERADMIN);
			assertThat(r.getQuestionType()).isEqualTo(YES_OR_NO_WITH_TEXT);
		});
	}

	@Test
	void updatePhaseEntityTest() {
		final var entity = createPhaseEntity();
		final var request = PhaseUpdateRequest.builder()
			.withName("new name")
			.withBodyText("new bodyText")
			.withTimeToComplete("P2Y")
			.withRoleType(MANAGER)
			.withPermission(SUPERADMIN)
			.withSortOrder(2)
			.build();

		assertThat(entity).satisfies(e -> {
			assertThat(e.getName()).isNotEqualTo("new name");
			assertThat(e.getBodyText()).isNotEqualTo("new bodyText");
			assertThat(e.getTimeToComplete()).isNotEqualTo("P2Y");
			assertThat(e.getRoleType()).isNotEqualTo(MANAGER);
			assertThat(e.getPermission()).isNotEqualTo(SUPERADMIN);
			assertThat(e.getSortOrder()).isNotEqualTo(2);
		});

		final var result = updatePhaseEntity(entity, request);

		assertThat(result).isEqualTo(entity).satisfies(r -> {
			assertThat(r.getName()).isEqualTo("new name");
			assertThat(r.getBodyText()).isEqualTo("new bodyText");
			assertThat(r.getTimeToComplete()).isEqualTo("P2Y");
			assertThat(r.getRoleType()).isEqualTo(MANAGER);
			assertThat(r.getPermission()).isEqualTo(SUPERADMIN);
			assertThat(r.getSortOrder()).isEqualTo(2);
		});
	}

	@Test
	void updateChecklistEntityTest() {
		final var entity = ChecklistEntity.builder()
			.withVersion(123)
			.withDisplayName("displayName")
			.withRoleType(RoleType.EMPLOYEE).build();
		final var request = ChecklistUpdateRequest.builder()
			.withDisplayName("newDisplayName")
			.withRoleType(MANAGER)
			.build();

		assertThat(entity).satisfies(e -> {
			assertThat(e.getDisplayName()).isEqualTo("displayName");
			assertThat(e.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
			assertThat(e.getVersion()).isEqualTo(123);
			assertThat(e.getPhases()).isEmpty();
		});

		final var result = updateChecklistEntity(entity, request);

		assertThat(result)
			.hasAllNullFieldsOrPropertiesExcept("displayName", "roleType", "version", "phases")
			.isEqualTo(entity)
			.satisfies(r -> {
				assertThat(r.getDisplayName()).isEqualTo("newDisplayName");
				assertThat(r.getRoleType()).isEqualTo(MANAGER);
				assertThat(r.getVersion()).isEqualTo(123);
				assertThat(r.getPhases()).isEmpty();
			});
	}

	@Test
	void toChecklistsFromNull() {
		assertThat(ChecklistMapper.toChecklists(null)).isEmpty();
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
		assertThat(ChecklistMapper.toChecklistEntity(null)).isNull();
	}

	@Test
	void toPhaseEntityFromNull() {
		assertThat(ChecklistMapper.toPhaseEntity(null)).isNull();
	}

	@Test
	void toTaskEntityFromNull() {
		assertThat(ChecklistMapper.toTaskEntity(null)).isNull();
	}
}
