package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseUpdateRequest;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;

@ExtendWith(MockitoExtension.class)
class PhaseServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";

	private ChecklistEntity checklistEntity;
	private PhaseEntity phaseEntity;

	@Mock
	private PhaseRepository mockPhaseRepository;

	@Mock
	private ChecklistRepository mockChecklistRepository;

	@InjectMocks
	private PhaseService service;

	@Captor
	private ArgumentCaptor<ChecklistEntity> checklistEntityCaptor;

	@Captor
	private ArgumentCaptor<PhaseEntity> phaseEntityCaptor;

	@BeforeEach
	void setup() {
		checklistEntity = createChecklistEntity();
		phaseEntity = checklistEntity.getPhases().getFirst();
	}

	@Test
	void getChecklistsPhases() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		final var result = service.getPhases(MUNICIPALITY_ID, checklistEntity.getId());

		assertThat(result).isNotEmpty().hasSize(2);
		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void getChecklistPhasesChecklistNotFound() {
		assertThatThrownBy(() -> service.getPhases(MUNICIPALITY_ID, checklistEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void getChecklistPhase() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		final var result = service.getPhase(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId());

		assertThat(result).isNotNull().isInstanceOf(Phase.class);
		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void getChecklistPhasePhaseNotFound() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		assertThatThrownBy(() -> service.getPhase(MUNICIPALITY_ID, checklistEntity.getId(), UUID.randomUUID().toString()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found in checklist");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void createPhase() {
		final var request = createPhaseCreateRequest();
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		final var result = service.createPhase(MUNICIPALITY_ID, checklistEntity.getId(), request);

		verify(mockPhaseRepository).save(phaseEntityCaptor.capture());
		verify(mockChecklistRepository).save(checklistEntityCaptor.capture());
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
		assertThat(result).isNotNull().isInstanceOf(Phase.class);
		assertThat(phaseEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getBodyText()).isEqualTo(request.getBodyText());
			assertThat(entity.getCreated()).isNull();
			assertThat(entity.getId()).isNull();
			assertThat(entity.getName()).isEqualTo(request.getName());
			assertThat(entity.getPermission()).isEqualTo(request.getPermission());
			assertThat(entity.getSortOrder()).isEqualTo(request.getSortOrder());
			assertThat(entity.getTasks()).isNullOrEmpty();
			assertThat(entity.getTimeToComplete()).isEqualTo(request.getTimeToComplete());
			assertThat(entity.getUpdated()).isNull();
		});
		assertThat(checklistEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getPhases()).contains(phaseEntityCaptor.getValue());
		});
	}

	@Test
	void createPhaseChecklistNotFound() {
		assertThatThrownBy(() -> service.createPhase(MUNICIPALITY_ID, checklistEntity.getId(), createPhaseCreateRequest()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void updatePhase() {
		final var request = createPhaseUpdateRequest();

		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockPhaseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var result = service.updatePhase(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), request);

		verify(mockPhaseRepository).save(phaseEntityCaptor.capture());
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
		assertThat(result).isNotNull().isInstanceOf(Phase.class);
		assertThat(phaseEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getId()).isEqualTo(phaseEntity.getId());
			assertThat(entity.getName()).isEqualTo(request.getName());
			assertThat(entity.getBodyText()).isEqualTo(request.getBodyText());
			assertThat(entity.getSortOrder()).isEqualTo(request.getSortOrder());
			assertThat(entity.getPermission()).isEqualTo(request.getPermission());
			assertThat(entity.getTimeToComplete()).isEqualTo(request.getTimeToComplete());
		});
	}

	@Test
	void updatePhaseChecklistNotFound() {
		assertThatThrownBy(() -> service.updatePhase(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId(), createPhaseUpdateRequest()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void updateChecklistPhasePhaseNotFound() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		assertThatThrownBy(() -> service.updatePhase(MUNICIPALITY_ID, checklistEntity.getId(), UUID.randomUUID().toString(), createPhaseUpdateRequest()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found in checklist");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void deletePhase() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		service.deletePhase(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId());

		verify(mockPhaseRepository).delete(phaseEntityCaptor.capture());
		verify(mockChecklistRepository).save(checklistEntityCaptor.capture());
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
		assertThat(phaseEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getId()).isEqualTo(phaseEntity.getId());
		});
		assertThat(checklistEntityCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getPhases()).isNotEmpty().doesNotContain(phaseEntityCaptor.getValue());
		});
	}

	@Test
	void deletePhaseChecklistNotFound() {
		assertThatThrownBy(() -> service.deletePhase(MUNICIPALITY_ID, checklistEntity.getId(), phaseEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void deletePhasePhaseNotFound() {
		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));

		assertThatThrownBy(() -> service.deletePhase(MUNICIPALITY_ID, checklistEntity.getId(), UUID.randomUUID().toString()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found in checklist");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
	}
}
