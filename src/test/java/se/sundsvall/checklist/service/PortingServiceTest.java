package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.ACTIVE;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.CREATED;
import static se.sundsvall.checklist.service.PortingService.SYSTEM;

import java.util.ArrayList;
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
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.service.mapper.OrganizationMapper;

@ExtendWith(MockitoExtension.class)
class PortingServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";

	@Mock
	private ChecklistRepository checklistRepositoryMock;

	@Mock
	private OrganizationRepository organizationRepositoryMock;

	@InjectMocks
	private PortingService service;

	@Captor
	private ArgumentCaptor<ChecklistEntity> checklistEntityCaptor;

	@Captor
	private ArgumentCaptor<OrganizationEntity> organizationEntityCaptor;

	@Test
	void exportLatestVersionOfChecklist() {
		// Arrange
		final var organizationNumber = 123;
		final var organizationEntity = OrganizationEntity.builder()
			.withChecklists(List.of(
				ChecklistEntity.builder()
					.withVersion(3)
					.build(),
				ChecklistEntity.builder()
					.withVersion(2)
					.build(),
				ChecklistEntity.builder()
					.withVersion(1)
					.build()))
			.build();

		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		// Act
		final var response = service.exportChecklist(MUNICIPALITY_ID, organizationNumber, null);

		// Assert and verify
		assertThat(response).isEqualTo("{\"version\":3,\"tasks\":[]}");

		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);
	}

	@Test
	void exportSpecificChecklistVersion() {
		// Arrange
		final var organizationNumber = 123;
		final var organizationEntity = OrganizationEntity.builder()
			.withChecklists(List.of(
				ChecklistEntity.builder()
					.withVersion(3)
					.build(),
				ChecklistEntity.builder()
					.withVersion(2)
					.build(),
				ChecklistEntity.builder()
					.withVersion(1)
					.build()))
			.build();

		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		// Act
		final var response = service.exportChecklist(MUNICIPALITY_ID, organizationNumber, 1);

		// Assert and verify
		assertThat(response).isEqualTo("{\"version\":1,\"tasks\":[]}");

		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);
	}

	@Test
	void exportThrowsExceptionWhenNoChecklistMatch() {
		// Arrange
		final var organizationNumber = 123;
		final var organizationEntity = OrganizationEntity.builder().build();

		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.exportChecklist(MUNICIPALITY_ID, organizationNumber, null));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No checklist matching sent in parameters exist.");

		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);
	}

	@Test
	void addChecklistVersionWhenActiveVersionExists() {
		// Arrange
		final var activeVersionName = "activeVersionName";
		final var organizationNumber = 123;
		final var organizationName = "organizationName";
		final var organizationEntity = OrganizationEntity.builder()
			.withChecklists(new ArrayList<>(List.of(
				ChecklistEntity.builder()
					.withName(activeVersionName)
					.withLifeCycle(ACTIVE)
					.withVersion(1)
					.build())))
			.build();
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		// Act
		service.importChecklist(MUNICIPALITY_ID, organizationNumber, organizationName, jsonStructure, false);

		// Assert and verify
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);
		verify(checklistRepositoryMock).save(checklistEntityCaptor.capture());

		assertThat(organizationEntity.getChecklists()).hasSize(2)
			.filteredOn(ch -> CREATED == ch.getLifeCycle())
			.allSatisfy(ch -> {
				assertThat(ch.getDisplayName()).isEqualTo("displayName");
				assertThat(ch.getName()).isEqualTo("activeVersionName");
				assertThat(ch.getVersion()).isEqualTo(2);
				assertThat(ch.getLastSavedBy()).isEqualTo(SYSTEM);
			});

		assertThat(checklistEntityCaptor.getValue().getName()).isEqualTo("activeVersionName");
		assertThat(checklistEntityCaptor.getValue().getDisplayName()).isEqualTo("displayName");
		assertThat(checklistEntityCaptor.getValue().getVersion()).isEqualTo(2);
	}

	@Test
	void addChecklistVersionWhenCreatedVersionExists() {
		// Arrange
		final var organizationNumber = 123;
		final var organizationName = "organizationName";
		final var existingChecklistEntities = List.of(
			ChecklistEntity.builder()
				.withLifeCycle(ACTIVE)
				.withVersion(1)
				.build(),
			ChecklistEntity.builder()
				.withLifeCycle(CREATED)
				.withVersion(2)
				.build());
		final var organizationEntity = OrganizationEntity.builder()
			.withChecklists(new ArrayList<>(existingChecklistEntities))
			.build();

		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> service.importChecklist(MUNICIPALITY_ID, organizationNumber, organizationName, jsonStructure, false));

		// Assert and verify
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);

		assertThat(organizationEntity.getChecklists()).isEqualTo(existingChecklistEntities);
		assertThat(e.getStatus()).isEqualTo(Status.CONFLICT);
		assertThat(e.getMessage()).isEqualTo("Conflict: The organization has an existing checklist with lifecycle status CREATED present, operation aborted.");
	}

	@Test
	void addChecklistVersionWhenNoChecklistExists() {
		// Arrange
		final var organizationNumber = 123;
		final var organizationName = "organizationName";
		final var organizationEntity = OrganizationEntity.builder().build();
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		// Act
		service.importChecklist(MUNICIPALITY_ID, organizationNumber, organizationName, jsonStructure, false);

		// Assert and verify
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);
		verify(checklistRepositoryMock).save(checklistEntityCaptor.capture());

		assertThat(organizationEntity.getChecklists()).hasSize(1)
			.allSatisfy(ch -> {
				assertThat(ch.getDisplayName()).isEqualTo("displayName");
				assertThat(ch.getName()).isEqualTo("name");
				assertThat(ch.getVersion()).isEqualTo(1);
				assertThat(ch.getLastSavedBy()).isEqualTo(SYSTEM);
			});

		assertThat(checklistEntityCaptor.getValue().getName()).isEqualTo("name");
		assertThat(checklistEntityCaptor.getValue().getDisplayName()).isEqualTo("displayName");
		assertThat(checklistEntityCaptor.getValue().getVersion()).isEqualTo(1);
	}

	@Test
	void addChecklistVersionForNewOrganization() {
		// Arrange
		final var organizationNumber = 123;
		final var organizationName = "organizationName";
		final var organizationEntity = OrganizationMapper.toOrganizationEntity(organizationNumber, organizationName, MUNICIPALITY_ID);
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		when(organizationRepositoryMock.save(any())).thenReturn(organizationEntity);

		// Act
		service.importChecklist(MUNICIPALITY_ID, organizationNumber, organizationName, jsonStructure, false);

		// Assert and verify
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);
		verify(organizationRepositoryMock).save(organizationEntityCaptor.capture());
		verify(checklistRepositoryMock).save(checklistEntityCaptor.capture());

		assertThat(organizationEntity.getChecklists()).hasSize(1)
			.allSatisfy(ch -> {
				assertThat(ch.getDisplayName()).isEqualTo("displayName");
				assertThat(ch.getName()).isEqualTo("name");
				assertThat(ch.getVersion()).isEqualTo(1);
				assertThat(ch.getLastSavedBy()).isEqualTo(SYSTEM);
			});

		assertThat(organizationEntityCaptor.getValue().getOrganizationName()).isEqualTo(organizationName);
		assertThat(organizationEntityCaptor.getValue().getOrganizationNumber()).isEqualTo(organizationNumber);

		assertThat(checklistEntityCaptor.getValue().getName()).isEqualTo("name");
		assertThat(checklistEntityCaptor.getValue().getDisplayName()).isEqualTo("displayName");
		assertThat(checklistEntityCaptor.getValue().getVersion()).isEqualTo(1);
	}

	@Test
	void replaceChecklistOverwritingActiveVersion() {
		// Arrange
		final var activeVersionName = "activeVersionName";
		final var organizationNumber = 123;
		final var organizationName = "organizationName";
		final var checklistId = UUID.randomUUID().toString();
		final var organizationEntity = OrganizationEntity.builder()
			.withChecklists(new ArrayList<>(List.of(
				ChecklistEntity.builder()
					.withId(checklistId)
					.withName(activeVersionName)
					.withLifeCycle(ACTIVE)
					.withVersion(1)
					.build())))
			.build();
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		// Act
		service.importChecklist(MUNICIPALITY_ID, organizationNumber, organizationName, jsonStructure, true);

		// Assert and verify
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);
		verify(checklistRepositoryMock).save(checklistEntityCaptor.capture());

		assertThat(organizationEntity.getChecklists()).hasSize(1)
			.allSatisfy(ch -> {
				assertThat(ch.getId()).isEqualTo(checklistId);
				assertThat(ch.getDisplayName()).isEqualTo("displayName");
				assertThat(ch.getName()).isEqualTo(activeVersionName);
				assertThat(ch.getVersion()).isEqualTo(1);
				assertThat(ch.getLastSavedBy()).isEqualTo(SYSTEM);
			});

		assertThat(checklistEntityCaptor.getValue().getName()).isEqualTo(activeVersionName);
		assertThat(checklistEntityCaptor.getValue().getDisplayName()).isEqualTo("displayName");
		assertThat(checklistEntityCaptor.getValue().getVersion()).isEqualTo(1);
	}

	@Test
	void replaceChecklistOverwritingCreatedVersion() {
		// Arrange
		final var createdVersionName = "createdVersionName";
		final var organizationNumber = 123;
		final var organizationName = "organizationName";
		final var checklistId = UUID.randomUUID().toString();
		final var organizationEntity = OrganizationEntity.builder()
			.withChecklists(new ArrayList<>(List.of(
				ChecklistEntity.builder()
					.withLifeCycle(ACTIVE)
					.withVersion(1)
					.build(),
				ChecklistEntity.builder()
					.withId(checklistId)
					.withName(createdVersionName)
					.withLifeCycle(CREATED)
					.withVersion(2)
					.build())))
			.build();
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		// Act
		service.importChecklist(MUNICIPALITY_ID, organizationNumber, organizationName, jsonStructure, true);

		// Assert and verify
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);
		verify(checklistRepositoryMock).save(checklistEntityCaptor.capture());

		assertThat(organizationEntity.getChecklists()).hasSize(2)
			.filteredOn(ch -> CREATED == ch.getLifeCycle())
			.allSatisfy(ch -> {
				assertThat(ch.getDisplayName()).isEqualTo("displayName");
				assertThat(ch.getName()).isEqualTo(createdVersionName);
				assertThat(ch.getVersion()).isEqualTo(2);
				assertThat(ch.getLastSavedBy()).isEqualTo(SYSTEM);
			});

		assertThat(checklistEntityCaptor.getValue().getName()).isEqualTo(createdVersionName);
		assertThat(checklistEntityCaptor.getValue().getDisplayName()).isEqualTo("displayName");
		assertThat(checklistEntityCaptor.getValue().getVersion()).isEqualTo(2);
	}

	@Test
	void replaceChecklistWhenNoChecklistExists() {
		// Arrange
		final var organizationNumber = 123;
		final var organizationName = "organizationName";
		final var organizationEntity = OrganizationEntity.builder().build();
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		when(organizationRepositoryMock.findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID)).thenReturn(Optional.of(organizationEntity));

		// Act
		service.importChecklist(MUNICIPALITY_ID, organizationNumber, organizationName, jsonStructure, true);

		// Assert and verify
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);
		verify(checklistRepositoryMock).save(checklistEntityCaptor.capture());

		assertThat(organizationEntity.getChecklists()).hasSize(1)
			.allSatisfy(ch -> {
				assertThat(ch.getDisplayName()).isEqualTo("displayName");
				assertThat(ch.getName()).isEqualTo("name");
				assertThat(ch.getVersion()).isEqualTo(1);
				assertThat(ch.getLastSavedBy()).isEqualTo(SYSTEM);
			});

		assertThat(checklistEntityCaptor.getValue().getName()).isEqualTo("name");
		assertThat(checklistEntityCaptor.getValue().getDisplayName()).isEqualTo("displayName");
		assertThat(checklistEntityCaptor.getValue().getVersion()).isEqualTo(1);
	}

	@Test
	void replaceChecklistForNewOrganization() {
		// Arrange
		final var organizationNumber = 123;
		final var organizationName = "organizationName";
		final var organizationEntity = OrganizationMapper.toOrganizationEntity(organizationNumber, organizationName, MUNICIPALITY_ID);
		final var jsonStructure = """
			{
				"name": "name",
				"displayName": "displayName"
			}""";

		when(organizationRepositoryMock.save(any())).thenReturn(organizationEntity);

		// Act
		service.importChecklist(MUNICIPALITY_ID, organizationNumber, organizationName, jsonStructure, true);

		// Assert and verify
		verify(organizationRepositoryMock).findByOrganizationNumberAndMunicipalityId(organizationNumber, MUNICIPALITY_ID);
		verify(organizationRepositoryMock).save(organizationEntityCaptor.capture());
		verify(checklistRepositoryMock).save(checklistEntityCaptor.capture());

		assertThat(organizationEntity.getChecklists()).hasSize(1)
			.allSatisfy(ch -> {
				assertThat(ch.getDisplayName()).isEqualTo("displayName");
				assertThat(ch.getName()).isEqualTo("name");
				assertThat(ch.getVersion()).isEqualTo(1);
				assertThat(ch.getLastSavedBy()).isEqualTo(SYSTEM);
			});

		assertThat(organizationEntityCaptor.getValue().getOrganizationName()).isEqualTo(organizationName);
		assertThat(organizationEntityCaptor.getValue().getOrganizationNumber()).isEqualTo(organizationNumber);

		assertThat(checklistEntityCaptor.getValue().getName()).isEqualTo("name");
		assertThat(checklistEntityCaptor.getValue().getDisplayName()).isEqualTo("displayName");
		assertThat(checklistEntityCaptor.getValue().getVersion()).isEqualTo(1);
	}

	@AfterEach
	void noMoreInteractions() {
		verifyNoMoreInteractions(checklistRepositoryMock, organizationRepositoryMock);
	}
}
