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
import static se.sundsvall.checklist.TestObjectFactory.createDelegateEntity;
import static se.sundsvall.checklist.TestObjectFactory.createEmployeeChecklistEntity;
import static se.sundsvall.checklist.TestObjectFactory.generatePortalPersonData;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zalando.problem.Problem;
import se.sundsvall.checklist.integration.db.EmployeeChecklistIntegration;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.DelegateRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.employee.EmployeeIntegration;
import se.sundsvall.checklist.service.model.Employee;

@ExtendWith(MockitoExtension.class)
class DelegationServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";

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

	@Mock
	private SortorderService mockSortorderService;

	@InjectMocks
	private DelegationService service;

	@BeforeEach
	void initializeFields() {
		ReflectionTestUtils.setField(service, "employeeInformationUpdateInterval", Duration.ofDays(1));
	}

	@AfterEach
	void finalAssertsAndVerifications() {
		verifyNoMoreInteractions(mockEmployeeChecklistRepository, mockEmployeeIntegration, mockDelegateRepository, mockEmployeeChecklistIntegration, mockCustomTaskRepository, mockSortorderService);
	}

	@Test
	void delegateEmployeeChecklistTest() {
		final var email = "email";
		final var employeeChecklist = createEmployeeChecklistEntity();
		final var portalPersonData = generatePortalPersonData(UUID.randomUUID());

		when(mockEmployeeChecklistRepository.findByIdAndChecklistsMunicipalityId(employeeChecklist.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(employeeChecklist));
		when(mockEmployeeIntegration.getEmployeeByEmail(MUNICIPALITY_ID, email)).thenReturn(Optional.of(portalPersonData));
		when(mockDelegateRepository.findByEmployeeChecklistAndEmail(employeeChecklist, email)).thenReturn(Optional.empty());

		service.delegateEmployeeChecklist(MUNICIPALITY_ID, employeeChecklist.getId(), email);

		verify(mockEmployeeChecklistRepository).findByIdAndChecklistsMunicipalityId(employeeChecklist.getId(), MUNICIPALITY_ID);
		verify(mockEmployeeIntegration).getEmployeeByEmail(MUNICIPALITY_ID, email);
		verify(mockDelegateRepository).findByEmployeeChecklistAndEmail(employeeChecklist, email);
		verify(mockEmployeeChecklistRepository).save(employeeChecklist);
	}

	@Test
	void delegateEmployeeChecklistAlreadyDelegatedTest() {
		final var email = "email";
		final var employeeChecklist = createEmployeeChecklistEntity();
		final var portalPersonData = generatePortalPersonData(UUID.randomUUID());
		final var delegateEntity = createDelegateEntity();

		when(mockEmployeeChecklistRepository.findByIdAndChecklistsMunicipalityId(employeeChecklist.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(employeeChecklist));
		when(mockEmployeeIntegration.getEmployeeByEmail(MUNICIPALITY_ID, email)).thenReturn(Optional.of(portalPersonData));
		when(mockDelegateRepository.findByEmployeeChecklistAndEmail(employeeChecklist, email)).thenReturn(Optional.of(delegateEntity));

		assertThatThrownBy(() -> service.delegateEmployeeChecklist(MUNICIPALITY_ID, employeeChecklist.getId(), email))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", CONFLICT)
			.hasFieldOrPropertyWithValue("title", "Conflict")
			.hasFieldOrPropertyWithValue("detail", "Employee checklist with id " + employeeChecklist.getId() + " is already delegated to " + email);

		verify(mockEmployeeChecklistRepository).findByIdAndChecklistsMunicipalityId(employeeChecklist.getId(), MUNICIPALITY_ID);
		verify(mockEmployeeIntegration).getEmployeeByEmail(MUNICIPALITY_ID, email);
		verify(mockDelegateRepository).findByEmployeeChecklistAndEmail(employeeChecklist, email);
		verify(mockDelegateRepository, never()).save(any());
	}

	@Test
	void delegateEmployeeChecklistNotFoundTest() {
		final var employeeChecklistId = "123";
		final var email = "email";

		assertThatThrownBy(() -> service.delegateEmployeeChecklist(MUNICIPALITY_ID, employeeChecklistId, email))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("title", "Not Found")
			.hasFieldOrPropertyWithValue("detail", "Employee checklist with id 123 was not found.");

		verify(mockEmployeeChecklistRepository).findByIdAndChecklistsMunicipalityId(employeeChecklistId, MUNICIPALITY_ID);
	}

	@Test
	void delegateEmployeeChecklistEmployeeNotFoundTest() {
		final var email = "test@test.com";
		final var employeeChecklist = createEmployeeChecklistEntity();

		when(mockEmployeeChecklistRepository.findByIdAndChecklistsMunicipalityId(employeeChecklist.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(employeeChecklist));
		when(mockEmployeeIntegration.getEmployeeByEmail(MUNICIPALITY_ID, email)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delegateEmployeeChecklist(MUNICIPALITY_ID, employeeChecklist.getId(), email))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("title", "Not Found")
			.hasFieldOrPropertyWithValue("detail", "Employee with email test@test.com was not found.");

		verify(mockEmployeeChecklistRepository).findByIdAndChecklistsMunicipalityId(employeeChecklist.getId(), MUNICIPALITY_ID);
		verify(mockEmployeeIntegration).getEmployeeByEmail(MUNICIPALITY_ID, email);
	}

	@Test
	void fetchDelegatedEmployeeChecklistsByUsernameTest() {
		final var username = "username";
		final var delegateEntity = createDelegateEntity();

		when(mockDelegateRepository.findAllByUsername(username)).thenReturn(List.of(delegateEntity));
		when(mockSortorderService.applySorting(any(), any())).thenAnswer(arg -> arg.getArgument(1));

		final var result = service.fetchDelegatedEmployeeChecklistsByUsername(MUNICIPALITY_ID, username);

		assertThat(result).isNotNull();
		assertThat(result.getEmployeeChecklists()).hasSize(1);

		verify(mockDelegateRepository).findAllByUsername(username);
		verify(mockCustomTaskRepository).findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(delegateEntity.getEmployeeChecklist().getId(), MUNICIPALITY_ID);
		verify(mockEmployeeChecklistIntegration).fetchDelegateEmails(delegateEntity.getEmployeeChecklist().getId());
		verify(mockSortorderService).applySorting(any(), any());
	}

	private static Stream<Arguments> fetchDelegatedEmployeeChecklistsByUsernameWhenEmployeeInformationNeedsUpdateProvider() {
		return Stream.of(
			Arguments.of(OffsetDateTime.now().minusDays(1).minusNanos(1)));
	}

	@ParameterizedTest
	@MethodSource("fetchDelegatedEmployeeChecklistsByUsernameWhenEmployeeInformationNeedsUpdateProvider")
	@NullSource
	void fetchDelegatedEmployeeChecklistsByUsernameWhenEmployeeInformationNeedsUpdateTest(OffsetDateTime updated) {
		final var username = "username";
		final var delegateEntity = createDelegateEntity();
		final var employee = Employee.builder().build();
		final var employeeId = delegateEntity.getEmployeeChecklist().getEmployee().getId();
		delegateEntity.getEmployeeChecklist().getEmployee().setUpdated(updated);

		when(mockDelegateRepository.findAllByUsername(username)).thenReturn(List.of(delegateEntity));
		when(mockEmployeeIntegration.getEmployeeInformation(MUNICIPALITY_ID, employeeId)).thenReturn(List.of(employee));
		when(mockSortorderService.applySorting(any(), any())).thenAnswer(arg -> arg.getArgument(1));

		final var result = service.fetchDelegatedEmployeeChecklistsByUsername(MUNICIPALITY_ID, username);

		assertThat(result).isNotNull();
		assertThat(result.getEmployeeChecklists()).hasSize(1);

		verify(mockDelegateRepository).findAllByUsername(username);
		verify(mockEmployeeIntegration).getEmployeeInformation(MUNICIPALITY_ID, employeeId);
		verify(mockEmployeeChecklistIntegration).updateEmployeeInformation(delegateEntity.getEmployeeChecklist().getEmployee(), employee);
		verify(mockCustomTaskRepository).findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(delegateEntity.getEmployeeChecklist().getId(), MUNICIPALITY_ID);
		verify(mockEmployeeChecklistIntegration).fetchDelegateEmails(delegateEntity.getEmployeeChecklist().getId());
		verify(mockSortorderService).applySorting(any(), any());
	}

	@Test
	void fetchDelegatedEmployeeChecklistsByUsernameWhenEmptyTest() {
		final var username = "username";

		when(mockDelegateRepository.findAllByUsername(username)).thenReturn(List.of());

		final var result = service.fetchDelegatedEmployeeChecklistsByUsername(MUNICIPALITY_ID, username);

		assertThat(result).isNotNull();
		assertThat(result.getEmployeeChecklists()).isEmpty();

		verify(mockDelegateRepository).findAllByUsername(username);
	}

	@Test
	void deleteEmployeeChecklistDelegationTest() {
		final var employeeChecklist = createEmployeeChecklistEntity();

		when(mockEmployeeChecklistRepository.findByIdAndChecklistsMunicipalityId(employeeChecklist.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(employeeChecklist));
		when(mockDelegateRepository.existsByEmployeeChecklistAndEmail(employeeChecklist, "email")).thenReturn(true);

		service.removeEmployeeChecklistDelegation(MUNICIPALITY_ID, employeeChecklist.getId(), "email");

		verify(mockEmployeeChecklistRepository).findByIdAndChecklistsMunicipalityId(employeeChecklist.getId(), MUNICIPALITY_ID);
		verify(mockDelegateRepository).existsByEmployeeChecklistAndEmail(employeeChecklist, "email");
		verify(mockDelegateRepository).deleteByEmployeeChecklistAndEmail(employeeChecklist, "email");
	}

	@Test
	void deleteEmployeeChecklistDelegationNotFoundTest() {
		final var employeeChecklistId = "123";
		final var email = "email";

		assertThatThrownBy(() -> service.removeEmployeeChecklistDelegation(MUNICIPALITY_ID, employeeChecklistId, email))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("title", "Not Found")
			.hasFieldOrPropertyWithValue("detail", "Employee checklist with id 123 was not found.");

		verify(mockEmployeeChecklistRepository).findByIdAndChecklistsMunicipalityId(employeeChecklistId, MUNICIPALITY_ID);
	}
}
