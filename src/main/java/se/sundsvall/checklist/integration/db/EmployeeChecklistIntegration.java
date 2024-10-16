package se.sundsvall.checklist.integration.db;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.NOT_ACCEPTABLE;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.ACTIVE;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toCustomFulfilmentEntity;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toCustomTaskEntity;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toEmployeeChecklistEntity;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toFulfilmentEntity;
import static se.sundsvall.checklist.service.mapper.OrganizationMapper.toEmployeeEntity;
import static se.sundsvall.checklist.service.mapper.OrganizationMapper.toManagerEntity;
import static se.sundsvall.checklist.service.mapper.OrganizationMapper.toOrganizationEntity;
import static se.sundsvall.checklist.service.mapper.OrganizationMapper.updateEmployeeEntity;
import static se.sundsvall.checklist.service.util.ServiceUtils.getMainEmployment;
import static se.sundsvall.checklist.service.util.StringUtils.toReadableString;
import static se.sundsvall.checklist.service.util.VerificationUtils.verifyUnlockedEmployeeChecklist;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import generated.se.sundsvall.employee.Manager;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistPaginatedResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.api.specification.EmployeeCheclistFilterSpecification;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.DelegateRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeRepository;
import se.sundsvall.checklist.integration.db.repository.ManagerRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.service.OrganizationTree;
import se.sundsvall.checklist.service.OrganizationTree.OrganizationLine;
import se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;

@Component
public class EmployeeChecklistIntegration {
	private static final String EMPLOYEE_HAS_CHECKLIST = "Employee with loginname %s already has an employee checklist.";
	private static final String EMPLOYEE_SUCCESSFULLY_PROCESSED = "Employee with loginname %s processed successfully.";
	private static final String EMPLOYMENT_TYPE_NOT_VALID_FOR_CHECKLIST = "Employee with loginname %s does not have an employment type that validates for creating an employee checklist.";
	private static final String NO_MATCHING_CHECKLIST_FOUND = "No %s checklist was found for any id in the organization tree for employee %s. Search has been performed for id %s.";
	private static final String NO_MATCHING_EMPLOYEE_CHECKLIST_FOUND = "Employee checklist with id %s was not found.";
	private static final String NO_MATCHING_EMPLOYEE_CHECKLIST_PHASE_FOUND = "Phase with id %s was not found in employee checklist with id %s.";
	private static final String NO_FULFILMENT_INFORMATION_FOUND = "No fulfilment information found for task with id %s in employee checklist with id %s.";
	private static final List<String> VALID_EMPLOYMENT_FORMS_FOR_CHECKLIST = List.of("1", "2", "9"); // Permanent employment ("1"), temporary monthly paid employment ("2") and probationary employment ("9")

	private final DelegateRepository delegateRepository;
	private final EmployeeRepository employeeRepository;
	private final ManagerRepository managerRepository;
	private final EmployeeChecklistRepository employeeChecklistRepository;
	private final OrganizationRepository organizationRepository;
	private final CustomTaskRepository customTaskRepository;

	public EmployeeChecklistIntegration(
		final DelegateRepository delegateRepository,
		final EmployeeRepository employeeRepository,
		final ManagerRepository managerRepository,
		final EmployeeChecklistRepository employeeChecklistRepository,
		final OrganizationRepository organizationRepository,
		final CustomTaskRepository customTaskRepository) {

		this.delegateRepository = delegateRepository;
		this.employeeRepository = employeeRepository;
		this.managerRepository = managerRepository;
		this.employeeChecklistRepository = employeeChecklistRepository;
		this.organizationRepository = organizationRepository;
		this.customTaskRepository = customTaskRepository;
	}

	public EmployeeChecklistPaginatedResponse fetchPaginatedEmployeeChecklistsByString(final EmployeeCheclistFilterSpecification specification, final Pageable pageable) {
		final var matches = employeeChecklistRepository.findAll(specification, pageable);
		final var employeeChecklists = matches.getContent();

		return EmployeeChecklistPaginatedResponse.builder()
			.withPagingAndSortingMetaData(PagingAndSortingMetaData.create().withPageData(matches))
			.withEmployeeChecklists(employeeChecklists.stream().map(EmployeeChecklistMapper::toEmployeeChecklistDTO).toList())
			.build();
	}

