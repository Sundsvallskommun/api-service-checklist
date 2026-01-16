package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.TestObjectFactory.generateSortorderRequest;

import generated.se.sundsvall.company.Organization;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import se.sundsvall.checklist.integration.company.CompanyIntegration;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.SortorderEntity;
import se.sundsvall.checklist.integration.db.model.enums.ComponentType;
import se.sundsvall.checklist.integration.db.repository.SortorderRepository;

@ExtendWith(MockitoExtension.class)
class SortorderServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final int ORGANIZATION_NUMBER = 123456;
	private static final String COMPONENT_ID1 = UUID.randomUUID().toString();
	private static final String COMPONENT_ID2 = UUID.randomUUID().toString();

	@Mock
	private SortorderRepository sortorderRepositoryMock;

	@Mock
	private CompanyIntegration companyIntegrationMock;

	@Mock
	private Organization companyMock;

	@Mock
	private Organization organizationMock;

	@InjectMocks
	private SortorderService service;

	@Captor
	private ArgumentCaptor<List<SortorderEntity>> saveAllCaptor;

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

	private static List<SortorderEntity> createCustomorderForTasks() {
		return List.of(
			SortorderEntity.builder()
				.withComponentId(COMPONENT_ID1)
				.withComponentType(ComponentType.TASK)
				.withPosition(20)
				.build(),
			SortorderEntity.builder()
				.withComponentId(COMPONENT_ID2)
				.withComponentType(ComponentType.TASK)
				.withPosition(10)
				.build());
	}

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

		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 1)).thenReturn(List.of(organizationMock));
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

		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 1)).thenReturn(List.of(organizationMock));
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
	void applySortingForChecklists() {
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

		when(companyIntegrationMock.getCompanies(MUNICIPALITY_ID)).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 1)).thenReturn(Collections.emptyList());
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 2)).thenReturn(List.of(organizationMock));
		when(organizationMock.getCompanyId()).thenReturn(2);
		when(organizationMock.getOrgId()).thenReturn(ORGANIZATION_NUMBER);
		when(sortorderRepositoryMock.findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(createCustomorder());

		final var result = service.applySortingToChecklists(MUNICIPALITY_ID, ORGANIZATION_NUMBER, List.of(checklist));

		assertThat(result).extracting(Checklist::getPhases).satisfiesExactly(phases -> {
			assertThat(phases.getFirst().getSortOrder()).isEqualTo(10);
			assertThat(phases.getFirst().getTasks().getFirst().getSortOrder()).isEqualTo(20);
		});
	}

	@Test
	void applySortingForChecklistsWhenNoCustomSort() {
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

		when(companyIntegrationMock.getCompanies(MUNICIPALITY_ID)).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 1)).thenReturn(Collections.emptyList());
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 2)).thenReturn(List.of(organizationMock));
		when(organizationMock.getCompanyId()).thenReturn(2);
		when(organizationMock.getOrgId()).thenReturn(ORGANIZATION_NUMBER);

		final var result = service.applySortingToChecklists(MUNICIPALITY_ID, ORGANIZATION_NUMBER, List.of(checklist));

		assertThat(result).extracting(Checklist::getPhases).satisfiesExactly(phases -> {
			assertThat(phases.getFirst().getSortOrder()).isEqualTo(1);
			assertThat(phases.getFirst().getTasks().getFirst().getSortOrder()).isEqualTo(2);
		});

		verify(sortorderRepositoryMock).findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
	}

	@Test
	void applySortingForChecklistsWhenNoMatchingOrganization() {
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

		when(companyIntegrationMock.getCompanies(MUNICIPALITY_ID)).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 1)).thenReturn(Collections.emptyList());
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 2)).thenReturn(List.of(organizationMock));

		final var result = service.applySortingToChecklists(MUNICIPALITY_ID, ORGANIZATION_NUMBER, List.of(checklist));

		assertThat(result).extracting(Checklist::getPhases).satisfiesExactly(phases -> {
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

		when(companyIntegrationMock.getCompanies(MUNICIPALITY_ID)).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 1)).thenReturn(Collections.emptyList());
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 2)).thenReturn(List.of(organizationMock));
		when(organizationMock.getCompanyId()).thenReturn(2);
		when(organizationMock.getParentId()).thenReturn(1);
		when(organizationMock.getOrgId()).thenReturn(ORGANIZATION_NUMBER);
		when(sortorderRepositoryMock.findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(createCustomorder());

		final var result = service.applySortingToChecklist(MUNICIPALITY_ID, ORGANIZATION_NUMBER, checklist);

		assertThat(result.getPhases()).satisfiesExactly(phase -> {
			assertThat(phase.getSortOrder()).isEqualTo(10);
			assertThat(phase.getTasks().getFirst().getSortOrder()).isEqualTo(20);
		});
	}

	@Test
	void applySortingToTasks() {
		final var tasks = List.of(
			Task.builder()
				.withId(COMPONENT_ID1)
				.withSortOrder(1)
				.build(),
			Task.builder()
				.withId(COMPONENT_ID2)
				.withSortOrder(2)
				.build());

		when(companyIntegrationMock.getCompanies(MUNICIPALITY_ID)).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 1)).thenReturn(Collections.emptyList());
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 2)).thenReturn(List.of(organizationMock));
		when(organizationMock.getCompanyId()).thenReturn(2);
		when(organizationMock.getOrgId()).thenReturn(ORGANIZATION_NUMBER);
		when(sortorderRepositoryMock.findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(createCustomorderForTasks());

		final var result = service.applySortingToTasks(MUNICIPALITY_ID, ORGANIZATION_NUMBER, tasks);

		assertThat(result).hasSize(2).satisfiesExactly(task -> {
			assertThat(task.getSortOrder()).isEqualTo(10);
			assertThat(task.getId()).isEqualTo(COMPONENT_ID2);
		}, task -> {
			assertThat(task.getSortOrder()).isEqualTo(20);
			assertThat(task.getId()).isEqualTo(COMPONENT_ID1);
		});
	}

	@Test
	void applySortingToTasksWhenNoCustomSort() {
		final var tasks = List.of(
			Task.builder()
				.withId(COMPONENT_ID1)
				.withSortOrder(1)
				.build(),
			Task.builder()
				.withId(COMPONENT_ID2)
				.withSortOrder(2)
				.build());

		when(companyIntegrationMock.getCompanies(MUNICIPALITY_ID)).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 1)).thenReturn(Collections.emptyList());
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 2)).thenReturn(List.of(organizationMock));
		when(organizationMock.getCompanyId()).thenReturn(2);
		when(organizationMock.getOrgId()).thenReturn(ORGANIZATION_NUMBER);

		final var result = service.applySortingToTasks(MUNICIPALITY_ID, ORGANIZATION_NUMBER, tasks);

		assertThat(result).hasSize(2).satisfiesExactly(task -> {
			assertThat(task.getSortOrder()).isEqualTo(1);
			assertThat(task.getId()).isEqualTo(COMPONENT_ID1);
		}, task -> {
			assertThat(task.getSortOrder()).isEqualTo(2);
			assertThat(task.getId()).isEqualTo(COMPONENT_ID2);
		});
	}

	@Test
	void applySortingToTasksWhenNoMatchingOrganization() {
		final var tasks = List.of(
			Task.builder()
				.withId(COMPONENT_ID1)
				.withSortOrder(1)
				.build(),
			Task.builder()
				.withId(COMPONENT_ID2)
				.withSortOrder(2)
				.build());

		when(companyIntegrationMock.getCompanies(MUNICIPALITY_ID)).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 1)).thenReturn(Collections.emptyList());
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 2)).thenReturn(List.of(organizationMock));

		final var result = service.applySortingToTasks(MUNICIPALITY_ID, ORGANIZATION_NUMBER, tasks);

		assertThat(result).hasSize(2).satisfiesExactly(task -> {
			assertThat(task.getSortOrder()).isEqualTo(1);
			assertThat(task.getId()).isEqualTo(COMPONENT_ID1);
		}, task -> {
			assertThat(task.getSortOrder()).isEqualTo(2);
			assertThat(task.getId()).isEqualTo(COMPONENT_ID2);
		});
	}

	@Test
	void applySortingToTask() {
		final var task = Task.builder()
			.withId(COMPONENT_ID1)
			.withSortOrder(1)
			.build();

		when(companyIntegrationMock.getCompanies(MUNICIPALITY_ID)).thenReturn(List.of(companyMock, companyMock));
		when(companyMock.getCompanyId()).thenReturn(1, 2);
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 1)).thenReturn(Collections.emptyList());
		when(companyIntegrationMock.getOrganizationsForCompany(MUNICIPALITY_ID, 2)).thenReturn(List.of(organizationMock));
		when(organizationMock.getCompanyId()).thenReturn(2);
		when(organizationMock.getOrgId()).thenReturn(ORGANIZATION_NUMBER);
		when(sortorderRepositoryMock.findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(createCustomorderForTasks());

		final var result = service.applySortingToTask(MUNICIPALITY_ID, ORGANIZATION_NUMBER, task);

		assertThat(result).isSameAs(task);
		assertThat(result.getSortOrder()).isEqualTo(20);
	}

	@Test
	void copySortorderItems() {
		final var newId = UUID.randomUUID().toString();
		final var translationMap = Map.of(newId, COMPONENT_ID1);

		final var sortorderEntities = List.of(
			SortorderEntity.builder()
				.withComponentId(COMPONENT_ID1)
				.withOrganizationNumber(ORGANIZATION_NUMBER)
				.withMunicipalityId(MUNICIPALITY_ID)
				.withPosition(121)
				.build(),
			SortorderEntity.builder()
				.withComponentId(COMPONENT_ID1)
				.withOrganizationNumber(ORGANIZATION_NUMBER + 1)
				.withMunicipalityId(MUNICIPALITY_ID)
				.withPosition(212)
				.build());

		when(sortorderRepositoryMock.findAllByComponentId(COMPONENT_ID1)).thenReturn(sortorderEntities);

		service.copySortorderItems(translationMap);

		verify(sortorderRepositoryMock).findAllByComponentId(COMPONENT_ID1);
		verify(sortorderRepositoryMock).saveAll(saveAllCaptor.capture());

		assertThat(saveAllCaptor.getValue()).hasSize(2)
			.allSatisfy(entity -> {
				assertThat(entity.getComponentId()).isEqualTo(newId);
				assertThat(entity.getComponentType()).isEqualTo(ComponentType.TASK);
				assertThat(entity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
			})
			.satisfiesExactlyInAnyOrder(entity -> {
				assertThat(entity.getOrganizationNumber()).isEqualTo(ORGANIZATION_NUMBER);
				assertThat(entity.getPosition()).isEqualTo(121);
			}, entity -> {
				assertThat(entity.getOrganizationNumber()).isEqualTo(ORGANIZATION_NUMBER + 1);
				assertThat(entity.getPosition()).isEqualTo(212);
			});
	}
}
