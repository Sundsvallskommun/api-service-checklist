package se.sundsvall.checklist.service;

import static generated.se.sundsvall.eventlog.EventType.DELETE;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import static se.sundsvall.checklist.service.EventService.CHECKLIST_CREATED;
import static se.sundsvall.checklist.service.EventService.CHECKLIST_DELETED;
import static se.sundsvall.checklist.service.EventService.CHECKLIST_UPDATED;

import generated.se.sundsvall.eventlog.EventType;
import java.util.List;
import java.util.Map;
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
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.integration.db.ChecklistBuilder;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.service.util.ChecklistUtils;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

	private static final String USER = "Chuck Norris";
	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String UUID = randomUUID().toString();

	@Mock
	private ChecklistRepository checklistRepositoryMock;

	@Mock
	private OrganizationRepository organizationRepositoryMock;

	@Mock
	private ChecklistUtils checklistUtilsMock;

	@Mock
	private ChecklistBuilder checklistBuilderMock;

	@Mock
	private SortorderService sortorderServiceMock;

	@Mock
	private EventService eventServiceMock;

	@Captor
	private ArgumentCaptor<ChecklistEntity> checklistEntityCaptor;

	@Captor
	private ArgumentCaptor<String> sortorderItemIdCaptor;

	@Captor
	private ArgumentCaptor<Map<String, String>> sortorderItemsCaptor;

	@InjectMocks
	private ChecklistService checklistService;

	@AfterEach
	void verifyNoMoreInteraction() {
		verifyNoMoreInteractions(checklistRepositoryMock, organizationRepositoryMock, checklistUtilsMock, checklistBuilderMock, sortorderServiceMock, eventServiceMock);
	}

	@Test
	void getChecklists() {
		final var checklist1 = createChecklistEntity();
		final var checklist2 = createChecklistEntity();
		when(checklistRepositoryMock.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(checklist1, checklist2));
		when(checklistBuilderMock.buildChecklist(checklist1)).thenReturn(Checklist.builder().withId(checklist1.getId()).build());
		when(checklistBuilderMock.buildChecklist(checklist2)).thenReturn(Checklist.builder().withId(checklist2.getId()).build());
		when(sortorderServiceMock.applySortingToChecklist(any(), any(), any(Checklist.class))).thenAnswer(inv -> inv.getArgument(2));

		final var result = checklistService.getChecklists(MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(2).extracting(Checklist::getId).containsExactlyInAnyOrder(checklist1.getId(), checklist2.getId());

		verify(checklistRepositoryMock).findAllByMunicipalityId(MUNICIPALITY_ID);
		verify(checklistBuilderMock).buildChecklist(checklist1);
		verify(checklistBuilderMock).buildChecklist(checklist2);
		verify(sortorderServiceMock, times(2)).applySortingToChecklist(eq(MUNICIPALITY_ID), eq(checklist1.getOrganization().getOrganizationNumber()), any(Checklist.class));
	}

	@Test
	void getChecklist() {
		final var checklist = createChecklistEntity();
		when(checklistRepositoryMock.findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID)).thenReturn(Optional.of(checklist));
		when(checklistBuilderMock.buildChecklist(checklist)).thenReturn(Checklist.builder().withId(checklist.getId()).build());
		when(sortorderServiceMock.applySortingToChecklist(any(), any(), any(Checklist.class))).thenAnswer(inv -> inv.getArgument(2));

		final var result = checklistService.getChecklist(MUNICIPALITY_ID, UUID);

		assertThat(result).isNotNull();

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verify(checklistBuilderMock).buildChecklist(checklist);
		verify(sortorderServiceMock).applySortingToChecklist(eq(MUNICIPALITY_ID), eq(checklist.getOrganization().getOrganizationNumber()), any(Checklist.class));
	}

	@Test
	void getChecklistChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.getChecklist(MUNICIPALITY_ID, UUID))
			.isInstanceOfAny(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
	}

	@Test
	void createChecklist() {
		final var body = createChecklistCreateRequest();
		final var checklistEntity = createChecklistEntity();
		final var organizationEntity = createOrganizationEntity();
		when(checklistRepositoryMock.save(any())).thenReturn(checklistEntity);
		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(body.getOrganizationNumber(), MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));
		when(organizationRepositoryMock.save(organizationEntity)).thenAnswer(invocation -> invocation.getArgument(0));
		doNothing().when(eventServiceMock).createChecklistEvent(EventType.CREATE, CHECKLIST_CREATED.formatted(checklistEntity.getDisplayName()), checklistEntity, body.getCreatedBy());

		final var result = checklistService.createChecklist(MUNICIPALITY_ID, body);

		assertThat(result).isNotNull().satisfies(checklist -> {
			assertThat(checklist.getName()).isEqualTo(body.getName());
			assertThat(checklist.getLifeCycle()).isEqualTo(CREATED);
		});

		verify(checklistRepositoryMock).existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).save(any());
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(body.getOrganizationNumber(), MUNICIPALITY_ID);
		verify(organizationRepositoryMock).save(organizationEntity);
		verify(eventServiceMock).createChecklistEvent(EventType.CREATE, CHECKLIST_CREATED.formatted(checklistEntity.getDisplayName()), checklistEntity, body.getCreatedBy());
	}

	@Test
	void createChecklistOrganizationNotFound() {
		final var body = createChecklistCreateRequest();

		assertThatThrownBy(() -> checklistService.createChecklist(MUNICIPALITY_ID, body))
			.isInstanceOfAny(Problem.class)
			.hasMessage("Bad Request: Organization with organization number %s does not exist within municipality %s".formatted(body.getOrganizationNumber(), MUNICIPALITY_ID));

		verify(checklistRepositoryMock).existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(body.getOrganizationNumber(), MUNICIPALITY_ID);
	}

	@Test
	void createChecklistChecklistAlreadyExists() {
		final var body = createChecklistCreateRequest();
		when(checklistRepositoryMock.existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID)).thenReturn(true);

		assertThatThrownBy(() -> checklistService.createChecklist(MUNICIPALITY_ID, body))
			.isInstanceOfAny(Problem.class)
			.hasMessage("Bad Request: Checklist with name '%s' already exists in municipality %s".formatted(body.getName(), MUNICIPALITY_ID));

		verify(checklistRepositoryMock).existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID);
	}

	@Test
	void createChecklistWhenOrganizationAlreadyHasConnectedChecklist() {
		final var body = createChecklistCreateRequest();
		final var organization = createOrganizationEntity();
		organization.setOrganizationNumber(body.getOrganizationNumber());
		organization.setChecklists(List.of(createChecklistEntity()));

		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(anyInt(), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(organization));

		assertThatThrownBy(() -> checklistService.createChecklist(MUNICIPALITY_ID, body))
			.isInstanceOfAny(Problem.class)
			.hasMessage("Bad Request: Organization %s already has a defined checklist and can therefor not create a new checklist".formatted(body.getOrganizationNumber()));

		verify(checklistRepositoryMock).existsByNameAndMunicipalityId(body.getName(), MUNICIPALITY_ID);
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(body.getOrganizationNumber(), MUNICIPALITY_ID);
	}

	@ParameterizedTest
	@EnumSource(value = LifeCycle.class, names = {
		"ACTIVE", "DEPRECATED"
	}, mode = Mode.EXCLUDE)
	void deleteUnattachedChecklist(LifeCycle lifeCycle) {
		final var entity = createChecklistEntity();
		entity.setLifeCycle(lifeCycle);
		when(checklistRepositoryMock.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		doNothing().when(eventServiceMock).createChecklistEvent(DELETE, CHECKLIST_DELETED.formatted(entity.getDisplayName()), entity, USER);

		checklistService.deleteChecklist(MUNICIPALITY_ID, entity.getId(), USER);

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).delete(entity);
		verify(organizationRepositoryMock).findByChecklistsIdAndChecklistsMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(sortorderServiceMock, times(3)).deleteSortorderItem(sortorderItemIdCaptor.capture());
		verify(eventServiceMock).createChecklistEvent(DELETE, CHECKLIST_DELETED.formatted(entity.getDisplayName()), entity, USER);

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
		doNothing().when(eventServiceMock).createChecklistEvent(DELETE, CHECKLIST_DELETED.formatted(checklistEntity.getDisplayName()), checklistEntity, USER);

		checklistService.deleteChecklist(MUNICIPALITY_ID, checklistEntity.getId(), USER);

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).delete(checklistEntity);
		verify(organizationRepositoryMock).findByChecklistsIdAndChecklistsMunicipalityId(checklistEntity.getId(), MUNICIPALITY_ID);
		verify(organizationRepositoryMock).save(organizationEntity);
		verify(sortorderServiceMock, times(3)).deleteSortorderItem(sortorderItemIdCaptor.capture());
		verify(eventServiceMock).createChecklistEvent(DELETE, CHECKLIST_DELETED.formatted(checklistEntity.getDisplayName()), checklistEntity, USER);

		assertThat(organizationEntity.getChecklists()).doesNotContain(checklistEntity);
		assertThat(sortorderItemIdCaptor.getAllValues()).satisfiesExactlyInAnyOrder(
			id -> assertThat(checklistEntity.getTasks().getFirst().getId()).isEqualTo(id),
			id -> assertThat(checklistEntity.getTasks().getLast().getId()).isEqualTo(id),
			id -> assertThat(checklistEntity.getTasks().getFirst().getPhase().getId()).isEqualTo(id));
	}

	@Test
	void deleteChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.deleteChecklist(MUNICIPALITY_ID, UUID, USER))
			.isInstanceOfAny(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

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

		assertThatThrownBy(() -> checklistService.deleteChecklist(MUNICIPALITY_ID, entity.getId(), USER))
			.isInstanceOfAny(Problem.class)
			.hasMessage("Bad Request: Cannot delete checklist with lifecycle %s".formatted(lifeCycle));

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
	}

	@Test
	void updateChecklist() {
		final var entity = createChecklistEntity();

		final var checklist = createChecklistUpdateRequest();
		when(checklistRepositoryMock.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(checklistRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(checklistBuilderMock.buildChecklist(entity)).thenReturn(Checklist.builder().withId(entity.getId()).build());
		when(sortorderServiceMock.applySortingToChecklist(any(), any(), any(Checklist.class))).thenAnswer(inv -> inv.getArgument(2));
		doNothing().when(eventServiceMock).createChecklistEvent(EventType.UPDATE, CHECKLIST_UPDATED.formatted(checklist.getDisplayName()), entity, checklist.getUpdatedBy());

		final var result = checklistService.updateChecklist(MUNICIPALITY_ID, entity.getId(), checklist);

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).save(checklistEntityCaptor.capture());
		verify(checklistBuilderMock).buildChecklist(entity);
		verify(sortorderServiceMock).applySortingToChecklist(eq(MUNICIPALITY_ID), eq(entity.getOrganization().getOrganizationNumber()), any(Checklist.class));
		verify(eventServiceMock).createChecklistEvent(EventType.UPDATE, CHECKLIST_UPDATED.formatted(checklist.getDisplayName()), entity, checklist.getUpdatedBy());

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
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
	}

	@Test
	void createNewVersion() {
		final var organizationEntity = createOrganizationEntity();
		final var checklistEntity = createChecklistEntity();
		checklistEntity.setId(UUID);
		checklistEntity.setLifeCycle(ACTIVE);
		organizationEntity.getChecklists().add(checklistEntity);
		final var copyEntity = createChecklistEntity();
		copyEntity.setLifeCycle(CREATED);
		copyEntity.setVersion(2);
		copyEntity.getTasks().getFirst().setPhase(checklistEntity.getTasks().getFirst().getPhase());
		copyEntity.getTasks().getLast().setPhase(checklistEntity.getTasks().getLast().getPhase());

		when(checklistRepositoryMock.findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID)).thenReturn(Optional.of(checklistEntity));
		when(organizationRepositoryMock.findByChecklistsIdAndChecklistsMunicipalityId(UUID, MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));
		when(checklistUtilsMock.clone(checklistEntity)).thenReturn(copyEntity);
		when(checklistRepositoryMock.save(any())).thenAnswer(invoker -> invoker.getArgument(0));
		when(checklistBuilderMock.buildChecklist(copyEntity)).thenReturn(Checklist.builder().withId(copyEntity.getId()).build());
		when(sortorderServiceMock.applySortingToChecklist(any(), any(), any(Checklist.class))).thenAnswer(inv -> inv.getArgument(2));

		final var result = checklistService.createNewVersion(MUNICIPALITY_ID, UUID);

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
		verify(checklistRepositoryMock).existsByNameAndMunicipalityIdAndLifeCycle(checklistEntity.getName(), MUNICIPALITY_ID, CREATED);
		verify(checklistUtilsMock).clone(checklistEntity);
		verify(checklistRepositoryMock).save(copyEntity);
		verify(checklistBuilderMock).buildChecklist(copyEntity);
		verify(sortorderServiceMock).copySortorderItems(anyMap());
		verify(sortorderServiceMock).applySortingToChecklist(eq(MUNICIPALITY_ID), eq(organizationEntity.getOrganizationNumber()), any(Checklist.class));
		verify(sortorderServiceMock).copySortorderItems(sortorderItemsCaptor.capture());

		assertThat(organizationEntity.getChecklists()).hasSize(2).containsExactlyInAnyOrder(checklistEntity, copyEntity);

		assertThat(sortorderItemsCaptor.getValue()).hasSize(2).containsExactlyInAnyOrderEntriesOf(Map.of(
			copyEntity.getTasks().getFirst().getId(), checklistEntity.getTasks().getFirst().getId(),
			copyEntity.getTasks().getLast().getId(), checklistEntity.getTasks().getLast().getId()));

		assertThat(result).isNotNull();
	}

	@Test
	void createNewVersionChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.createNewVersion(MUNICIPALITY_ID, UUID))
			.isInstanceOfAny(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
	}

	@Test
	void createNewVersionOrganizationNotFound() {
		final var entity = createChecklistEntity();

		when(checklistRepositoryMock.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		assertThatThrownBy(() -> checklistService.createNewVersion(MUNICIPALITY_ID, entity.getId()))
			.isInstanceOfAny(Problem.class)
			.hasMessage("Not Found: No organization is connected to checklist with id %s".formatted(entity.getId()));

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(organizationRepositoryMock).findByChecklistsIdAndChecklistsMunicipalityId(entity.getId(), MUNICIPALITY_ID);
	}

	@Test
	void createNewVersionBadRequest() {
		final var entity = createChecklistEntity();

		when(checklistRepositoryMock.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(organizationRepositoryMock.findByChecklistsIdAndChecklistsMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(OrganizationEntity.builder().build()));
		when(checklistRepositoryMock.existsByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, CREATED)).thenReturn(true);

		assertThatThrownBy(() -> checklistService.createNewVersion(MUNICIPALITY_ID, entity.getId()))
			.isInstanceOfAny(Problem.class)
			.hasMessage("Bad Request: Checklist already has a draft version in progress preventing another draft version from being created");

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(organizationRepositoryMock).findByChecklistsIdAndChecklistsMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).existsByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, CREATED);
	}

	@Test
	void activateChecklist() {
		final var entity = createChecklistEntity();
		final var oldVersion = createChecklistEntity();
		when(checklistRepositoryMock.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(checklistRepositoryMock.findByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, ACTIVE)).thenReturn(Optional.of(oldVersion));
		when(checklistRepositoryMock.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(checklistBuilderMock.buildChecklist(entity)).thenReturn(Checklist.builder().withLifeCycle(ACTIVE).build());
		when(sortorderServiceMock.applySortingToChecklist(any(), any(), any(Checklist.class))).thenAnswer(inv -> inv.getArgument(2));

		final var result = checklistService.activateChecklist(MUNICIPALITY_ID, entity.getId());

		assertThat(result).isNotNull().satisfies(checklistEntity -> {
			assertThat(checklistEntity.getLifeCycle()).isEqualTo(ACTIVE);
		});
		assertThat(oldVersion.getLifeCycle()).isEqualTo(DEPRECATED);
		verify(checklistRepositoryMock).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(checklistRepositoryMock).findByNameAndMunicipalityIdAndLifeCycle(entity.getName(), MUNICIPALITY_ID, ACTIVE);
		verify(checklistRepositoryMock).saveAndFlush(any(ChecklistEntity.class));
		verify(checklistBuilderMock).buildChecklist(entity);
		verify(sortorderServiceMock).applySortingToChecklist(eq(MUNICIPALITY_ID), eq(entity.getOrganization().getOrganizationNumber()), any(Checklist.class));
	}

	@Test
	void activateChecklistNotFound() {
		assertThatThrownBy(() -> checklistService.activateChecklist(MUNICIPALITY_ID, UUID))
			.isInstanceOfAny(Problem.class)
			.hasMessage("Not Found: Checklist not found within municipality %s".formatted(MUNICIPALITY_ID));

		verify(checklistRepositoryMock).findByIdAndMunicipalityId(UUID, MUNICIPALITY_ID);
	}

}
