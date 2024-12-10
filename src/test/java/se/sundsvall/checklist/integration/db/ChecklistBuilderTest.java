package se.sundsvall.checklist.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.checklist.TestObjectFactory;
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.api.model.Task;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;

@ExtendWith(MockitoExtension.class)
class ChecklistBuilderTest {

	@Mock
	private PhaseRepository phaseRepositoryMock;

	@InjectMocks
	private ChecklistBuilder checklistBuilder;

	@Test
	void buildChecklist() {
		// Arrange
		final var phaseEntity = TestObjectFactory.createPhaseEntity();
		final var checklistEntity = TestObjectFactory.createChecklistEntity(phaseEntity);
		final var task1 = checklistEntity.getTasks().getFirst();
		final var task2 = checklistEntity.getTasks().getLast();

		when(phaseRepositoryMock.findAllByMunicipalityId(checklistEntity.getMunicipalityId())).thenReturn(List.of(phaseEntity));

		// Act
		final var checklist = checklistBuilder.buildChecklist(checklistEntity);

		// Assert
		assertChecklistValues(checklistEntity, checklist);
		assertThat(checklist.getPhases()).hasSize(1).satisfiesExactly(phase -> {
			assertPhaseValues(phase, phaseEntity);
			assertThat(phase.getTasks()).hasSize(2).satisfiesExactlyInAnyOrder(task -> {
				assertTaskValues(task, task1);
			}, task -> {
				assertTaskValues(task, task2);
			});
		});
	}

	@Test
	void buildChecklistFromNull() {
		// Act and assert
		assertThat(checklistBuilder.buildChecklist(null)).isNull();
	}

	@Test
	void buildChecklistWithNoPhases() {
		// Arrange
		final var checklistEntity = TestObjectFactory.createChecklistEntity();
		checklistEntity.setTasks(null);

		// Act
		final var checklist = checklistBuilder.buildChecklist(checklistEntity);

		// Assert
		assertChecklistValues(checklistEntity, checklist);
		assertThat(checklist.getPhases()).isNullOrEmpty();
	}

	private static void assertChecklistValues(final ChecklistEntity checklistEntity, final Checklist checklist) {
		assertThat(checklist).isNotNull();
		assertThat(checklist.getCreated()).isEqualTo(checklistEntity.getCreated());
		assertThat(checklist.getDisplayName()).isEqualTo(checklistEntity.getDisplayName());
		assertThat(checklist.getId()).isEqualTo(checklistEntity.getId());
		assertThat(checklist.getLastSavedBy()).isEqualTo(checklistEntity.getLastSavedBy());
		assertThat(checklist.getLifeCycle()).isEqualTo(checklistEntity.getLifeCycle());
		assertThat(checklist.getName()).isEqualTo(checklistEntity.getName());
		assertThat(checklist.getUpdated()).isEqualTo(checklistEntity.getUpdated());
		assertThat(checklist.getVersion()).isEqualTo(checklistEntity.getVersion());
	}

	private static void assertPhaseValues(final Phase phase, final PhaseEntity phaseEntity) {
		assertThat(phase.getBodyText()).isEqualTo(phaseEntity.getBodyText());
		assertThat(phase.getCreated()).isEqualTo(phaseEntity.getCreated());
		assertThat(phase.getId()).isEqualTo(phaseEntity.getId());
		assertThat(phase.getLastSavedBy()).isEqualTo(phaseEntity.getLastSavedBy());
		assertThat(phase.getName()).isEqualTo(phaseEntity.getName());
		assertThat(phase.getPermission()).isEqualTo(phaseEntity.getPermission());
		assertThat(phase.getSortOrder()).isEqualTo(phaseEntity.getSortOrder());
		assertThat(phase.getTimeToComplete()).isEqualTo(phaseEntity.getTimeToComplete());
		assertThat(phase.getUpdated()).isEqualTo(phaseEntity.getUpdated());
	}

	private static void assertTaskValues(final Task task, final TaskEntity taskEntity) {
		assertThat(task.getCreated()).isEqualTo(taskEntity.getCreated());
		assertThat(task.getHeading()).isEqualTo(taskEntity.getHeading());
		assertThat(task.getId()).isEqualTo(taskEntity.getId());
		assertThat(task.getLastSavedBy()).isEqualTo(taskEntity.getLastSavedBy());
		assertThat(task.getPermission()).isEqualTo(taskEntity.getPermission());
		assertThat(task.getQuestionType()).isEqualTo(taskEntity.getQuestionType());
		assertThat(task.getRoleType()).isEqualTo(taskEntity.getRoleType());
		assertThat(task.getSortOrder()).isEqualTo(taskEntity.getSortOrder());
		assertThat(task.getText()).isEqualTo(taskEntity.getText());
		assertThat(task.getUpdated()).isEqualTo(taskEntity.getUpdated());
	}
}
