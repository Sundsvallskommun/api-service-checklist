package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.TestObjectFactory.createChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.createOrganizationCreateRequest;
import static se.sundsvall.checklist.TestObjectFactory.createOrganizationEntity;
import static se.sundsvall.checklist.TestObjectFactory.createOrganizationUpdateRequest;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

import se.sundsvall.checklist.api.model.Organization;
import se.sundsvall.checklist.integration.db.ChecklistBuilder;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String ORGANIZATION_NUMBER_ALREADY_EXISTS = "Organization with organization number %s already exists";
	private static final String ORGANIZATION_HAS_CHECKLISTS = "Organization with id %s has non retired checklists and cannot be deleted";
	private static final String ORGANIZATION_NOT_FOUND = "Organization with id %s does not exist";

	@Mock
	private OrganizationRepository mockOrganizationRepository;

	@Mock
	private ChecklistBuilder mockChecklistBuilder;

	@Mock
	private SortorderService mockSortorderService;

	@InjectMocks
	private OrganizationService organizationService;

	@Test
	void createOrganizationAlreadyExists() {
		final var request = createOrganizationCreateRequest();
		when(mockOrganizationRepository.findByOrganizationNumberAndMunicipalityId(request.getOrganizationNumber(), MUNICIPALITY_ID)).thenReturn(Optional.of(createOrganizationEntity()));

		assertThatThrownBy(() -> organizationService.createOrganization(MUNICIPALITY_ID, request))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", CONFLICT)
			.hasMessageContaining(ORGANIZATION_NUMBER_ALREADY_EXISTS.formatted(request.getOrganizationNumber()));

		verify(mockOrganizationRepository).findByOrganizationNumberAndMunicipalityId(request.getOrganizationNumber(), MUNICIPALITY_ID);
	}

	@Test
	void createOrganization() {
		final var request = createOrganizationCreateRequest();
		final var entity = createOrganizationEntity();

		when(mockOrganizationRepository.findByOrganizationNumberAndMunicipalityId(request.getOrganizationNumber(), MUNICIPALITY_ID)).thenReturn(Optional.empty());
		when(mockOrganizationRepository.save(any())).thenReturn(entity);

		final var result = organizationService.createOrganization(MUNICIPALITY_ID, request);

		assertThat(result).isEqualTo(entity.getId());

		verify(mockOrganizationRepository).findByOrganizationNumberAndMunicipalityId(request.getOrganizationNumber(), MUNICIPALITY_ID);
		verify(mockOrganizationRepository).save(any());
	}

	@Test
	void fetchAllOrganizationsWithoutFilter() {
		final var entity = createOrganizationEntity();
		when(mockOrganizationRepository.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(entity));

		final var result = organizationService.fetchAllOrganizations(MUNICIPALITY_ID, null, null);

		assertThat(result).hasSize(1);
		assertThat(result.getFirst()).isInstanceOf(Organization.class);

		verify(mockOrganizationRepository).findAllByMunicipalityId(MUNICIPALITY_ID);
		entity.getChecklists().forEach(checklist -> {
			verify(mockChecklistBuilder).buildChecklist(checklist);
		});
	}

	@Test
	void fetchAllOrganizationsWithFilter() {
		final var entity = createOrganizationEntity();
		entity.setOrganizationNumber(2345);
		when(mockOrganizationRepository.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(entity, createOrganizationEntity()));

		final var result = organizationService.fetchAllOrganizations(MUNICIPALITY_ID, List.of(1234), null);

		assertThat(result).hasSize(1);
		assertThat(result.getFirst()).isInstanceOf(Organization.class);

		verify(mockOrganizationRepository).findAllByMunicipalityId(MUNICIPALITY_ID);
		entity.getChecklists().forEach(checklist -> {
			verify(mockChecklistBuilder).buildChecklist(checklist);
		});
	}

	@Test
	void fetchOrganization() {
		final var entity = createOrganizationEntity();
		when(mockOrganizationRepository.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		final var result = organizationService.fetchOrganization(MUNICIPALITY_ID, entity.getId(), null);

		assertThat(result).satisfies(organization -> {
			assertThat(organization).isInstanceOf(Organization.class);
			assertThat(organization.getId()).isEqualTo(entity.getId());
			assertThat(organization.getOrganizationName()).isEqualTo(entity.getOrganizationName());
			assertThat(organization.getOrganizationNumber()).isEqualTo(entity.getOrganizationNumber());
			assertThat(organization.getCreated()).isEqualTo(entity.getCreated());
			assertThat(organization.getUpdated()).isEqualTo(entity.getUpdated());
		});

		verify(mockOrganizationRepository).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		entity.getChecklists().forEach(checklist -> {
			verify(mockChecklistBuilder).buildChecklist(checklist);
		});
	}

	@Test
	void fetchOrganizationsByIdNotFound() {
		assertThatThrownBy(() -> organizationService.fetchOrganization(MUNICIPALITY_ID, "id", null))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasMessageContaining(ORGANIZATION_NOT_FOUND.formatted("id"));

		verify(mockOrganizationRepository).findByIdAndMunicipalityId("id", MUNICIPALITY_ID);
	}

	@Test
	void updateOrganization() {
		final var entity = createOrganizationEntity();
		final var request = createOrganizationUpdateRequest();
		when(mockOrganizationRepository.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(mockOrganizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		final var result = organizationService.updateOrganization(MUNICIPALITY_ID, entity.getId(), request);

		assertThat(result).satisfies(organization -> {
			assertThat(organization.getOrganizationName()).isEqualTo(request.getOrganizationName());
			assertThat(organization.getCommunicationChannels()).isEqualTo(request.getCommunicationChannels());
		});

		verify(mockOrganizationRepository).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(mockOrganizationRepository).save(any());
	}

	@Test
	void updateOrganizationNotFound() {
		final var request = createOrganizationUpdateRequest();

		assertThatThrownBy(() -> organizationService.updateOrganization(MUNICIPALITY_ID, "id", request))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasMessageContaining(ORGANIZATION_NOT_FOUND.formatted("id"));

		verify(mockOrganizationRepository).findByIdAndMunicipalityId("id", MUNICIPALITY_ID);
		verify(mockOrganizationRepository, never()).save(any());
	}

	@Test
	void deleteOrganization() {
		final var entity = createOrganizationEntity();
		when(mockOrganizationRepository.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		organizationService.deleteOrganization(MUNICIPALITY_ID, entity.getId());

		verify(mockOrganizationRepository).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(mockSortorderService).deleteSortorder(MUNICIPALITY_ID, entity.getOrganizationNumber());
		verify(mockOrganizationRepository).delete(entity);
	}

	@Test
	void deleteOrganizationNotFound() {
		assertThatThrownBy(() -> organizationService.deleteOrganization(MUNICIPALITY_ID, "id"))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasMessageContaining(ORGANIZATION_NOT_FOUND.formatted("id"));

		verify(mockOrganizationRepository).findByIdAndMunicipalityId("id", MUNICIPALITY_ID);
		verify(mockOrganizationRepository, never()).delete(any());
	}

	@Test
	void deleteOrganizationConflict() {
		final var entity = createOrganizationEntity();
		entity.setChecklists(List.of(createChecklistEntity()));
		when(mockOrganizationRepository.findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		assertThatThrownBy(() -> organizationService.deleteOrganization(MUNICIPALITY_ID, entity.getId()))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", CONFLICT)
			.hasMessageContaining(ORGANIZATION_HAS_CHECKLISTS.formatted(entity.getId()));

		verify(mockOrganizationRepository).findByIdAndMunicipalityId(entity.getId(), MUNICIPALITY_ID);
		verify(mockOrganizationRepository, never()).delete(any());
	}

	@AfterEach
	void verifyNoMoreInteraction() {
		verifyNoMoreInteractions(mockOrganizationRepository, mockChecklistBuilder, mockSortorderService);
	}
}
