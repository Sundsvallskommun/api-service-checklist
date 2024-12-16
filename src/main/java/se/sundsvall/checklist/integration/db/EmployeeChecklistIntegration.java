package se.sundsvall.checklist.integration.db;

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

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import generated.se.sundsvall.employee.Manager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.api.model.Mentor;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomFulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.MentorEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.DelegateRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeRepository;
import se.sundsvall.checklist.integration.db.repository.ManagerRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.checklist.integration.db.repository.PhaseRepository;
import se.sundsvall.checklist.service.OrganizationTree;
import se.sundsvall.checklist.service.OrganizationTree.OrganizationLine;

@Component
public class EmployeeChecklistIntegration {
	private static final String EMPLOYEE_HAS_CHECKLIST = "Employee with loginname %s already has an employee checklist.";
	private static final String EMPLOYEE_SUCCESSFULLY_PROCESSED = "Employee with loginname %s processed successfully.";
	private static final String EMPLOYMENT_TYPE_NOT_VALID_FOR_CHECKLIST = "Employee with loginname %s does not have an employment type that validates for creating an employee checklist.";
	private static final String NO_MATCHING_CHECKLIST_FOUND = "No checklist was found for any id in the organization tree for employee %s. Search has been performed for id %s.";
	private static final String NO_MATCHING_EMPLOYEE_CHECKLIST_FOUND = "Employee checklist with id %s was not found within municipality %s.";
	private static final String NO_MATCHING_PHASE_FOUND = "Phase with id %s was not found within municipality %s.";
	private static final String NO_FULFILMENT_INFORMATION_FOUND = "No fulfilment information found for task with id %s in employee checklist with id %s.";
	private static final List<String> VALID_EMPLOYMENT_FORMS_FOR_CHECKLIST = List.of("1", "2", "9"); // Permanent employment ("1"), temporary monthly paid employment ("2") and probationary employment ("9")

	private final DelegateRepository delegateRepository;
	private final EmployeeRepository employeeRepository;
	private final ManagerRepository managerRepository;
	private final EmployeeChecklistRepository employeeChecklistRepository;
	private final PhaseRepository phaseRepository;
	private final OrganizationRepository organizationRepository;
	private final CustomTaskRepository customTaskRepository;

	public EmployeeChecklistIntegration(
		final DelegateRepository delegateRepository,
		final EmployeeRepository employeeRepository,
		final ManagerRepository managerRepository,
		final EmployeeChecklistRepository employeeChecklistRepository,
		final PhaseRepository phaseRepository,
		final OrganizationRepository organizationRepository,
		final CustomTaskRepository customTaskRepository) {

		this.delegateRepository = delegateRepository;
		this.employeeRepository = employeeRepository;
		this.managerRepository = managerRepository;
		this.employeeChecklistRepository = employeeChecklistRepository;
		this.phaseRepository = phaseRepository;
		this.organizationRepository = organizationRepository;
		this.customTaskRepository = customTaskRepository;
	}

	public Optional<EmployeeChecklistEntity> fetchOptionalEmployeeChecklist(String municipalityId, String username) {
		return ofNullable(employeeChecklistRepository.findByChecklistsMunicipalityIdAndEmployeeUsername(municipalityId, username));
	}

