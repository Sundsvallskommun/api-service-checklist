package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistUpdateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createOrganizationEntity;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.ACTIVE;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.CREATED;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.DEPRECATED;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

	@Mock
	private ChecklistRepository mockChecklistRepository;

	@Mock
	private OrganizationRepository mockOrganizationRepository;

	@InjectMocks
	private ChecklistService checklistService;

	@Test
	void getAllChecklistsTest() {
		when(mockChecklistRepository.findAll()).thenReturn(List.of(createChecklistEntity(), createChecklistEntity()));

		var result = checklistService.getAllChecklists();

		assertThat(result).isNotNull().hasSize(2);
		verify(mockChecklistRepository).findAll();
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void getChecklistById_WhenFoundTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.of(createChecklistEntity()));

		var result = checklistService.getChecklistById(any());

		assertThat(result).isNotNull();
		verify(mockChecklistRepository).findById(any());
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void getChecklistById_NotFoundTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> checklistService.getChecklistById(any()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found");

		verify(mockChecklistRepository).findById(any());
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void createChecklistTest() {
		var requestBody = createChecklistCreateRequest();

		when(mockChecklistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(mockOrganizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(mockOrganizationRepository.findByOrganizationNumber(anyInt())).thenReturn(Optional.of(createOrganizationEntity()));

		var result = checklistService.createChecklist(requestBody);

		assertThat(result).isNotNull().satisfies(checklist -> {
			assertThat(checklist.getName()).isEqualTo(requestBody.getName());
			assertThat(checklist.getRoleType()).isEqualTo(requestBody.getRoleType());
			assertThat(checklist.getLifeCycle()).isEqualTo(CREATED);
		});

		verify(mockChecklistRepository).existsByName(any());
		verify(mockChecklistRepository).save(any());
	}

	@Test
	void createChecklistAlreadyExistsTest() {
		var requestBody = createChecklistCreateRequest();
		when(mockChecklistRepository.existsByName(requestBody.getName())).thenReturn(true);

		assertThatThrownBy(() -> checklistService.createChecklist(requestBody))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist with name: Test checklist template already exists");

		verify(mockChecklistRepository).existsByName(any());
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void deleteUnattachedChecklistTest() {
		var entity = createChecklistEntity();
		when(mockChecklistRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

		checklistService.deleteChecklist(entity.getId());

		verify(mockChecklistRepository).findById(any());
		verify(mockChecklistRepository).deleteById(entity.getId());
		verify(mockOrganizationRepository).findByChecklistsId(entity.getId());
		verify(mockOrganizationRepository, never()).save(any());
	}

	@Test
	void deleteAttachedChecklistTest() {
		final var checklistEntity = createChecklistEntity();
		final var organizationEntity = createOrganizationEntity();
		organizationEntity.getChecklists().add(checklistEntity);

		when(mockChecklistRepository.findById(checklistEntity.getId())).thenReturn(Optional.of(checklistEntity));
		when(mockOrganizationRepository.findByChecklistsId(checklistEntity.getId())).thenReturn(Optional.of(organizationEntity));

		checklistService.deleteChecklist(checklistEntity.getId());

		verify(mockChecklistRepository).findById(checklistEntity.getId());
		verify(mockChecklistRepository).deleteById(checklistEntity.getId());
		verify(mockOrganizationRepository).findByChecklistsId(checklistEntity.getId());
		verify(mockOrganizationRepository).save(organizationEntity);

		assertThat(organizationEntity.getChecklists()).doesNotContain(checklistEntity);
	}

	@Test
	void deleteChecklistNotFoundTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> checklistService.deleteChecklist(UUID.randomUUID().toString()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found");

		verify(mockChecklistRepository).findById(any());
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void deleteChecklistWrongLifecycleTest() {
		var entity = createChecklistEntity();
		entity.setLifeCycle(ACTIVE);
		when(mockChecklistRepository.findById(entity.getId())).thenReturn(Optional.of(entity));

		assertThatThrownBy(() -> checklistService.deleteChecklist(entity.getId()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Cannot delete checklist with lifecycle: ACTIVE");

		verify(mockChecklistRepository).findById(any());
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void createDeepCopyTest() {
		var entity = createChecklistEntity();

		var result = checklistService.createDeepCopy(entity);

		assertThat(result).isNotEqualTo(entity).satisfies(checklistEntity -> {
			assertThat(checklistEntity.getName()).isEqualTo(entity.getName());
			assertThat(checklistEntity.getVersion()).isEqualTo(entity.getVersion());
			assertThat(checklistEntity.getRoleType()).isEqualTo(entity.getRoleType());
			assertThat(checklistEntity.getLifeCycle()).isEqualTo(entity.getLifeCycle());
		});
	}

	@Test
	void updateChecklistTest() {
		var entity = createChecklistEntity();
		entity.setLifeCycle(ACTIVE);
		var checklist = createChecklistUpdateRequest();
		when(mockChecklistRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
		when(mockChecklistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var result = checklistService.updateChecklist(entity.getId(), checklist);

		assertThat(result).isNotNull().satisfies(checklistEntity -> {
			assertThat(checklistEntity.getLifeCycle()).isEqualTo(ACTIVE);
			assertThat(checklistEntity.getRoleType()).isEqualTo(checklist.getRoleType());
		});

		verify(mockChecklistRepository).findById(entity.getId());
		verify(mockChecklistRepository).save(any(ChecklistEntity.class));
	}

	@Test
	void createNewVersionTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.of(createChecklistEntity()));
		when(mockChecklistRepository.existsByNameAndLifeCycle(any(), any())).thenReturn(false);
		when(mockChecklistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var result = checklistService.createNewVersion(any());

		assertThat(result).isNotNull().satisfies(checklistEntity -> {
			assertThat(checklistEntity.getLifeCycle()).isEqualTo(CREATED);
			assertThat(checklistEntity.getVersion()).isEqualTo(2);
		});
		verify(mockChecklistRepository).findById(any());
		verify(mockChecklistRepository).existsByNameAndLifeCycle(any(), any());
		verify(mockChecklistRepository).save(any(ChecklistEntity.class));
	}

	@Test
	void createNewVersionNotFoundTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> checklistService.createNewVersion(any()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found");

		verify(mockChecklistRepository).findById(any());
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void createNewVersionBadRequestTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.of(createChecklistEntity()));
		when(mockChecklistRepository.existsByNameAndLifeCycle(any(), any())).thenReturn(true);

		assertThatThrownBy(() -> checklistService.createNewVersion(any()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist already has a draft version in progress preventing another draft version from being created");

		verify(mockChecklistRepository).findById(any());
		verify(mockChecklistRepository).existsByNameAndLifeCycle(any(), any());
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void activateChecklistTest() {
		var entity = createChecklistEntity();
		var oldVersion = createChecklistEntity();
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.of(entity));
		when(mockChecklistRepository.findByNameAndLifeCycle(entity.getName(), ACTIVE)).thenReturn(Optional.of(oldVersion));
		when(mockChecklistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var result = checklistService.activateChecklist(any());

		assertThat(result).isNotNull().satisfies(checklistEntity -> {
			assertThat(checklistEntity.getLifeCycle()).isEqualTo(ACTIVE);
		});
		assertThat(oldVersion.getLifeCycle()).isEqualTo(DEPRECATED);
		verify(mockChecklistRepository).findById(any());
		verify(mockChecklistRepository).findByNameAndLifeCycle(entity.getName(), ACTIVE);
		verify(mockChecklistRepository).save(any(ChecklistEntity.class));
	}

	@Test
	void activateChecklistNotFoundTest() {
		when(mockChecklistRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> checklistService.activateChecklist(any()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found");

		verify(mockChecklistRepository).findById(any());
		verifyNoMoreInteractions(mockChecklistRepository);
	}

}
