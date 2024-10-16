package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

import se.sundsvall.checklist.api.model.Organization;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

	private static final String ORGANIZATION_NUMBER_ALREADY_EXISTS = "Organization with organization number %s already exists";
	private static final String ORGANIZATION_HAS_CHECKLISTS = "Organization with id %s has non retired checklists and cannot be deleted";
	private static final String ORGANIZATION_NOT_FOUND = "Organization with id %s does not exist";

	@Mock
	private OrganizationRepository mockOrganizationRepository;

	@InjectMocks
	private OrganizationService organizationService;

	@Test
	void createOrganizationAlreadyExistsTest() {
		var request = createOrganizationCreateRequest();
		when(mockOrganizationRepository.findByOrganizationNumber(anyInt())).thenReturn(Optional.of(createOrganizationEntity()));

		assertThatThrownBy(() -> organizationService.createOrganization(request))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", CONFLICT)
			.hasMessageContaining(ORGANIZATION_NUMBER_ALREADY_EXISTS.formatted(request.getOrganizationNumber()));
		verify(mockOrganizationRepository).findByOrganizationNumber(request.getOrganizationNumber());
		verify(mockOrganizationRepository, never()).save(createOrganizationEntity());
	}

	@Test
	void createOrganizationTest() {
		var request = createOrganizationCreateRequest();
		var entity = createOrganizationEntity();

		when(mockOrganizationRepository.findByOrganizationNumber(anyInt())).thenReturn(Optional.empty());
		when(mockOrganizationRepository.save(any())).thenReturn(entity);

		var result = organizationService.createOrganization(request);

		assertThat(result).isEqualTo(entity.getId());

		verify(mockOrganizationRepository).findByOrganizationNumber(anyInt());
		verify(mockOrganizationRepository).save(any());
		verifyNoMoreInteractions(mockOrganizationRepository);
	}

	@Test
	void fetchAllOrganizationsTest() {
		when(mockOrganizationRepository.findAll()).thenReturn(List.of(createOrganizationEntity()));

		var result = organizationService.fetchAllOrganizations();

		assertThat(result).hasSize(1);
		assertThat(result.getFirst()).isInstanceOf(Organization.class);

		verify(mockOrganizationRepository).findAll();
		verifyNoMoreInteractions(mockOrganizationRepository);
	}

	@Test
	void fetchOrganizationByIdTest() {
		var entity = createOrganizationEntity();
		when(mockOrganizationRepository.findById(any())).thenReturn(Optional.of(entity));

		var result = organizationService.fetchOrganizationById(entity.getId());

		assertThat(result).satisfies(organization -> {
			assertThat(organization).isInstanceOf(Organization.class);
			assertThat(organization.getId()).isEqualTo(entity.getId());
			assertThat(organization.getOrganizationName()).isEqualTo(entity.getOrganizationName());
			assertThat(organization.getOrganizationNumber()).isEqualTo(entity.getOrganizationNumber());
			assertThat(organization.getCreated()).isEqualTo(entity.getCreated());
			assertThat(organization.getUpdated()).isEqualTo(entity.getUpdated());
		});

		verify(mockOrganizationRepository).findById(entity.getId());
		verifyNoMoreInteractions(mockOrganizationRepository);
	}

	@Test
	void fetchOrganizationsByIdNotFoundTest() {
		when(mockOrganizationRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> organizationService.fetchOrganizationById("id"))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasMessageContaining(ORGANIZATION_NOT_FOUND.formatted("id"));

		verify(mockOrganizationRepository).findById("id");
		verifyNoMoreInteractions(mockOrganizationRepository);
	}

	@Test
	void updateOrganizationTest() {
		var entity = createOrganizationEntity();
		var request = createOrganizationUpdateRequest();
		when(mockOrganizationRepository.findById(any())).thenReturn(Optional.of(entity));
		when(mockOrganizationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var result = organizationService.updateOrganization(entity.getId(), request);

		assertThat(result).satisfies(organization -> {
			assertThat(organization.getOrganizationName()).isEqualTo(request.getOrganizationName());
			assertThat(organization.getCommunicationChannels()).isEqualTo(request.getCommunicationChannels());
		});

		verify(mockOrganizationRepository).findById(entity.getId());
		verify(mockOrganizationRepository).save(any());
		verifyNoMoreInteractions(mockOrganizationRepository);
	}

	@Test
	void updateOrganizationNotFoundTest() {
		var request = createOrganizationUpdateRequest();
		when(mockOrganizationRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> organizationService.updateOrganization("id", request))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasMessageContaining(ORGANIZATION_NOT_FOUND.formatted("id"));

		verify(mockOrganizationRepository).findById("id");
		verify(mockOrganizationRepository, never()).save(any());
		verifyNoMoreInteractions(mockOrganizationRepository);
	}

	@Test
	void deleteOrganizationTest() {
		var entity = createOrganizationEntity();
		when(mockOrganizationRepository.findById(any())).thenReturn(Optional.of(entity));

		organizationService.deleteOrganization(entity.getId());

		verify(mockOrganizationRepository).findById(entity.getId());
		verify(mockOrganizationRepository).delete(entity);
		verifyNoMoreInteractions(mockOrganizationRepository);
	}

	@Test
	void deleteOrganizationNotFoundTest() {
		when(mockOrganizationRepository.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> organizationService.deleteOrganization("id"))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasMessageContaining(ORGANIZATION_NOT_FOUND.formatted("id"));

		verify(mockOrganizationRepository).findById("id");
		verify(mockOrganizationRepository, never()).delete(any());
		verifyNoMoreInteractions(mockOrganizationRepository);
	}

	@Test
	void deleteOrganizationConflictTest() {
		var entity = createOrganizationEntity();
		entity.setChecklists(List.of(createChecklistEntity()));
		when(mockOrganizationRepository.findById(any())).thenReturn(Optional.of(entity));

		assertThatThrownBy(() -> organizationService.deleteOrganization("id"))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", CONFLICT)
			.hasMessageContaining(ORGANIZATION_HAS_CHECKLISTS.formatted("id"));

		verify(mockOrganizationRepository).findById("id");
		verify(mockOrganizationRepository, never()).delete(any());
		verifyNoMoreInteractions(mockOrganizationRepository);
	}

}
