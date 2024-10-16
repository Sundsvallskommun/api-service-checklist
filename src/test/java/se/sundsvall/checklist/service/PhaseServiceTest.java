package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createPhaseUpdateRequest;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

	private ChecklistEntity checklistEntity;
	private PhaseEntity phaseEntity;

	@Mock
	private PhaseRepository mockPhaseRepository;

	@Mock
	private ChecklistRepository mockChecklistRepository;

	@InjectMocks
	private PhaseService phaseService;

	@BeforeEach
	void setup() {
		this.checklistEntity = createChecklistEntity();
		this.phaseEntity = checklistEntity.getPhases().getFirst();
	}

	@Test
	void getChecklistsPhasesTest() {
		var serviceSpy = spy(phaseService);
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(anyString());

		var result = serviceSpy.getChecklistPhases(anyString());

		assertThat(result).isNotEmpty().hasSize(2);
		verify(serviceSpy).getChecklistById(any());
		verify(serviceSpy).getChecklistPhases(anyString());
		verifyNoInteractions(mockPhaseRepository);
	}

	@Test
	void getChecklistPhaseTest() {
		var serviceSpy = spy(phaseService);
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(anyString());
		doReturn(phaseEntity).when(serviceSpy).getPhaseInChecklist(any(), anyString());

		var result = serviceSpy.getChecklistPhase(anyString(), anyString());

		assertThat(result).isNotNull().isInstanceOf(Phase.class);
		verify(serviceSpy).getChecklistById(anyString());
		verify(serviceSpy).getChecklistPhase(anyString(), anyString());
		verify(serviceSpy).getChecklistPhase(anyString(), anyString());
		verifyNoInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void getChecklistPhaseChecklistNotFoundTest() {
		var serviceSpy = spy(phaseService);
		doThrow(Problem.valueOf(NOT_FOUND, "Checklist not found")).when(serviceSpy).getChecklistById(anyString());

		assertThatThrownBy(() -> serviceSpy.getChecklistPhase(anyString(), anyString()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Checklist not found");

		verify(serviceSpy).getChecklistPhase(any(), any());
		verify(serviceSpy).getChecklistById(anyString());
		verifyNoMoreInteractions(serviceSpy);
		verifyNoInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void getChecklistPhasePhaseNotFoundTest() {
		var serviceSpy = spy(phaseService);
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(checklistEntity.getId());
		doThrow(Problem.valueOf(NOT_FOUND, "Phase not found")).when(serviceSpy).getPhaseInChecklist(checklistEntity, phaseEntity.getId());

		assertThatThrownBy(() -> serviceSpy.getChecklistPhase(checklistEntity.getId(), phaseEntity.getId()))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Phase not found");

		verify(serviceSpy).getChecklistPhase(checklistEntity.getId(), phaseEntity.getId());
		verify(serviceSpy).getChecklistById(checklistEntity.getId());
		verify(serviceSpy).getPhaseInChecklist(checklistEntity, phaseEntity.getId());
		verifyNoMoreInteractions(serviceSpy);
		verifyNoInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void createChecklistPhase() {
		var serviceSpy = spy(phaseService);
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(checklistEntity.getId());

		var result = serviceSpy.createChecklistPhase(checklistEntity.getId(), createPhaseCreateRequest());

		assertThat(result).isNotNull().isInstanceOf(Phase.class);
		verify(serviceSpy).getChecklistById(anyString());
		verify(serviceSpy).createChecklistPhase(anyString(), any());
		verify(mockPhaseRepository).save(any());
		verify(mockChecklistRepository).save(any());
		verifyNoMoreInteractions(mockPhaseRepository, mockChecklistRepository);
	}

	@Test
	void updateChecklistPhaseTest() {
		var serviceSpy = spy(phaseService);
		var request = createPhaseUpdateRequest();
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(checklistEntity.getId());
		doReturn(phaseEntity).when(serviceSpy).getPhaseInChecklist(checklistEntity, phaseEntity.getId());
		when(mockPhaseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var result = serviceSpy.updateChecklistPhase(checklistEntity.getId(), phaseEntity.getId(), request);

		assertThat(result).isNotNull().satisfies(c -> {
			assertThat(c.getId()).isEqualTo(phaseEntity.getId());
			assertThat(c.getName()).isEqualTo(request.getName());
			assertThat(c.getBodyText()).isEqualTo(request.getBodyText());
			assertThat(c.getRoleType()).isEqualTo(request.getRoleType());
			assertThat(c.getSortOrder()).isEqualTo(request.getSortOrder());
			assertThat(c.getPermission()).isEqualTo(request.getPermission());
			assertThat(c.getTimeToComplete()).isEqualTo(request.getTimeToComplete());
		});
		verify(serviceSpy).getChecklistById(anyString());
		verify(serviceSpy).getPhaseInChecklist(any(), anyString());
		verify(serviceSpy).updateChecklistPhase(anyString(), anyString(), any());
		verify(mockPhaseRepository).save(any());
		verifyNoMoreInteractions(mockPhaseRepository, serviceSpy);
		verifyNoInteractions(mockChecklistRepository);
	}

	@Test
	void deleteChecklistPhaseTest() {
		var serviceSpy = spy(phaseService);
		doReturn(checklistEntity).when(serviceSpy).getChecklistById(checklistEntity.getId());
		doReturn(phaseEntity).when(serviceSpy).getPhaseInChecklist(checklistEntity, phaseEntity.getId());
		when(mockChecklistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		serviceSpy.deleteChecklistPhase(checklistEntity.getId(), phaseEntity.getId());

		assertThat(checklistEntity.getPhases()).hasSize(1);
		assertThat(checklistEntity.getPhases()).doesNotContain(phaseEntity);
		verify(serviceSpy).getChecklistById(anyString());
		verify(serviceSpy).getPhaseInChecklist(any(), anyString());
		verify(serviceSpy).deleteChecklistPhase(anyString(), anyString());
		verify(mockChecklistRepository).save(any());
		verify(mockPhaseRepository).delete(any());
		verifyNoMoreInteractions(mockChecklistRepository, mockPhaseRepository, serviceSpy);
	}

	@Test
	void getChecklistByIdTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.of(checklistEntity));

		var result = phaseService.getChecklistById(any());

		assertThat(result).isNotNull().satisfies(list -> {
			assertThat(list.getName()).isEqualTo(checklistEntity.getName());
			assertThat(list.getPhases()).hasSize(checklistEntity.getPhases().size());
			assertThat(list.getVersion()).isEqualTo(checklistEntity.getVersion());
			assertThat(list.getRoleType()).isEqualTo(checklistEntity.getRoleType());
			assertThat(list.getLifeCycle()).isEqualTo(checklistEntity.getLifeCycle());
		});
		verify(mockChecklistRepository).findById(any());
		verifyNoMoreInteractions(mockChecklistRepository);
		verifyNoInteractions(mockPhaseRepository);
	}

	@Test
	void getChecklistByIdNotFoundTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> phaseService.getChecklistById(any()))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Checklist not found");
		verify(mockChecklistRepository).findById(any());
		verifyNoMoreInteractions(mockChecklistRepository);
		verifyNoInteractions(mockPhaseRepository);
	}

	@Test
	void getPhaseInChecklistTest() {
		var phaseId = checklistEntity.getPhases().getFirst().getId();

		var result = phaseService.getPhaseInChecklist(checklistEntity, phaseId);

		assertThat(result).isEqualTo(checklistEntity.getPhases().getFirst());
	}

	@Test
	void getPhaseInChecklistNotFoundTest() {
		var phaseId = "";

		assertThatThrownBy(() -> phaseService.getPhaseInChecklist(checklistEntity, phaseId))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Phase not found");
	}

}
