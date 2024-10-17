package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.TestObjectFactory.createDelegateEntity;
import static se.sundsvall.checklist.TestObjectFactory.createEmployeeChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.generatePortalPersonData;
import static se.sundsvall.checklist.integration.employee.EmployeeFilterBuilder.buildUuidEmployeeFilter;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zalando.problem.Problem;

import generated.se.sundsvall.employee.Employee;
import se.sundsvall.checklist.integration.db.EmployeeChecklistIntegration;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.DelegateRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.employee.EmployeeIntegration;

@ExtendWith(MockitoExtension.class)
class DelegationServiceTest {

	@Mock
	private EmployeeChecklistRepository mockEmployeeChecklistRepository;

	@Mock
	private DelegateRepository mockDelegateRepository;

	@Mock
	private EmployeeIntegration mockEmployeeIntegration;

	@Mock
	private EmployeeChecklistIntegration mockEmployeeChecklistIntegration;

	@Mock
	private CustomTaskRepository mockCustomTaskRepository;

	@InjectMocks
	private DelegationService service;

	@BeforeEach
	void initializeFields() {
		ReflectionTestUtils.setField(service, "employeeInformationUpdateInterval", Duration.ofDays(1));
	}

	@AfterEach
	void finalAssertsAndVerifications() {
		verifyNoMoreInteractions(mockEmployeeChecklistRepository, mockEmployeeIntegration, mockDelegateRepository, mockEmployeeChecklistIntegration, mockCustomTaskRepository);
	}

	@Test
	void delegateEmployeeChecklistTest() {
		final var email = "email";
		final var employeeChecklist = createEmployeeChecklistEntity();
		final var portalPersonData = generatePortalPersonData(UUID.randomUUID());

		when(mockEmployeeChecklistRepository.findById(employeeChecklist.getId())).thenReturn(Optional.of(employeeChecklist));
		when(mockEmployeeIntegration.getEmployeeByEmail(email)).thenReturn(Optional.of(portalPersonData));
		when(mockDelegateRepository.findByEmployeeChecklistAndEmail(employeeChecklist, email)).thenReturn(Optional.empty());

		service.delegateEmployeeChecklist(employeeChecklist.getId(), email);

		verify(mockEmployeeChecklistRepository).findById(employeeChecklist.getId());
		verify(mockEmployeeIntegration).getEmployeeByEmail(email);
		verify(mockDelegateRepository).findByEmployeeChecklistAndEmail(employeeChecklist, email);
		verify(mockEmployeeChecklistRepository).save(employeeChecklist);
	}

	@Test
	void delegateEmployeeChecklistAlreadyDelegatedTest() {
		final var email = "email";
		final var employeeChecklist = createEmployeeChecklistEntity();
		final var portalPersonData = generatePortalPersonData(UUID.randomUUID());
		final var delegateEntity = createDelegateEntity();

		when(mockEmployeeChecklistRepository.findById(employeeChecklist.getId())).thenReturn(Optional.of(employeeChecklist));
		when(mockEmployeeIntegration.getEmployeeByEmail(email)).thenReturn(Optional.of(portalPersonData));
		when(mockDelegateRepository.findByEmployeeChecklistAndEmail(employeeChecklist, email)).thenReturn(Optional.of(delegateEntity));

		assertThatThrownBy(() -> service.delegateEmployeeChecklist(employeeChecklist.getId(), email))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", CONFLICT)
			.hasFieldOrPropertyWithValue("title", "Conflict")
			.hasFieldOrPropertyWithValue("detail", "Employee checklist with id " + employeeChecklist.getId() + " is already delegated to " + email);

		verify(mockEmployeeChecklistRepository).findById(employeeChecklist.getId());
		verify(mockEmployeeIntegration).getEmployeeByEmail(email);
		verify(mockDelegateRepository).findByEmployeeChecklistAndEmail(employeeChecklist, email);
		verify(mockDelegateRepository, never()).save(any());
	}

