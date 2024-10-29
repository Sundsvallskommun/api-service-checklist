package se.sundsvall.checklist.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static org.zalando.problem.Status.OK;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.MANAGER;
import static se.sundsvall.checklist.integration.employee.EmployeeFilterBuilder.buildDefaultNewEmployeeFilter;
import static se.sundsvall.checklist.integration.employee.EmployeeFilterBuilder.buildUuidEmployeeFilter;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toCustomTask;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.updateCustomTaskEntity;
import static se.sundsvall.checklist.service.util.EmployeeChecklistDecorator.decorateWithCustomTasks;
import static se.sundsvall.checklist.service.util.EmployeeChecklistDecorator.decorateWithFulfilment;
import static se.sundsvall.checklist.service.util.ServiceUtils.calculateTaskType;
import static se.sundsvall.checklist.service.util.ServiceUtils.fetchEntity;
import static se.sundsvall.checklist.service.util.VerificationUtils.verifyMandatoryInformation;
import static se.sundsvall.checklist.service.util.VerificationUtils.verifyUnlockedEmployeeChecklist;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.StatusType;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.employee.Employee;
import se.sundsvall.checklist.api.model.CustomTask;
import se.sundsvall.checklist.api.model.CustomTaskCreateRequest;
import se.sundsvall.checklist.api.model.CustomTaskUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhase;
import se.sundsvall.checklist.api.model.EmployeeChecklistPhaseUpdateRequest;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse.Detail;
import se.sundsvall.checklist.api.model.EmployeeChecklistTask;
import se.sundsvall.checklist.api.model.EmployeeChecklistTaskUpdateRequest;
import se.sundsvall.checklist.integration.db.EmployeeChecklistIntegration;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.employee.EmployeeIntegration;
import se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper;
import se.sundsvall.checklist.service.util.ServiceUtils;
import se.sundsvall.checklist.service.util.TaskType;

