package se.sundsvall.checklist.service;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String UUID = randomUUID().toString();

	@Mock
	private ChecklistRepository mockChecklistRepository;

	@Mock
	private OrganizationRepository mockOrganizationRepository;

	@Mock
	private ObjectMapper objectMapperMock;

	@Captor
	private ArgumentCaptor<ChecklistEntity> checklistEntityCaptor;

	@InjectMocks
	private ChecklistService checklistService;

	@Test
	void getChecklists() {
		when(mockChecklistRepository.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(createChecklistEntity(), createChecklistEntity()));

		final var result = checklistService.getChecklists(MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(2);
		verify(mockChecklistRepository).findAllByMunicipalityId(MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void getChecklist() {

		when(mockChecklistRepository.findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID)).thenReturn(Optional.of(createChecklistEntity()));

		final var result = checklistService.getChecklist(MUNICIPALITY_ID, UUID);

		assertThat(result).isNotNull();
		verify(mockChecklistRepository).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void getChecklistChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.getChecklist(MUNICIPALITY_ID, UUID))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void createChecklist() {
		final var body = createChecklistCreateRequest();

		when(mockChecklistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(mockOrganizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(mockOrganizationRepository.findByOrganizationNumberAndMunicipalityId(anyInt(), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(createOrganizationEntity()));

		final var result = checklistService.createChecklist(MUNICIPALITY_ID, body);

		assertThat(result).isNotNull().satisfies(checklist -> {
			assertThat(checklist.getName()).isEqualTo(body.getName());
			assertThat(checklist.getLifeCycle()).isEqualTo(CREATED);
		});

		verify(mockChecklistRepository).existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID);
		verify(mockChecklistRepository).save(any());
	}

	@Test
	void createChecklistChecklistAlreadyExists() {
		final var body = createChecklistCreateRequest();
		when(mockChecklistRepository.existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID)).thenReturn(true);

		assertThatThrownBy(() -> checklistService.createChecklist(MUNICIPALITY_ID, body))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist with name '%s' already exists in municipality %s".formatted(body.getName(), MUNICIPALITY_ID));

		verify(mockChecklistRepository).existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@ParameterizedTest
	@EnumSource(value = LifeCycle.class, names = {
		"ACTIVE", "DEPRECATED"
	}, mode = Mode.EXCLUDE)
	void deleteUnattachedChecklist(LifeCycle lifeCycle) {
		final var entity = createChecklistEntity();
		entity.setLifeCycle(lifeCycle);
		when(mockChecklistRepository.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		checklistService.deleteChecklist(MUNICIPALITY_ID, entity.getId());

		verify(mockChecklistRepository).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(mockChecklistRepository).delete(entity);
		verify(mockOrganizationRepository).findByChecklistsIdAndChecklistsMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(mockOrganizationRepository, never()).save(any());
	}

	@ParameterizedTest
	@EnumSource(value = LifeCycle.class, names = {
		"ACTIVE", "DEPRECATED"
	}, mode = Mode.EXCLUDE)
	void deleteAttachedChecklist(LifeCycle lifeCycle) {
		final var checklistEntity = createChecklistEntity();
		final var organizationEntity = createOrganizationEntity();
		organizationEntity.getChecklists().add(checklistEntity);
		checklistEntity.setLifeCycle(lifeCycle);

		when(mockChecklistRepository.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(mockOrganizationRepository.findByChecklistsIdAndChecklistsMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		checklistService.deleteChecklist(MUNICIPALITY_ID, checklistEntity.getId());

		verify(mockChecklistRepository).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockChecklistRepository).delete(checklistEntity);
		verify(mockOrganizationRepository).findByChecklistsIdAndChecklistsMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(mockOrganizationRepository).save(organizationEntity);

		assertThat(organizationEntity.getChecklists()).doesNotContain(checklistEntity);
	}

	@Test
	void deleteChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.deleteChecklist(MUNICIPALITY_ID, UUID))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@ParameterizedTest
	@EnumSource(value = LifeCycle.class, names = {
		"ACTIVE", "DEPRECATED"
	})
	void deleteChecklistWrongLifecycle(LifeCycle lifeCycle) {
		final var entity = createChecklistEntity();
		entity.setLifeCycle(lifeCycle);
		when(mockChecklistRepository.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		assertThatThrownBy(() -> checklistService.deleteChecklist(MUNICIPALITY_ID, entity.getId()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Cannot delete checklist with lifecycle %s".formatted(lifeCycle));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void createDeepCopy() throws Exception {
		final var entity = createChecklistEntity();
		final var json = "{}";

		when(objectMapperMock.writeValueAsString(any())).thenReturn(json);
		when(objectMapperMock.readValue(json, ChecklistEntity.class)).thenReturn(entity);

		checklistService.createDeepCopy(entity);

		verify(objectMapperMock).writeValueAsString(entity);
		verify(objectMapperMock).readValue(json, ChecklistEntity.class);
	}

	@Test
	void updateChecklist() {
		final var entity = createChecklistEntity();
		entity.setLifeCycle(ACTIVE);
		final var checklist = createChecklistUpdateRequest();
		when(mockChecklistRepository.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(mockChecklistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var result = checklistService.updateChecklist(MUNICIPALITY_ID, entity.getId(), checklist);

		assertThat(result).isNotNull().satisfies(checklistEntity -> {
			assertThat(checklistEntity.getLifeCycle()).isEqualTo(ACTIVE);
			assertThat(checklistEntity.getDisplayName()).isEqualTo(checklist.getDisplayName());
		});

		verify(mockChecklistRepository).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(mockChecklistRepository).save(any(ChecklistEntity.class));
	}

	@Test
	void updateChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.updateChecklist(MUNICIPALITY_ID, UUID, createChecklistUpdateRequest()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(mockChecklistRepository).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void createNewVersion() throws Exception {
		final var json = "{}";
		final var entity = createChecklistEntity();
		entity.setId(UUID);
		entity.setLifeCycle(ACTIVE);

		when(mockChecklistRepository.findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(objectMapperMock.writeValueAsString(any())).thenReturn(json);
		when(objectMapperMock.readValue(json, ChecklistEntity.class)).thenReturn(entity);
		when(mockChecklistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var result = checklistService.createNewVersion(MUNICIPALITY_ID, UUID);

		verify(mockChecklistRepository).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verify(mockChecklistRepository).existsByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, CREATED);
		verify(objectMapperMock).writeValueAsString(entity);
		verify(objectMapperMock).readValue(json, ChecklistEntity.class);
		verify(mockChecklistRepository).save(checklistEntityCaptor.capture());

		assertThat(checklistEntityCaptor.getValue()).satisfies(entityToSave -> {
			assertThat(entityToSave.getId()).isNull();
			assertThat(entityToSave.getLifeCycle()).isEqualTo(CREATED);
			assertThat(entityToSave.getVersion()).isEqualTo(2);
		});
		assertThat(result).isNotNull().satisfies(checklistEntity -> {
			assertThat(checklistEntity.getLifeCycle()).isEqualTo(CREATED);
			assertThat(checklistEntity.getVersion()).isEqualTo(2);
		});
	}

	@Test
	void createNewVersionNotFound() {
		assertThatThrownBy(() -> checklistService.createNewVersion(MUNICIPALITY_ID, UUID))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void createNewVersionBadRequest() {
		final var entity = createChecklistEntity();

		when(mockChecklistRepository.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(mockChecklistRepository.existsByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, CREATED)).thenReturn(true);

		assertThatThrownBy(() -> checklistService.createNewVersion(MUNICIPALITY_ID, entity.getId()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist already has a draft version in progress preventing another draft version from being created");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(mockChecklistRepository).existsByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, CREATED);
		verifyNoMoreInteractions(mockChecklistRepository);
	}

	@Test
	void activateChecklist() {
		final var entity = createChecklistEntity();
		final var oldVersion = createChecklistEntity();
		when(mockChecklistRepository.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(mockChecklistRepository.findByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, ACTIVE)).thenReturn(Optional.of(oldVersion));
		when(mockChecklistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var result = checklistService.activateChecklist(MUNICIPALITY_ID, entity.getId());

		assertThat(result).isNotNull().satisfies(checklistEntity -> {
			assertThat(checklistEntity.getLifeCycle()).isEqualTo(ACTIVE);
		});
		assertThat(oldVersion.getLifeCycle()).isEqualTo(DEPRECATED);
		verify(mockChecklistRepository).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(mockChecklistRepository).findByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, ACTIVE);
		verify(mockChecklistRepository).save(any(ChecklistEntity.class));
	}

	@Test
	void activateChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.activateChecklist(MUNICIPALITY_ID, UUID))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found");

		verify(mockChecklistRepository).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockChecklistRepository);
	}

}