	public List<EmployeeChecklistEntity> fetchEmployeeChecklistsForManager(String municipalityId, String username) {
		return employeeChecklistRepository.findAllByChecklistsMunicipalityIdAndEmployeeManagerUsername(municipalityId, username);
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
	public EmployeeChecklistEntity updateAllFulfilmentForAllTasksInPhase(String municipalityId, String employeeChecklistId, String phaseId, EmployeeChecklistPhaseUpdateRequest request) {
		final var employeeChecklist = fetchEmployeeChecklist(municipalityId, employeeChecklistId);

		verifyUnlockedEmployeeChecklist(employeeChecklist);

		if (isNull(request.getTasksFulfilmentStatus())) {
			return employeeChecklist;
		}

		// Update of all common tasks (if such exists) in phase
		employeeChecklist.getChecklists().stream()
			.map(ChecklistEntity::getTasks)
			.flatMap(List::stream)
			.filter(task -> Objects.equals(task.getPhase().getId(), phaseId))
			.forEach(task -> updateCommonTask(employeeChecklist, task, request.getTasksFulfilmentStatus(), request.getUpdatedBy()));

		// Update of all custom tasks (if such exists) in phase
		employeeChecklist.getCustomTasks().stream()
			.filter(task -> Objects.equals(task.getPhase().getId(), phaseId))
			.forEach(task -> updateCustomTask(employeeChecklist, task, request.getTasksFulfilmentStatus(), request.getUpdatedBy()));

		return employeeChecklistRepository.save(employeeChecklist);
	}

	private void updateCommonTask(EmployeeChecklistEntity employeeChecklist, TaskEntity task, FulfilmentStatus fulfilmentStatus, String updatedBy) {
		employeeChecklist.getFulfilments().stream()
			.filter(fulfilment -> Objects.equals(task, fulfilment.getTask()))
			.findAny()
			.ifPresentOrElse(
				fulfilment -> {
					fulfilment.setCompleted(fulfilmentStatus);
					fulfilment.setLastSavedBy(updatedBy);
				},
				() -> employeeChecklist.getFulfilments().add(toFulfilmentEntity(employeeChecklist, task, fulfilmentStatus, null, updatedBy)));
	}

	private void updateCustomTask(EmployeeChecklistEntity employeeChecklist, CustomTaskEntity customTask, FulfilmentStatus fulfilmentStatus, String updatedBy) {
		employeeChecklist.getCustomFulfilments().stream()
			.filter(fulfilment -> Objects.equals(customTask, fulfilment.getCustomTask()))
			.findAny()
			.ifPresentOrElse(
				fulfilment -> {
					fulfilment.setCompleted(fulfilmentStatus);
					fulfilment.setLastSavedBy(updatedBy);
				},
				() -> employeeChecklist.getCustomFulfilments().add(toCustomFulfilmentEntity(employeeChecklist, customTask, fulfilmentStatus, null, updatedBy)));
	}

	public EmployeeChecklistEntity fetchEmployeeChecklist(String municipalityId, String employeeChecklistId) {
		return employeeChecklistRepository.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NO_MATCHING_EMPLOYEE_CHECKLIST_FOUND.formatted(employeeChecklistId, municipalityId)));
	}

	@Transactional
	public FulfilmentEntity updateCommonTaskFulfilment(String municipalityId, String employeeChecklistId, String taskId, EmployeeChecklistTaskUpdateRequest request) {
		final var employeeChecklist = fetchEmployeeChecklist(municipalityId, employeeChecklistId);

		employeeChecklist.getChecklists().stream()
			.map(ChecklistEntity::getTasks)
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
					fulfilment.setLastSavedBy(request.getUpdatedBy());
				}, () -> employeeChecklist.getFulfilments().add(toFulfilmentEntity(employeeChecklist, task, request.getFulfilmentStatus(), request.getResponseText(), request.getUpdatedBy())));
	}

	@Transactional
	public CustomFulfilmentEntity updateCustomTaskFulfilment(String municipalityId, String employeeChecklistId, String taskId, EmployeeChecklistTaskUpdateRequest request) {
		final var employeeChecklist = fetchEmployeeChecklist(municipalityId, employeeChecklistId);

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
					fulfilment.setLastSavedBy(request.getUpdatedBy());
				}, () -> employeeChecklist.getCustomFulfilments().add(toCustomFulfilmentEntity(employeeChecklist, customTask, request.getFulfilmentStatus(), request.getResponseText(), request.getUpdatedBy())));
	}

	@Transactional
	public void deleteEmployeeChecklist(String municipalityId, String employeeChecklistId) {
		final var employeeChecklist = fetchEmployeeChecklist(municipalityId, employeeChecklistId);
		final var employee = employeeChecklist.getEmployee();
		final var manager = employeeChecklist.getEmployee().getManager();

		delegateRepository.deleteByEmployeeChecklist(employeeChecklist);
		employeeChecklistRepository.delete(employeeChecklist);
		manager.getEmployees().remove(employee); // This will remove the manager if it no longer has any employees connected to it
		employeeRepository.delete(employee);
	}

	@Transactional
	public EmployeeChecklistEntity setMentor(final String municipalityId, final String employeeChecklistId, final Mentor mentor) {
		final var employeeChecklist = fetchEmployeeChecklist(municipalityId, employeeChecklistId);
		employeeChecklist.setMentor(MentorEntity.builder()
			.withUserId(mentor.getUserId())
			.withName(mentor.getName())
			.build());
		return employeeChecklistRepository.save(employeeChecklist);
	}

	@Transactional
	public void deleteMentor(final String municipalityId, final String employeeChecklistId) {
		final var employeeChecklist = fetchEmployeeChecklist(municipalityId, employeeChecklistId);
		employeeChecklist.setMentor(null);
		employeeChecklistRepository.save(employeeChecklist);
	}

	@Transactional
	public CustomTaskEntity createCustomTask(String municipalityId, String employeeChecklistId, String phaseId, CustomTaskCreateRequest request) {
		final var employeeChecklist = fetchEmployeeChecklist(municipalityId, employeeChecklistId);
		final var phaseEntity = phaseRepository.findByIdAndMunicipalityId(phaseId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, NO_MATCHING_PHASE_FOUND.formatted(phaseId, municipalityId)));

		final var customTaskEntity = toCustomTaskEntity(employeeChecklist, phaseEntity, request);
		customTaskRepository.save(customTaskEntity);

		employeeChecklist.getCustomTasks().add(customTaskEntity);
		employeeChecklistRepository.save(employeeChecklist);

		return customTaskEntity;
	}

	/**
	 * Method for creating an employee checklist based on nearest organizational checklist.
	 *
	 * @param  municipalityId   the id of the municipality where the employee belongs
	 * @param  employee         the employee to onboard
	 * @param  orgTree          the organization tree for the employee
	 * @return                  status of the process of creating the employee checklist
	 * @throws ThrowableProblem if error occurs when processing employee
	 */
	@Transactional
	public String initiateEmployee(String municipalityId, Employee employee, OrganizationTree orgTree) {
		if (employeeRepository.existsById(employee.getPersonId().toString())) {
			return EMPLOYEE_HAS_CHECKLIST.formatted(employee.getLoginname());
		}
		if (!isValidForChecklist(employee)) {
			throw Problem.valueOf(NOT_ACCEPTABLE, EMPLOYMENT_TYPE_NOT_VALID_FOR_CHECKLIST.formatted(employee.getLoginname()));
		}
		final var employment = getMainEmployment(employee);
		final var employeeEntity = toEmployeeEntity(employee);

		// Attach existing organizational units to the employee (or create new unit if not present in the persistant layer)
		employeeEntity.setCompany(retrieveOrganizationEntity(municipalityId, employment.getCompanyId(), null));
		employeeEntity.setDepartment(retrieveOrganizationEntity(municipalityId, employment.getOrgId(), employment.getOrgName()));

		// Attach existing manager to the employee (or create new manager if not present in the persistant layer)
		employeeEntity.setManager(retrieveManagerEntity(employment.getManager()));

		// Persist employee and create checklist for him/her
		final var persistedEmployee = employeeRepository.save(employeeEntity);
		initiateEmployeeChecklist(municipalityId, persistedEmployee, orgTree);

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

	private OrganizationEntity retrieveOrganizationEntity(String municipalityId, int organizationNumber, String organizationName) {
		return organizationRepository.findByOrganizationNumberAndMunicipalityId(organizationNumber, municipalityId)
			.orElse(toOrganizationEntity(organizationNumber, organizationName, municipalityId));
	}

	private void initiateEmployeeChecklist(String municipalityId, EmployeeEntity employeeEntity, OrganizationTree orgTree) {
		final var checklistEntities = retrieveChecklists(municipalityId, orgTree.getTree().descendingMap().values().iterator(), new ArrayList<>());
		if (CollectionUtils.isEmpty(checklistEntities)) {
			throw Problem.valueOf(NOT_FOUND, NO_MATCHING_CHECKLIST_FOUND.formatted(employeeEntity.getUsername(),
				toReadableString(orgTree.getTree().values().stream().map(OrganizationLine::getOrgId).toList())));
		}

		employeeChecklistRepository.save(toEmployeeChecklistEntity(employeeEntity, checklistEntities));
	}

	private List<ChecklistEntity> retrieveChecklists(String municipalityId, Iterator<OrganizationLine> organizationIterator, List<ChecklistEntity> resultList) {
		organizationIterator.forEachRemaining(orgLine -> retrieveActiveChecklist(municipalityId, Integer.parseInt(orgLine.getOrgId()))
			.ifPresent(resultList::add));

		return resultList;
	}

	private Optional<ChecklistEntity> retrieveActiveChecklist(String municipalityId, int organizationNumber) {
		return organizationRepository.findByOrganizationNumberAndMunicipalityId(organizationNumber, municipalityId)
			.map(OrganizationEntity::getChecklists)
			.map(List::stream)
			.orElse(Stream.empty())
			.filter(checklist -> Objects.equals(checklist.getLifeCycle(), ACTIVE))
			.findAny();
	}

	/**
	 * Fetches all ongoing employee checklists for a specific municipality. An ongoing employee checklist is an employee
	 * checklist that started before today and ends after today.
	 *
	 * @param  municipalityId the id of the municipality
	 * @param  pageable       the page request
	 * @return                a page of ongoing employee checklists
	 */
	public Page<EmployeeChecklistEntity> fetchAllOngoingEmployeeChecklists(final String municipalityId, final PageRequest pageable) {
		return employeeChecklistRepository.findAllByChecklistsMunicipalityIdAndStartDateIsAfterAndEndDateIsAfter(municipalityId, LocalDate.now().minusDays(1), LocalDate.now(), pageable);
	}
}
