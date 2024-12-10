package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.TestObjectFactory.generateSortorderRequest;

import generated.se.sundsvall.mdviewer.Organization;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.api.model.Task;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.SortorderEntity;
import se.sundsvall.checklist.integration.db.model.enums.ComponentType;
import se.sundsvall.checklist.integration.db.repository.SortorderRepository;
import se.sundsvall.checklist.integration.mdviewer.MDViewerClient;

@ExtendWith(MockitoExtension.class)
class SortorderServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final int ORGANIZATION_NUMBER = 123456;
	private static final String COMPONENT_ID1 = UUID.randomUUID().toString();
	private static final String COMPONENT_ID2 = UUID.randomUUID().toString();

	@Mock
	private SortorderRepository sortorderRepositoryMock;

	@Mock
	private MDViewerClient mdViewerClientMock;

	@Mock
	private Organization companyMock;

	@Mock
	private Organization organizationMock;

	@InjectMocks
	private SortorderService service;

	@Captor
	private ArgumentCaptor<List<SortorderEntity>> saveAllCaptor;

	@Test
	void saveSortorder() {
		// Arrange
		final var sortorderRequest = generateSortorderRequest();
		final var sortorderEntities = List.of(SortorderEntity.builder().build(), SortorderEntity.builder().build());

		when(sortorderRepositoryMock.findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(sortorderEntities);

		// Act
		service.saveSortorder(MUNICIPALITY_ID, ORGANIZATION_NUMBER, sortorderRequest);

		// Assert and verify
		verify(sortorderRepositoryMock).findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(sortorderRepositoryMock).deleteAllInBatch(sortorderEntities);
		verify(sortorderRepositoryMock).saveAll(saveAllCaptor.capture());
		verifyNoMoreInteractions(sortorderRepositoryMock);

		assertThat(saveAllCaptor.getValue()).hasSize(6);
	}

	@Test
	void deleteSortorder() {
		final var sortOrderEntities = List.of(SortorderEntity.builder().build(), SortorderEntity.builder().build());
		when(sortorderRepositoryMock.findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(sortOrderEntities);

		service.deleteSortorder(MUNICIPALITY_ID, ORGANIZATION_NUMBER);

		verify(sortorderRepositoryMock).findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(sortorderRepositoryMock).deleteAllInBatch(sortOrderEntities);
	}

	@Test
	void deleteSortorderItem() {
		final var sortOrderEntities = List.of(SortorderEntity.builder().build(), SortorderEntity.builder().build());
		when(sortorderRepositoryMock.findAllByComponentId(COMPONENT_ID1)).thenReturn(sortOrderEntities);

		service.deleteSortorderItem(COMPONENT_ID1);

		verify(sortorderRepositoryMock).findAllByComponentId(COMPONENT_ID1);
		verify(sortorderRepositoryMock).deleteAllInBatch(sortOrderEntities);
	}

	@Test
	void applySortingForEmployeeChecklist() {
		final var employeeChecklist = EmployeeChecklist.builder()
			.withPhases(List.of(
				EmployeeChecklistPhase.builder()
					.withId(COMPONENT_ID1)
					.withSortOrder(1)
					.withTasks(List.of(
						EmployeeChecklistTask.builder()
							.withId(COMPONENT_ID2)
							.withSortOrder(2)
							.build()))
					.build()))
			.build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(EmployeeEntity.builder()
				.withDepartment(OrganizationEntity.builder()
					.withMunicipalityId(MUNICIPALITY_ID)
					.withOrganizationNumber(ORGANIZATION_NUMBER)
					.build())
				.withCompany(OrganizationEntity.builder()
					.withOrganizationNumber(1)
					.build())
				.build())
			.build();

		when(mdViewerClientMock.getOrganizationsForCompany(1)).thenReturn(List.of(organizationMock));
		when(organizationMock.getOrgId()).thenReturn(ORGANIZATION_NUMBER);
		when(sortorderRepositoryMock.findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(createCustomorder());

		final var result = service.applySorting(Optional.of(employeeChecklistEntity), employeeChecklist);

		assertThat(result).extracting(EmployeeChecklist::getPhases).satisfies(phases -> {
			assertThat(phases.getFirst().getSortOrder()).isEqualTo(10);
			assertThat(phases.getFirst().getTasks().getFirst().getSortOrder()).isEqualTo(20);
		});
	}

	@Test
	void applySortingForEmployeeChecklistWhenNoCustomSort() {
		final var employeeChecklist = EmployeeChecklist.builder()
			.withPhases(List.of(
				EmployeeChecklistPhase.builder()
					.withId(COMPONENT_ID1)
					.withSortOrder(1)
					.withTasks(List.of(
						EmployeeChecklistTask.builder()
							.withId(COMPONENT_ID2)
							.withSortOrder(2)
							.build()))
					.build()))
			.build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(EmployeeEntity.builder()
				.withDepartment(OrganizationEntity.builder()
					.withMunicipalityId(MUNICIPALITY_ID)
					.withOrganizationNumber(ORGANIZATION_NUMBER)
					.build())
				.withCompany(OrganizationEntity.builder()
					.withOrganizationNumber(1)
					.build())
				.build())
			.build();

		when(mdViewerClientMock.getOrganizationsForCompany(1)).thenReturn(List.of(organizationMock));
		when(organizationMock.getOrgId()).thenReturn(ORGANIZATION_NUMBER);

		final var result = service.applySorting(Optional.of(employeeChecklistEntity), employeeChecklist);

		assertThat(result).extracting(EmployeeChecklist::getPhases).satisfies(phases -> {
			assertThat(phases.getFirst().getSortOrder()).isEqualTo(1);
			assertThat(phases.getFirst().getTasks().getFirst().getSortOrder()).isEqualTo(2);
		});

		verify(sortorderRepositoryMock).findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
	}

	@Test
	void applySortingForEmployeeChecklistWhenNoMatchingOrganization() {
		final var employeeChecklist = EmployeeChecklist.builder()
			.withPhases(List.of(
				EmployeeChecklistPhase.builder()
					.withId(COMPONENT_ID1)
					.withSortOrder(1)
					.withTasks(List.of(
						EmployeeChecklistTask.builder()
							.withId(COMPONENT_ID2)
							.withSortOrder(2)
							.build()))
					.build()))
			.build();
		final var employeeChecklistEntity = EmployeeChecklistEntity.builder()
			.withEmployee(EmployeeEntity.builder()
				.withDepartment(OrganizationEntity.builder()
					.withMunicipalityId(MUNICIPALITY_ID)
					.withOrganizationNumber(ORGANIZATION_NUMBER)
					.build())
				.withCompany(OrganizationEntity.builder()
					.withOrganizationNumber(1)
					.build())
				.build())
			.build();

		final var result = service.applySorting(Optional.of(employeeChecklistEntity), employeeChecklist);

		assertThat(result).extracting(EmployeeChecklist::getPhases).satisfies(phases -> {
			assertThat(phases.getFirst().getSortOrder()).isEqualTo(1);
			assertThat(phases.getFirst().getTasks().getFirst().getSortOrder()).isEqualTo(2);
		});

		verify(sortorderRepositoryMock, never()).findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
	}

	@Test
	void applySortingForChecklist() {
		final var checklist = Checklist.builder()
			.withPhases(
				List.of(Phase.builder()
					.withId(COMPONENT_ID1)
					.withSortOrder(1)
					.withTasks(
						List.of(Task.builder()
							.withId(COMPONENT_ID2)
							.withSortOrder(2)
							.build()))
					.build()))
			.build();

		when(mdViewerClientMock.getCompanies()).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(mdViewerClientMock.getOrganizationsForCompany(1)).thenReturn(Collections.emptyList());
		when(mdViewerClientMock.getOrganizationsForCompany(2)).thenReturn(List.of(organizationMock));
		when(organizationMock.getCompanyId()).thenReturn(2);
		when(organizationMock.getOrgId()).thenReturn(ORGANIZATION_NUMBER);
		when(sortorderRepositoryMock.findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(createCustomorder());

		final var result = service.applySorting(MUNICIPALITY_ID, ORGANIZATION_NUMBER, List.of(checklist));

		assertThat(result).extracting(Checklist::getPhases).satisfiesExactly(phases -> {
			assertThat(phases.getFirst().getSortOrder()).isEqualTo(10);
			assertThat(phases.getFirst().getTasks().getFirst().getSortOrder()).isEqualTo(20);
		});
	}

	@Test
	void applySortingForChecklistWhenNoCustomSort() {
		final var checklist = Checklist.builder()
			.withPhases(
				List.of(Phase.builder()
					.withId(COMPONENT_ID1)
					.withSortOrder(1)
					.withTasks(
						List.of(Task.builder()
							.withId(COMPONENT_ID2)
							.withSortOrder(2)
							.build()))
					.build()))
			.build();

		when(mdViewerClientMock.getCompanies()).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(mdViewerClientMock.getOrganizationsForCompany(1)).thenReturn(Collections.emptyList());
		when(mdViewerClientMock.getOrganizationsForCompany(2)).thenReturn(List.of(organizationMock));
		when(organizationMock.getCompanyId()).thenReturn(2);
		when(organizationMock.getOrgId()).thenReturn(ORGANIZATION_NUMBER);

		final var result = service.applySorting(MUNICIPALITY_ID, ORGANIZATION_NUMBER, List.of(checklist));

		assertThat(result).extracting(Checklist::getPhases).satisfiesExactly(phases -> {
			assertThat(phases.getFirst().getSortOrder()).isEqualTo(1);
			assertThat(phases.getFirst().getTasks().getFirst().getSortOrder()).isEqualTo(2);
		});

		verify(sortorderRepositoryMock).findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
	}

	@Test
	void applySortingForChecklistWhenNoMatchingOrganization() {
		final var checklist = Checklist.builder()
			.withPhases(
				List.of(Phase.builder()
					.withId(COMPONENT_ID1)
					.withSortOrder(1)
					.withTasks(
						List.of(Task.builder()
							.withId(COMPONENT_ID2)
							.withSortOrder(2)
							.build()))
					.build()))
			.build();

		when(mdViewerClientMock.getCompanies()).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(mdViewerClientMock.getOrganizationsForCompany(1)).thenReturn(Collections.emptyList());
		when(mdViewerClientMock.getOrganizationsForCompany(2)).thenReturn(List.of(organizationMock));

		final var result = service.applySorting(MUNICIPALITY_ID, ORGANIZATION_NUMBER, List.of(checklist));

		assertThat(result).extracting(Checklist::getPhases).satisfiesExactly(phases -> {
			assertThat(phases.getFirst().getSortOrder()).isEqualTo(1);
			assertThat(phases.getFirst().getTasks().getFirst().getSortOrder()).isEqualTo(2);
		});

		verify(sortorderRepositoryMock, never()).findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
	}

	private static List<SortorderEntity> createCustomorder() {
		return List.of(
			SortorderEntity.builder()
				.withComponentId(COMPONENT_ID1)
				.withComponentType(ComponentType.PHASE)
				.withPosition(10)
				.build(),
			SortorderEntity.builder()
				.withComponentId(COMPONENT_ID2)
				.withComponentType(ComponentType.TASK)
				.withPosition(20)
				.build());
	}
}