	@Test
	void delegateEmployeeChecklistNotFoundTest() {
		final var employeeChecklistId = "123";
		final var email = "email";

		when(mockEmployeeChecklistRepository.findById(employeeChecklistId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delegateEmployeeChecklist(employeeChecklistId, email))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("title", "Not Found")
			.hasFieldOrPropertyWithValue("detail", "Employee checklist with id 123 was not found.");

		verify(mockEmployeeChecklistRepository).findById(employeeChecklistId);
	}

	@Test
	void delegateEmployeeChecklistEmployeeNotFoundTest() {
		final var email = "test@test.com";
		final var employeeChecklist = createEmployeeChecklistEntity();

		when(mockEmployeeChecklistRepository.findById(employeeChecklist.getId())).thenReturn(Optional.of(employeeChecklist));
		when(mockEmployeeIntegration.getEmployeeByEmail(email)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delegateEmployeeChecklist(employeeChecklist.getId(), email))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("title", "Not Found")
			.hasFieldOrPropertyWithValue("detail", "Employee with email test@test.com was not found.");

		verify(mockEmployeeChecklistRepository).findById(employeeChecklist.getId());
		verify(mockEmployeeIntegration).getEmployeeByEmail(email);
	}

	@Test
	void fetchDelegatedEmployeeChecklistsByUserNameTest() {
		final var userName = "userName";
		final var delegateEntity = createDelegateEntity();

		when(mockDelegateRepository.findAllByUserName(userName)).thenReturn(List.of(delegateEntity));

		final var result = service.fetchDelegatedEmployeeChecklistsByUserName(userName);

		assertThat(result).isNotNull();
		assertThat(result.getEmployeeChecklists()).hasSize(1);

		verify(mockDelegateRepository).findAllByUserName(userName);
		verify(mockCustomTaskRepository).findAllByEmployeeChecklistId(delegateEntity.getEmployeeChecklist().getId());
		verify(mockEmployeeChecklistIntegration).fetchDelegateEmails(delegateEntity.getEmployeeChecklist().getId());
	}

	@Test
	void fetchDelegatedEmployeeChecklistsByUserNameWhenEmployeeInformationNeedsUpdateTest() {
		final var userName = "userName";
		final var delegateEntity = createDelegateEntity();
		final var filter = buildUuidEmployeeFilter(delegateEntity.getEmployeeChecklist().getEmployee().getId());
		final var employee = new Employee();
		delegateEntity.getEmployeeChecklist().getEmployee().setUpdated(OffsetDateTime.now().minusDays(1).minusNanos(1));

		when(mockDelegateRepository.findAllByUserName(userName)).thenReturn(List.of(delegateEntity));
		when(mockEmployeeIntegration.getEmployeeInformation(filter)).thenReturn(List.of(employee));

		final var result = service.fetchDelegatedEmployeeChecklistsByUserName(userName);

		assertThat(result).isNotNull();
		assertThat(result.getEmployeeChecklists()).hasSize(1);

		verify(mockDelegateRepository).findAllByUserName(userName);
		verify(mockEmployeeIntegration).getEmployeeInformation(filter);
		verify(mockEmployeeChecklistIntegration).updateEmployeeInformation(delegateEntity.getEmployeeChecklist().getEmployee(), employee);
		verify(mockCustomTaskRepository).findAllByEmployeeChecklistId(delegateEntity.getEmployeeChecklist().getId());
		verify(mockEmployeeChecklistIntegration).fetchDelegateEmails(delegateEntity.getEmployeeChecklist().getId());
	}

	@Test
	void fetchDelegatedEmployeeChecklistsByUserNameWhenEmptyTest() {
		final var userName = "userName";

		when(mockDelegateRepository.findAllByUserName(userName)).thenReturn(List.of());

		final var result = service.fetchDelegatedEmployeeChecklistsByUserName(userName);

		assertThat(result).isNotNull();
		assertThat(result.getEmployeeChecklists()).isEmpty();

		verify(mockDelegateRepository).findAllByUserName(userName);
	}

	@Test
	void deleteEmployeeChecklistDelegationTest() {
		final var employeeChecklist = createEmployeeChecklistEntity();

		when(mockEmployeeChecklistRepository.findById(anyString())).thenReturn(Optional.of(employeeChecklist));
		when(mockDelegateRepository.existsByEmployeeChecklistAndEmail(employeeChecklist, "email")).thenReturn(true);

		service.removeEmployeeChecklistDelegation(employeeChecklist.getId(), "email");

		verify(mockEmployeeChecklistRepository).findById(employeeChecklist.getId());
		verify(mockDelegateRepository).existsByEmployeeChecklistAndEmail(employeeChecklist, "email");
		verify(mockDelegateRepository).deleteByEmployeeChecklistAndEmail(employeeChecklist, "email");
	}

	@Test
	void deleteEmployeeChecklistDelegationNotFoundTest() {
		final var employeeChecklistId = "123";
		final var email = "email";
		when(mockEmployeeChecklistRepository.findById(anyString())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.removeEmployeeChecklistDelegation(employeeChecklistId, email))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("title", "Not Found")
			.hasFieldOrPropertyWithValue("detail", "Employee checklist with id 123 was not found.");

		verify(mockEmployeeChecklistRepository).findById(employeeChecklistId);
	}
}