	public Optional<EmployeeChecklistEntity> fetchOptionalEmployeeChecklist(String userId) {
		return ofNullable(employeeChecklistRepository.findByEmployeeUserName(userId));
	}

	public List<EmployeeChecklistEntity> fetchEmployeeChecklistsForManager(String userId) {
		return employeeChecklistRepository.findAllByEmployeeManagerUserName(userId);
	}

	@Transactional
	public void updateEmployeeInformation(EmployeeEntity employeeEntity, Employee employee) {
		updateEmployeeEntity(employeeEntity, employee);
		employeeEntity.setManager(retrieveManagerEntity(getMainEmployment(employee).getManager()));
		employeeRepository.save(employeeEntity);
	}

	public List<String> fetchDelegateEmails(String employeeChecklistId) {
		final var delegates = delegateRepository.findAllByEmployeeChecklistId(employeeChecklistId)
			.stream()
			.map(DelegateEntity::getEmail)
			.toList();

		return delegates.isEmpty() ? null : delegates;
	}

	@Transactional
	public EmployeeChecklistEntity updateAllTasksInPhase(String employeeChecklistId, String phaseId, EmployeeChecklistPhaseUpdateRequest request) {
		final var employeeChecklist = employeeChecklistRepository.findById(employeeChecklistId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NO_MATCHING_EMPLOYEE_CHECKLIST_FOUND.formatted(employeeChecklistId)));

		verifyUnlockedEmployeeChecklist(employeeChecklist);

		if (isNull(request.getTasksFulfilmentStatus())) {
			return employeeChecklist;
		}

		// Update of all common tasks (if such exists) in phase
		employeeChecklist.getChecklist().getPhases().stream()
			.filter(phase -> Objects.equals(phase.getId(), phaseId))
			.findAny()
			.map(PhaseEntity::getTasks)
			.ifPresent(tasks -> tasks.forEach(task -> updateCommonTask(employeeChecklist, task, request.getTasksFulfilmentStatus())));

		// Update of all custom tasks (if such exists) in phase
		employeeChecklist.getCustomTasks().stream()
			.filter(task -> Objects.equals(task.getPhase().getId(), phaseId))
			.forEach(task -> updateCustomTask(employeeChecklist, task, request.getTasksFulfilmentStatus()));

