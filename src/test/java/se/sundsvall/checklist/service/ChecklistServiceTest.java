package se.sundsvall.checklist.service;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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

import org.junit.jupiter.api.AfterEach;
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

import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.integration.db.ChecklistBuilder;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String UUID = randomUUID().toString();

	@Mock
	private ChecklistRepository checklistRepositoryMock;

	@Mock
	private OrganizationRepository organizationRepositoryMock;

	@Mock
	private ObjectMapper objectMapperMock;

	@Mock
	private ChecklistBuilder checklistBuilderMock;

	@Mock
	private SortorderService sortorderServiceMock;

	@Captor
	private ArgumentCaptor<ChecklistEntity> checklistEntityCaptor;

	@Captor
	private ArgumentCaptor<String> sortorderItemIdCaptor;

	@InjectMocks
	private ChecklistService checklistService;

	@Test
	void getChecklists() {
		final var checklist1 = createChecklistEntity();
		final var checklist2 = createChecklistEntity();
		when(checklistRepositoryMock.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(checklist1, checklist2));
		when(checklistBuilderMock.buildChecklist(checklist1)).thenReturn(Checklist.builder().withId(checklist1.getId()).build());
		when(checklistBuilderMock.buildChecklist(checklist2)).thenReturn(Checklist.builder().withId(checklist2.getId()).build());

		final var result = checklistService.getChecklists(MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(2).extracting(Checklist::getId).containsExactlyInAnyOrder(checklist1.getId(), checklist2.getId());

		verify(checklistRepositoryMock).findAllByMunicipalityId(MUNICIPALITY_ID);
		verify(checklistBuilderMock).buildChecklist(checklist1);
		verify(checklistBuilderMock).buildChecklist(checklist2);
	}

	@Test
	void getChecklist() {
		final var checklist = createChecklistEntity();
		when(checklistRepositoryMock.findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID)).thenReturn(Optional.of(checklist));
		when(checklistBuilderMock.buildChecklist(checklist)).thenReturn(Checklist.builder().withId(checklist.getId()).build());

		final var result = checklistService.getChecklist(MUNICIPALITY_ID, UUID);

		assertThat(result).isNotNull();

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verify(checklistBuilderMock).buildChecklist(checklist);
	}

	@Test
	void getChecklistChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.getChecklist(MUNICIPALITY_ID, UUID))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found");

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
	}

	@Test
	void createChecklist() {
		final var body = createChecklistCreateRequest();

		when(checklistRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(organizationRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(anyInt(), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(createOrganizationEntity()));

		final var result = checklistService.createChecklist(MUNICIPALITY_ID, body);

		assertThat(result).isNotNull().satisfies(checklist -> {
			assertThat(checklist.getName()).isEqualTo(body.getName());
			assertThat(checklist.getLifeCycle()).isEqualTo(CREATED);
		});

		verify(checklistRepositoryMock).existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).save(any());
	}

	@Test
	void createChecklistChecklistAlreadyExists() {
		final var body = createChecklistCreateRequest();
		when(checklistRepositoryMock.existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID)).thenReturn(true);

		assertThatThrownBy(() -> checklistService.createChecklist(MUNICIPALITY_ID, body))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist with name '%s' already exists in municipality %s".formatted(body.getName(), MUNICIPALITY_ID));

		verify(checklistRepositoryMock).existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID);
	}

	@ParameterizedTest
	@EnumSource(value = LifeCycle.class, names = {
		"ACTIVE", "DEPRECATED"
	}, mode = Mode.EXCLUDE)
	void deleteUnattachedChecklist(LifeCycle lifeCycle) {
		final var entity = createChecklistEntity();
		entity.setLifeCycle(lifeCycle);
		when(checklistRepositoryMock.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		checklistService.deleteChecklist(MUNICIPALITY_ID, entity.getId());

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).delete(entity);
		verify(organizationRepositoryMock).findByChecklistsIdAndChecklistsMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(sortorderServiceMock, times(3)).deleteSortorderItem(sortorderItemIdCaptor.capture());

		assertThat(sortorderItemIdCaptor.getAllValues()).satisfiesExactlyInAnyOrder(
			id -> assertThat(entity.getTasks().getFirst().getId()).isEqualTo(id),
			id -> assertThat(entity.getTasks().getLast().getId()).isEqualTo(id),
			id -> assertThat(entity.getTasks().getFirst().getPhase().getId()).isEqualTo(id));

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

		when(checklistRepositoryMock.findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(organizationRepositoryMock.findByChecklistsIdAndChecklistsMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		checklistService.deleteChecklist(MUNICIPALITY_ID, checklistEntity.getId());

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).delete(checklistEntity);
		verify(organizationRepositoryMock).findByChecklistsIdAndChecklistsMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(organizationRepositoryMock).save(organizationEntity);
		verify(sortorderServiceMock, times(3)).deleteSortorderItem(sortorderItemIdCaptor.capture());

		assertThat(organizationEntity.getChecklists()).doesNotContain(checklistEntity);
		assertThat(sortorderItemIdCaptor.getAllValues()).satisfiesExactlyInAnyOrder(
			id -> assertThat(checklistEntity.getTasks().getFirst().getId()).isEqualTo(id),
			id -> assertThat(checklistEntity.getTasks().getLast().getId()).isEqualTo(id),
			id -> assertThat(checklistEntity.getTasks().getFirst().getPhase().getId()).isEqualTo(id));
	}

	@Test
	void deleteChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.deleteChecklist(MUNICIPALITY_ID, UUID))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
	}

	@ParameterizedTest
	@EnumSource(value = LifeCycle.class, names = {
		"ACTIVE", "DEPRECATED"
	})
	void deleteChecklistWrongLifecycle(LifeCycle lifeCycle) {
		final var entity = createChecklistEntity();
		entity.setLifeCycle(lifeCycle);
		when(checklistRepositoryMock.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		assertThatThrownBy(() -> checklistService.deleteChecklist(MUNICIPALITY_ID, entity.getId()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Cannot delete checklist with lifecycle %s".formatted(lifeCycle));

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
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

		final var checklist = createChecklistUpdateRequest();
		when(checklistRepositoryMock.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(checklistRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(checklistBuilderMock.buildChecklist(entity)).thenReturn(Checklist.builder().withId(entity.getId()).build());

		final var result = checklistService.updateChecklist(MUNICIPALITY_ID, entity.getId(), checklist);

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).save(checklistEntityCaptor.capture());
		verify(checklistBuilderMock).buildChecklist(entity);

		assertThat(checklistEntityCaptor.getValue()).satisfies(e -> {
			assertThat(e.getDisplayName()).isEqualTo(checklist.getDisplayName());
			assertThat(e.getLastSavedBy()).isEqualTo(checklist.getUpdatedBy());
		});

		assertThat(result).isNotNull().satisfies(c -> {
			assertThat(c.getId()).isEqualTo(entity.getId());
		});
	}

	@Test
	void updateChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.updateChecklist(MUNICIPALITY_ID, UUID, createChecklistUpdateRequest()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
	}

	@Test
	void createNewVersion() throws Exception {
		final var json = "{}";
		final var entity = createChecklistEntity();
		entity.setId(UUID);
		entity.setLifeCycle(ACTIVE);

		when(checklistRepositoryMock.findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(objectMapperMock.writeValueAsString(any())).thenReturn(json);
		when(objectMapperMock.readValue(json, ChecklistEntity.class)).thenReturn(entity);
		when(checklistRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var result = checklistService.createNewVersion(MUNICIPALITY_ID, UUID);

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verify(checklistRepositoryMock).existsByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, CREATED);
		verify(objectMapperMock).writeValueAsString(entity);
		verify(objectMapperMock).readValue(json, ChecklistEntity.class);
		verify(checklistRepositoryMock).save(checklistEntityCaptor.capture());

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

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
	}

	@Test
	void createNewVersionBadRequest() {
		final var entity = createChecklistEntity();

		when(checklistRepositoryMock.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(checklistRepositoryMock.existsByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, CREATED)).thenReturn(true);

		assertThatThrownBy(() -> checklistService.createNewVersion(MUNICIPALITY_ID, entity.getId()))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist already has a draft version in progress preventing another draft version from being created");

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).existsByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, CREATED);
	}

	@Test
	void activateChecklist() {
		final var entity = createChecklistEntity();
		final var oldVersion = createChecklistEntity();
		when(checklistRepositoryMock.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(checklistRepositoryMock.findByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, ACTIVE)).thenReturn(Optional.of(oldVersion));
		when(checklistRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var result = checklistService.activateChecklist(MUNICIPALITY_ID, entity.getId());

		assertThat(result).isNotNull().satisfies(checklistEntity -> {
			assertThat(checklistEntity.getLifeCycle()).isEqualTo(ACTIVE);
		});
		assertThat(oldVersion.getLifeCycle()).isEqualTo(DEPRECATED);
		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).findByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, ACTIVE);
		verify(checklistRepositoryMock).save(any(ChecklistEntity.class));
	}

	@Test
	void activateChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.activateChecklist(MUNICIPALITY_ID, UUID))
			.isInstanceOfAny(Problem.class)
			.hasMessageContaining("Checklist not found");

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
	}

	@AfterEach
	void verifyNoMoreInteraction() {
		verifyNoMoreInteractions(checklistRepositoryMock, organizationRepositoryMock, objectMapperMock, checklistBuilderMock, sortorderServiceMock);
	}
}