@Service
public class EmployeeChecklistService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeChecklistService.class);
	private static final String ORGANIZATIONAL_STRUCTURE_DATA_NOT_FOUND = "Employee with username %s is missing information regarding organizational structure.";
	private static final String CUSTOM_TASK_NOT_FOUND = "Employee checklist with id %s does not contain any custom task with id %s.";
	private static final String ERROR_READING_PHASE_FROM_EMPLOYEE_CHECKLIST = "Could not read phase with id %s from employee checklist with id %s.";

	private final CustomTaskRepository customTaskRepository;
	private final EmployeeIntegration employeeIntegration;
	private final EmployeeChecklistIntegration employeeChecklistIntegration;
	private final Duration employeeInformationUpdateInterval;

	public EmployeeChecklistService(
		final CustomTaskRepository customTaskRepository,
		final EmployeeIntegration employeeIntegration,
		final EmployeeChecklistIntegration employeeChecklistIntegration,
		@Value("${checklist.employee-update-interval}") final Duration employeeInformationUpdateInterval) {

		this.customTaskRepository = customTaskRepository;
		this.employeeIntegration = employeeIntegration;
		this.employeeChecklistIntegration = employeeChecklistIntegration;
		this.employeeInformationUpdateInterval = employeeInformationUpdateInterval;
	}

	public Optional<EmployeeChecklist> fetchChecklistForEmployee(String municipalityId, String username) {
		final var employeeChecklist = employeeChecklistIntegration.fetchOptionalEmployeeChecklist(municipalityId, username);

		return employeeChecklist
			.map(this::handleUpdatedEmployeeInformation)
			.map(EmployeeChecklistMapper::toEmployeeChecklist)
			.map(ob -> decorateWithCustomTasks(ob, customTaskRepository.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistMunicipalityId(ob.getId(), municipalityId)))
			.map(ob -> decorateWithFulfilment(ob, employeeChecklist))
			.map(this::decorateWithDelegateInformation)
			.map(ServiceUtils::calculateCompleted)
			.map(this::removeManagerObjects);
	}

	public List<EmployeeChecklist> fetchChecklistsForManager(String municipalityId, String username) {
		final var employeeChecklists = employeeChecklistIntegration.fetchEmployeeChecklistsForManager(municipalityId, username);

		return employeeChecklists
			.stream()
			.map(this::handleUpdatedEmployeeInformation)
			.filter(ob -> Objects.equals(username, ob.getEmployee().getManager().getUsername())) // After possible update, the checklist might not be handled by sent in username anymore
			.map(EmployeeChecklistMapper::toEmployeeChecklist)
			.map(ob -> decorateWithCustomTasks(ob, customTaskRepository.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistMunicipalityId(ob.getId(), municipalityId)))
			.map(ob -> decorateWithFulfilment(ob, fetchEntity(employeeChecklists, ob.getId())))
			.map(this::decorateWithDelegateInformation)
			.map(ServiceUtils::calculateCompleted)
			.toList();
	}

	private EmployeeChecklistEntity handleUpdatedEmployeeInformation(EmployeeChecklistEntity employeeChecklist) {
		if (employeeChecklist.getEmployee().getUpdated().isBefore(OffsetDateTime.now().minus(employeeInformationUpdateInterval))) {
			final var filter = buildUuidEmployeeFilter(employeeChecklist.getEmployee().getId());
			employeeIntegration.getEmployeeInformation(filter).stream()
				.findFirst()
				.ifPresent(employee -> employeeChecklistIntegration.updateEmployeeInformation(employeeChecklist.getEmployee(), employee));
		}

		return employeeChecklist;
	}

	private EmployeeChecklist decorateWithDelegateInformation(EmployeeChecklist employeeChecklist) {
		employeeChecklist.setDelegatedTo(employeeChecklistIntegration.fetchDelegateEmails(employeeChecklist.getId()));
		return employeeChecklist;
	}

	private EmployeeChecklist removeManagerObjects(EmployeeChecklist employeeChecklist) {
		employeeChecklist.getPhases().removeIf(ph -> MANAGER == ph.getRoleType());
		employeeChecklist.getPhases().forEach(ph -> ph.getTasks().removeIf(task -> MANAGER == task.getRoleType()));

		return employeeChecklist;
	}

	public void deleteEmployeChecklist(String municipalityId, String employeeChecklistId) {
		employeeChecklistIntegration.deleteEmployeeChecklist(municipalityId, employeeChecklistId);
	}

	public CustomTask createCustomTask(String municipalityId, String employeeChecklistId, String phaseId, CustomTaskCreateRequest request) {
		verifyUnlockedEmployeeChecklist(employeeChecklistIntegration.fetchEmployeeChecklist(municipalityId, employeeChecklistId));
		return toCustomTask(employeeChecklistIntegration.createCustomTask(municipalityId, employeeChecklistId, phaseId, request));
	}

	public CustomTask readCustomTask(String municipalityId, String employeeChecklistId, String taskId) {
		return customTaskRepository.findById(taskId)
			.filter(customTask -> Objects.equals(employeeChecklistId, customTask.getEmployeeChecklist().getId()))
			.filter(customTask -> Objects.equals(municipalityId, customTask.getEmployeeChecklist().getChecklist().getMunicipalityId()))
			.map(EmployeeChecklistMapper::toCustomTask)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CUSTOM_TASK_NOT_FOUND.formatted(employeeChecklistId, taskId)));
	}

	public CustomTask updateCustomTask(String municipalityId, String employeeChecklistId, String taskId, CustomTaskUpdateRequest request) {
		final var entity = customTaskRepository.findById(taskId)
			.filter(customTask -> Objects.equals(employeeChecklistId, customTask.getEmployeeChecklist().getId()))
			.filter(customTask -> Objects.equals(municipalityId, customTask.getEmployeeChecklist().getChecklist().getMunicipalityId()))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CUSTOM_TASK_NOT_FOUND.formatted(employeeChecklistId, taskId)));

		verifyUnlockedEmployeeChecklist(entity.getEmployeeChecklist());
		updateCustomTaskEntity(entity, request);
		customTaskRepository.save(entity);

		return toCustomTask(entity);
	}

	@Transactional
	public void deleteCustomTask(String municipalityId, String employeeChecklistId, String taskId) {
		final var entity = customTaskRepository.findById(taskId)
			.filter(customTask -> Objects.equals(employeeChecklistId, customTask.getEmployeeChecklist().getId()))
			.filter(customTask -> Objects.equals(municipalityId, customTask.getEmployeeChecklist().getChecklist().getMunicipalityId()))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CUSTOM_TASK_NOT_FOUND.formatted(employeeChecklistId, taskId)));

		verifyUnlockedEmployeeChecklist(entity.getEmployeeChecklist());
		entity.getEmployeeChecklist().getCustomFulfilments()
			.removeIf(fulfilment -> Objects.equals(fulfilment.getCustomTask().getId(), taskId)); // Remove fulfilment for custom task if present

		// Remove custom task from checklist
		customTaskRepository.delete(entity);
	}

	public EmployeeChecklistPhase updateAllTasksInPhase(String municipalityId, String employeeChecklistId, String phaseId, EmployeeChecklistPhaseUpdateRequest request) {
		final var employeeChecklist = employeeChecklistIntegration.updateAllTasksInPhase(municipalityId, employeeChecklistId, phaseId, request);

		return employeeChecklist.getChecklist().getPhases().stream()
			.filter(phase -> Objects.equals(phase.getId(), phaseId))
			.findAny()
			.map(EmployeeChecklistMapper::toEmployeeChecklistPhase)
			.map(phase -> decorateWithCustomTasks(phase, customTaskRepository.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistMunicipalityId(employeeChecklistId, municipalityId)))
			.map(phase -> decorateWithFulfilment(phase, employeeChecklist))
			.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, ERROR_READING_PHASE_FROM_EMPLOYEE_CHECKLIST.formatted(phaseId, employeeChecklistId)));
	}

	public EmployeeChecklistTask updateTaskFulfilment(String municipalityId, String employeeChecklistId, String taskId, EmployeeChecklistTaskUpdateRequest request) {
		final var employeeChecklist = employeeChecklistIntegration.fetchEmployeeChecklist(municipalityId, employeeChecklistId);
		verifyUnlockedEmployeeChecklist(employeeChecklist);

		if (calculateTaskType(employeeChecklist, taskId) == TaskType.COMMON) {
			final var fulfilment = employeeChecklistIntegration.updateCommonTaskFulfilment(municipalityId, employeeChecklistId, taskId, request);
			return findTask(taskId, employeeChecklist.getChecklist())
				.map(EmployeeChecklistMapper::toEmployeeChecklistTask)
				.map(employeeChecklistTask -> decorateWithFulfilment(employeeChecklistTask, fulfilment))
				.orElse(null); // This will never happen as the task is verified to exist in the calculateTaskType method

		}
		final var fulfilment = employeeChecklistIntegration.updateCustomTaskFulfilment(municipalityId, employeeChecklistId, taskId, request);
		return findCustomTask(taskId, employeeChecklist.getCustomTasks())
			.map(EmployeeChecklistMapper::toEmployeeChecklistTask)
			.map(employeeChecklistTask -> decorateWithFulfilment(employeeChecklistTask, fulfilment))
			.orElse(null); // This will never happen as the custom task is verified to exist in the calculateTaskType method
	}

	private Optional<TaskEntity> findTask(String taskId, ChecklistEntity checklist) {
		return checklist.getPhases().stream()
			.map(PhaseEntity::getTasks)
			.flatMap(List::stream)
			.filter(task -> Objects.equals(task.getId(), taskId))
			.findAny();
	}

	private Optional<CustomTaskEntity> findCustomTask(String taskId, final List<CustomTaskEntity> tasks) {
		return ofNullable(tasks).orElse(emptyList()).stream()
			.filter(task -> Objects.equals(task.getId(), taskId))
			.findAny();
	}

	/**
	 * Fetch a specific employee from employee integration (regardless of other data than uuid,
	 * for example if it is a new or old employee) and initiate checklists for him or her.
	 */
	public EmployeeChecklistResponse initiateSpecificEmployeeChecklist(String municipalityId, String uuid) {
		final var filter = buildUuidEmployeeFilter(uuid);
		LOGGER.info("Fetching employee with filter: {}", filter);

		final var employees = employeeIntegration.getEmployeeInformation(filter);
		if (isEmpty(employees)) {
			return buildNoMatchResponse();
		}

		return processEmployees(municipalityId, employees);
	}

	/**
	 * Fetch new employees from employee integration and initiate checklists for them.
	 */
	public EmployeeChecklistResponse initiateEmployeeChecklists(String municipalityId) {
		final var filter = buildDefaultNewEmployeeFilter();
		LOGGER.info("Fetching new employees with filter: {}", filter);

		final var employees = employeeIntegration.getNewEmployees(filter);
		if (isEmpty(employees)) {
			return buildNoMatchResponse();
		}

		return processEmployees(municipalityId, employees);
	}

	private EmployeeChecklistResponse processEmployees(String municipalityId, final List<Employee> employees) {
		LOGGER.info("Found {} employees, creating checklists for these employees", employees.size());
		final var employeeChecklistResponse = createEmployeeChecklist(municipalityId, employees);
		final var errors = ofNullable(employeeChecklistResponse.getDetails()).orElse(emptyList())
			.stream()
			.filter(detail -> notEqual(OK, detail.getStatus()))
			.count();

		employeeChecklistResponse.setSummary(errors > 0 ? "%s potential problems occurred when importing %s employees".formatted(errors, employees.size()) : "Successful import of %s employees".formatted(employees.size()));

		return employeeChecklistResponse;
	}

	private EmployeeChecklistResponse createEmployeeChecklist(String municipalityId, final List<Employee> employees) {
		final var emplyeeChecklistResponse = new EmployeeChecklistResponse();

		employees.forEach(employee -> {
			try {
				// Verify that employee contains all mandatory information needed to create an employee checklist
				verifyMandatoryInformation(employee);

				final var portalPersonData = employeeIntegration.getEmployeeByEmail(employee.getEmailAddress())
					.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ORGANIZATIONAL_STRUCTURE_DATA_NOT_FOUND.formatted(employee.getLoginname())));

				final var result = employeeChecklistIntegration.initiateEmployee(municipalityId, employee, OrganizationTree.map(portalPersonData.getCompanyId(), portalPersonData.getOrgTree()));
				emplyeeChecklistResponse.getDetails().add(createDetail(OK, result));
			} catch (final ThrowableProblem e) {
				emplyeeChecklistResponse.getDetails().add(createDetail(e.getStatus(), e.getMessage()));
			} catch (final Exception e) {
				LOGGER.error("Exception occured when creating employee checklist", e);
				emplyeeChecklistResponse.getDetails().add(createDetail(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR.getReasonPhrase() + ": " + e.getMessage()));
			}
		});

		return emplyeeChecklistResponse;
	}

	private Detail createDetail(StatusType status, String message) {
		return Detail.builder()
			.withInformation(message)
			.withStatus(status).build();
	}

	private EmployeeChecklistResponse buildNoMatchResponse() {
		LOGGER.info("No employees found matching provided filter");
		return EmployeeChecklistResponse.builder()
			.withSummary("No employees found")
			.build();
	}

}