		return employeeChecklistRepository.save(employeeChecklist);
	}

	private void updateCommonTask(EmployeeChecklistEntity employeeChecklist, TaskEntity task, FulfilmentStatus fulfilmentStatus) {
		employeeChecklist.getFulfilments().stream()
			.filter(fulfilment -> Objects.equals(task, fulfilment.getTask()))
			.findAny()
			.ifPresentOrElse(
				fulfilment -> fulfilment.setCompleted(fulfilmentStatus),
				() -> employeeChecklist.getFulfilments().add(toFulfilmentEntity(employeeChecklist, task, fulfilmentStatus)));
	}

	private void updateCustomTask(EmployeeChecklistEntity employeeChecklist, CustomTaskEntity customTask, FulfilmentStatus fulfilmentStatus) {
		employeeChecklist.getCustomFulfilments().stream()
			.filter(fulfilment -> Objects.equals(customTask, fulfilment.getCustomTask()))
			.findAny()
			.ifPresentOrElse(
				fulfilment -> fulfilment.setCompleted(fulfilmentStatus),
				() -> employeeChecklist.getCustomFulfilments().add(toCustomFulfilmentEntity(employeeChecklist, customTask, fulfilmentStatus)));
	}

	public EmployeeChecklistEntity fetchEmployeeChecklist(String employeeChecklistId) {
		return employeeChecklistRepository.findById(employeeChecklistId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NO_MATCHING_EMPLOYEE_CHECKLIST_FOUND.formatted(employeeChecklistId)));
	}

	@Transactional
	public FulfilmentEntity updateCommonTaskFulfilment(String employeeChecklistId, String taskId, EmployeeChecklistTaskUpdateRequest request) {
		final var employeeChecklist = fetchEmployeeChecklist(employeeChecklistId);

		employeeChecklist.getChecklist().getPhases().stream()
			.map(PhaseEntity::getTasks)
			.flatMap(List::stream)
			.filter(task -> Objects.equals(task.getId(), taskId))
			.findAny()
			.ifPresent(task -> {
				updateCommonTask(employeeChecklist, task, request);
				employeeChecklistRepository.save(employeeChecklist);
			});

		return employeeChecklist.getFulfilments().stream()
			.filter(fulfilment -> Objects.equals(fulfilment.getTask().getId(), taskId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NO_FULFILMENT_INFORMATION_FOUND.formatted(taskId, employeeChecklistId)));
	}

	private void updateCommonTask(EmployeeChecklistEntity employeeChecklist, TaskEntity task, EmployeeChecklistTaskUpdateRequest request) {
		employeeChecklist.getFulfilments().stream()
			.filter(fulfilment -> Objects.equals(task, fulfilment.getTask()))
			.findAny()
			.ifPresentOrElse(
				fulfilment -> {
					ofNullable(request.getFulfilmentStatus()).ifPresent(fulfilment::setCompleted);
					ofNullable(request.getResponseText()).ifPresent(fulfilment::setResponseText);
				}, () -> employeeChecklist.getFulfilments().add(toFulfilmentEntity(employeeChecklist, task, request.getFulfilmentStatus(), request.getResponseText())));
	}

	@Transactional
	public CustomFulfilmentEntity updateCustomTaskFulfilment(String employeeChecklistId, String taskId, EmployeeChecklistTaskUpdateRequest request) {
		final var employeeChecklist = fetchEmployeeChecklist(employeeChecklistId);

		employeeChecklist.getCustomTasks().stream()
			.filter(task -> Objects.equals(task.getId(), taskId))
			.findAny()
			.ifPresent(task -> {
				updateCustomTask(employeeChecklist, task, request);
				employeeChecklistRepository.save(employeeChecklist);
			});

		return employeeChecklist.getCustomFulfilments().stream()
			.filter(fulfilment -> Objects.equals(fulfilment.getCustomTask().getId(), taskId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NO_FULFILMENT_INFORMATION_FOUND.formatted(taskId, employeeChecklistId)));
	}

	private void updateCustomTask(EmployeeChecklistEntity employeeChecklist, CustomTaskEntity customTask, EmployeeChecklistTaskUpdateRequest request) {
		employeeChecklist.getCustomFulfilments().stream()
			.filter(fulfilment -> Objects.equals(customTask, fulfilment.getCustomTask()))
			.findAny()
			.ifPresentOrElse(
				fulfilment -> {
					ofNullable(request.getFulfilmentStatus()).ifPresent(fulfilment::setCompleted);
					ofNullable(request.getResponseText()).ifPresent(fulfilment::setResponseText);
				}, () -> employeeChecklist.getCustomFulfilments().add(toCustomFulfilmentEntity(employeeChecklist, customTask, request.getFulfilmentStatus(), request.getResponseText())));
	}

	@Transactional
	public void deleteEmployeeChecklist(String employeeChecklistId) {
		final var employeeChecklist = fetchEmployeeChecklist(employeeChecklistId);

		delegateRepository.deleteByEmployeeChecklist(employeeChecklist);
		employeeChecklistRepository.deleteById(employeeChecklistId);
	}

	@Transactional
	public CustomTaskEntity createCustomTask(String employeeChecklistId, String phaseId, CustomTaskCreateRequest request) {
		final var employeeChecklist = fetchEmployeeChecklist(employeeChecklistId);

		final var phaseEntity = employeeChecklist.getChecklist().getPhases().stream()
			.filter(phase -> Objects.equals(phase.getId(), phaseId))
			.findAny()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NO_MATCHING_EMPLOYEE_CHECKLIST_PHASE_FOUND.formatted(phaseId, employeeChecklistId)));

		final var customTaskEntity = toCustomTaskEntity(employeeChecklist, phaseEntity, request);
		customTaskRepository.save(customTaskEntity);

		employeeChecklist.getCustomTasks().add(customTaskEntity);
		employeeChecklistRepository.save(employeeChecklist);

		return customTaskEntity;
	}

	/**
	 * Method for creating an employee checklist based on nearest organizational checklist.
	 *
	 * @param employee the employee to onboard
	 * @param orgTree  the organization tree for the employee
	 * @return status of the process of creating the employee checklist
	 * @throws ThrowableProblem if error occurs when processing employee
	 */
	@Transactional
	public String initiateEmployee(Employee employee, OrganizationTree orgTree) {
		if (employeeRepository.existsById(employee.getPersonId().toString())) {
			return EMPLOYEE_HAS_CHECKLIST.formatted(employee.getLoginname());
		}
		if (!isValidForChecklist(employee)) {
			throw Problem.valueOf(NOT_ACCEPTABLE, EMPLOYMENT_TYPE_NOT_VALID_FOR_CHECKLIST.formatted(employee.getLoginname()));
		}
		final var employment = getMainEmployment(employee);
		final var employeeEntity = toEmployeeEntity(employee);

		// Attach existing organizational units to the employee (or create new unit if not present in the persistant layer)
		employeeEntity.setCompany(retrieveOrganizationEntity(employment.getCompanyId(), null));
		employeeEntity.setDepartment(retrieveOrganizationEntity(employment.getOrgId(), employment.getOrgName()));

		// Attach existing manager to the employee (or create new manager if not present in the persistant layer)
		employeeEntity.setManager(retrieveManagerEntity(employment.getManager()));

		// Persist employee and create checklist for him/her
		final var persistedEmployee = employeeRepository.save(employeeEntity);
		initiateEmployeeChecklist(persistedEmployee, orgTree);

		return EMPLOYEE_SUCCESSFULLY_PROCESSED.formatted(employee.getLoginname());
	}

	private boolean isValidForChecklist(Employee employee) {
		return Stream.of(getMainEmployment(employee))
			.map(Employment::getFormOfEmploymentId)
			.filter(StringUtils::isNotBlank)
			.anyMatch(VALID_EMPLOYMENT_FORMS_FOR_CHECKLIST::contains);
	}

	private ManagerEntity retrieveManagerEntity(Manager manager) {
		return managerRepository.findById(manager.getPersonId().toString())
			.orElse(toManagerEntity(manager));
	}

	private OrganizationEntity retrieveOrganizationEntity(int organizationNumber, String organizationName) {
		return ofNullable(organizationRepository.findOneByOrganizationNumber(organizationNumber))
			.orElse(toOrganizationEntity(organizationNumber, organizationName));
	}

	private void initiateEmployeeChecklist(EmployeeEntity employeeEntity, OrganizationTree orgTree) {
		final var employeeRole = employeeEntity.getRoleType();

		final var checklistEntity = retrieveClosestAvailableChecklist(orgTree.getTree().descendingMap().values().iterator(), employeeRole)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NO_MATCHING_CHECKLIST_FOUND.formatted(employeeRole, employeeEntity.getUserName(),
				toReadableString(orgTree.getTree().values().stream().map(OrganizationLine::getOrgId).toList()))));

		employeeChecklistRepository.save(toEmployeeChecklistEntity(employeeEntity, checklistEntity));
	}

	private Optional<ChecklistEntity> retrieveClosestAvailableChecklist(Iterator<OrganizationLine> organizationIterator, RoleType roleType) {
		if (organizationIterator.hasNext()) {
			return retrieveChecklist(Integer.parseInt(organizationIterator.next().getOrgId()), roleType)
				.or(() -> retrieveClosestAvailableChecklist(organizationIterator, roleType));
		}
		return Optional.empty();
	}

	private Optional<ChecklistEntity> retrieveChecklist(int organizationNumber, RoleType roleType) {
		final var organization = organizationRepository.findOneByOrganizationNumber(organizationNumber);

		if (isNull(organization)) {
			return Optional.empty();
		}

		return ofNullable(organization.getChecklists()).orElse(emptyList()).stream()
			.filter(checklist -> Objects.equals(checklist.getLifeCycle(), ACTIVE))
			.filter(checklist -> Objects.equals(checklist.getRoleType(), roleType))
			.findAny();
	}
}
