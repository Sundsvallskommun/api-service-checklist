package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseEntity;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseUpdateRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.SortorderEntity;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;
import se.sundsvall.checklist.integration.db.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class PhaseServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";

	@Mock
	private PhaseRepository mockPhaseRepository;

	@Mock
	private TaskRepository mockTaskRepository;

	@Mock
	private CustomTaskRepository mockCustomTaskRepository;

	@Mock
	private SortorderService mockSortorderService;

	@InjectMocks
	private PhaseService service;

	@Captor
	private ArgumentCaptor<PhaseEntity> phaseEntityCaptor;

	@Test
	void getPhases() {
		when(mockPhaseRepository.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(createPhaseEntity(), createPhaseEntity()));

		final var result = service.getPhases(MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(2);

		verify(mockPhaseRepository).findAllByMunicipalityId(MUNICIPALITY_ID);
	}

	@Test
	void getPhasesWhenNoPhasesExists() {
		assertThat(service.getPhases(MUNICIPALITY_ID)).isNullOrEmpty();

		verify(mockPhaseRepository).findAllByMunicipalityId(MUNICIPALITY_ID);
	}

	@Test
	void getPhase() {
		final var phaseEntity = createPhaseEntity();
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));

		final var result = service.getPhase(MUNICIPALITY_ID, phaseEntity.getId());

		assertThat(result).isNotNull().isInstanceOf(Phase.class);

		verify(mockPhaseRepository).findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
	}

	@Test
	void getPhasePhaseNotFound() {
		final var randomId = UUID.randomUUID().toString();

		assertThatThrownBy(() -> service.getPhase(MUNICIPALITY_ID, randomId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockPhaseRepository).findByIdAndMunicipalityId(randomId, MUNICIPALITY_ID);
	}

	@Test
	void createPhase() {
		final var request = createPhaseCreateRequest();

		final var result = service.createPhase(MUNICIPALITY_ID, request);

		verify(mockPhaseRepository).save(phaseEntityCaptor.capture());

		assertThat(result).isNotNull().isInstanceOf(Phase.class);
		assertThat(phaseEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getBodyText()).isEqualTo(request.getBodyText());
			assertThat(entity.getCreated()).isNull();
			assertThat(entity.getId()).isNull();
			assertThat(entity.getName()).isEqualTo(request.getName());
			assertThat(entity.getPermission()).isEqualTo(request.getPermission());
			assertThat(entity.getSortOrder()).isEqualTo(request.getSortOrder());
			assertThat(entity.getTimeToComplete()).isEqualTo(request.getTimeToComplete());
			assertThat(entity.getUpdated()).isNull();
			assertThat(entity.getLastSavedBy()).isEqualTo(request.getCreatedBy());
		});
	}

	@Test
	void updatePhase() {
		final var phaseEntity = createPhaseEntity();
		final var request = createPhaseUpdateRequest();

		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));
		when(mockPhaseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var result = service.updatePhase(MUNICIPALITY_ID, phaseEntity.getId(), request);

		verify(mockPhaseRepository).save(phaseEntityCaptor.capture());

		assertThat(result).isNotNull().isInstanceOf(Phase.class);
		assertThat(phaseEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getId()).isEqualTo(phaseEntity.getId());
			assertThat(entity.getName()).isEqualTo(request.getName());
			assertThat(entity.getBodyText()).isEqualTo(request.getBodyText());
			assertThat(entity.getSortOrder()).isEqualTo(request.getSortOrder());
			assertThat(entity.getPermission()).isEqualTo(request.getPermission());
			assertThat(entity.getTimeToComplete()).isEqualTo(request.getTimeToComplete());
			assertThat(entity.getLastSavedBy()).isEqualTo(request.getUpdatedBy());
		});
	}

	@Test
	void updatePhasePhaseNotFound() {
		final var randomId = UUID.randomUUID().toString();

		assertThatThrownBy(() -> service.updatePhase(MUNICIPALITY_ID, randomId, createPhaseUpdateRequest()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockPhaseRepository).findByIdAndMunicipalityId(randomId, MUNICIPALITY_ID);
	}

	@Test
	void deletePhase() {
		final var phaseEntity = createPhaseEntity();
		List.of(SortorderEntity.builder().build(), SortorderEntity.builder().build());
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));

		service.deletePhase(MUNICIPALITY_ID, phaseEntity.getId());

		verify(mockPhaseRepository).findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(mockTaskRepository).countByPhaseId(phaseEntity.getId());
		verify(mockCustomTaskRepository).countByPhaseId(phaseEntity.getId());
		verify(mockSortorderService).deleteSortorderItem(phaseEntity.getId());
		verify(mockPhaseRepository).delete(phaseEntityCaptor.capture());

		assertThat(phaseEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getId()).isEqualTo(phaseEntity.getId());
		});
	}

	@Test
	void deletePhasePhaseNotFound() {
		final var randomId = UUID.randomUUID().toString();

		assertThatThrownBy(() -> service.deletePhase(MUNICIPALITY_ID, randomId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockPhaseRepository).findByIdAndMunicipalityId(randomId, MUNICIPALITY_ID);
	}

	@Test
	void deletePhaseWhenPhaseConnectedToTasks() {
		final var phaseEntity = createPhaseEntity();
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));
		when(mockTaskRepository.countByPhaseId(phaseEntity.getId())).thenReturn(1);

		assertThatThrownBy(() -> service.deletePhase(MUNICIPALITY_ID, phaseEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Conflict: Phase can not be deleted as it has tasks connected to it");

		verify(mockPhaseRepository).findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(mockTaskRepository).countByPhaseId(phaseEntity.getId());
	}

	@Test
	void deletePhaseWhenPhaseConnectedToCustomTasks() {
		final var phaseEntity = createPhaseEntity();
		when(mockPhaseRepository.findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(phaseEntity));
		when(mockCustomTaskRepository.countByPhaseId(phaseEntity.getId())).thenReturn(1);

		assertThatThrownBy(() -> service.deletePhase(MUNICIPALITY_ID, phaseEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Conflict: Phase can not be deleted as it has tasks connected to it");

		verify(mockPhaseRepository).findByIdAndMunicipalityId(phaseEntity.getId(), MUNICIPALITY_ID);
		verify(mockTaskRepository).countByPhaseId(phaseEntity.getId());
		verify(mockCustomTaskRepository).countByPhaseId(phaseEntity.getId());
	}

	@AfterEach
	void verifyNoMoreInteraction() {
		verifyNoMoreInteractions(mockPhaseRepository, mockTaskRepository, mockCustomTaskRepository, mockSortorderService);
	}
}
