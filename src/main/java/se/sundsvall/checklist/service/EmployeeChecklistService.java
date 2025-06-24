package se.sundsvall.checklist.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static org.zalando.problem.Status.OK;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.MANAGER_FOR_NEW_EMPLOYEE;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.MANAGER_FOR_NEW_MANAGER;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.createUpdateManagerDetailString;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.createUpdateManagerErrorString;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toCustomTask;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toDetail;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toInitiationInfoEntity;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toInitiationInformations;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toUpdateManagerResponse;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.updateCustomTaskEntity;
import static se.sundsvall.checklist.service.mapper.PagingAndSortingMapper.toPageRequest;
import static se.sundsvall.checklist.service.mapper.PagingAndSortingMapper.toPagingMetaData;
import static se.sundsvall.checklist.service.util.ChecklistUtils.removeObsoleteTasks;
import static se.sundsvall.checklist.service.util.EmployeeChecklistDecorator.decorateWithCustomTasks;
import static se.sundsvall.checklist.service.util.EmployeeChecklistDecorator.decorateWithFulfilment;
import static se.sundsvall.checklist.service.util.ServiceUtils.calculateTaskType;
import static se.sundsvall.checklist.service.util.ServiceUtils.fetchEntity;
import static se.sundsvall.checklist.service.util.StringUtils.toSecureString;
import static se.sundsvall.checklist.service.util.VerificationUtils.verifyMandatoryInformation;
import static se.sundsvall.checklist.service.util.VerificationUtils.verifyUnlockedEmployeeChecklist;
import static se.sundsvall.checklist.service.util.VerificationUtils.verifyValidEmployment;

import generated.se.sundsvall.mdviewer.Organization;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
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
import se.sundsvall.checklist.api.model.InitiationInformation;
import se.sundsvall.checklist.api.model.Mentor;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklistParameters;
import se.sundsvall.checklist.api.model.OngoingEmployeeChecklists;
import se.sundsvall.checklist.integration.db.EmployeeChecklistIntegration;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.InitiationRepository;
import se.sundsvall.checklist.integration.employee.EmployeeIntegration;
import se.sundsvall.checklist.integration.mdviewer.MDViewerClient;
import se.sundsvall.checklist.service.OrganizationTree.OrganizationLine;
import se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper;
import se.sundsvall.checklist.service.model.Employee;
import se.sundsvall.checklist.service.util.ChecklistUtils;
import se.sundsvall.checklist.service.util.TaskType;

@Service
public class EmployeeChecklistService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeChecklistService.class);

	private static final LocalDate DEFAULT_HIRE_DATE_FROM_PARAMETER_VALUE = LocalDate.now().minusDays(30);
	private static final String ORGANIZATIONAL_STRUCTURE_DATA_NOT_FOUND = "Employee with loginname %s is missing information regarding organizational structure.";
	private static final String CUSTOM_TASK_NOT_FOUND = "Employee checklist with id %s does not contain any custom task with id %s.";
	private static final String ERROR_READING_PHASE_FROM_EMPLOYEE_CHECKLIST = "Could not read phase with id %s from employee checklist with id %s.";

	private final CustomTaskRepository customTaskRepository;
	private final InitiationRepository initiationRepository;
	private final EmployeeIntegration employeeIntegration;
	private final EmployeeChecklistIntegration employeeChecklistIntegration;
	private final SortorderService sortorderService;
	private final MDViewerClient mdViewerClient;
	private final Duration employeeInformationUpdateInterval;

	public EmployeeChecklistService(
		final CustomTaskRepository customTaskRepository,
		final InitiationRepository initiationRepository,
		final EmployeeIntegration employeeIntegration,
		final EmployeeChecklistIntegration employeeChecklistIntegration,
		final SortorderService sortorderService,
		final MDViewerClient mdViewerClient,
		@Value("${checklist.employee-update-interval}") final Duration employeeInformationUpdateInterval) {

		this.customTaskRepository = customTaskRepository;
		this.initiationRepository = initiationRepository;
		this.employeeIntegration = employeeIntegration;
		this.employeeChecklistIntegration = employeeChecklistIntegration;
		this.sortorderService = sortorderService;
		this.mdViewerClient = mdViewerClient;
		this.employeeInformationUpdateInterval = employeeInformationUpdateInterval;
	}

	public Optional<EmployeeChecklist> fetchChecklistForEmployee(final String municipalityId, final String username) {
		final var employeeChecklist = employeeChecklistIntegration.fetchOptionalEmployeeChecklist(municipalityId, username);

		return employeeChecklist

			.map(checklist -> handleUpdatedEmployeeInformation(municipalityId, checklist))
			.map(EmployeeChecklistMapper::toEmployeeChecklist)
			.map(list -> decorateWithCustomTasks(list, customTaskRepository.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(list.getId(), municipalityId)))
			.map(list -> removeObsoleteTasks(list, employeeChecklist))
			.map(ChecklistUtils::initializeWithEmptyFulfilment)
			.map(list -> decorateWithFulfilment(list, employeeChecklist))
			.map(this::decorateWithDelegateInformation)
			.map(this::removeManagerTasks)
			.map(list -> sortorderService.applySorting(employeeChecklist, list));
	}

	public List<EmployeeChecklist> fetchChecklistsForManager(final String municipalityId, final String username) {
		final var employeeChecklists = employeeChecklistIntegration.fetchEmployeeChecklistsForManager(municipalityId, username);

		return employeeChecklists
			.stream()
			.map(checklist -> handleUpdatedEmployeeInformation(municipalityId, checklist))
			.filter(list -> Objects.equals(username, list.getEmployee().getManager().getUsername())) // After possible update, the checklist might not be handled by sent in username anymore
			.map(EmployeeChecklistMapper::toEmployeeChecklist)
			.map(list -> decorateWithCustomTasks(list, customTaskRepository.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(list.getId(), municipalityId)))
			.map(list -> removeObsoleteTasks(list, fetchEntity(employeeChecklists, list.getId())))
			.map(ChecklistUtils::initializeWithEmptyFulfilment)
			.map(list -> decorateWithFulfilment(list, fetchEntity(employeeChecklists, list.getId())))
			.map(list -> sortorderService.applySorting(fetchEntity(employeeChecklists, list.getId()), list))
			.map(this::decorateWithDelegateInformation)
			.toList();
	}

	private EmployeeChecklistEntity handleUpdatedEmployeeInformation(final String municipalityId, final EmployeeChecklistEntity employeeChecklist) {
		if (ofNullable(employeeChecklist.getEmployee().getUpdated()).orElse(OffsetDateTime.MIN).isBefore(OffsetDateTime.now().minus(employeeInformationUpdateInterval))) {
			employeeIntegration.getEmployeeInformation(municipalityId, employeeChecklist.getEmployee().getId()).stream()
				.findFirst()
				.ifPresent(employee -> employeeChecklistIntegration.updateEmployeeInformation(employeeChecklist.getEmployee(), employee));
		}

		return employeeChecklist;
	}

	private EmployeeChecklist decorateWithDelegateInformation(final EmployeeChecklist employeeChecklist) {
		employeeChecklist.setDelegatedTo(employeeChecklistIntegration.fetchDelegateEmails(employeeChecklist.getId()));
		return employeeChecklist;
	}

	private EmployeeChecklist removeManagerTasks(final EmployeeChecklist employeeChecklist) {
		employeeChecklist.getPhases().forEach(ph -> ph.getTasks().removeIf(
			task -> MANAGER_FOR_NEW_EMPLOYEE == task.getRoleType() || MANAGER_FOR_NEW_MANAGER == task.getRoleType()));
		employeeChecklist.getPhases().removeIf(ph -> CollectionUtils.isEmpty(ph.getTasks()));

		return employeeChecklist;
	}

	public void deleteEmployeeChecklist(final String municipalityId, final String employeeChecklistId) {
		employeeChecklistIntegration.deleteEmployeeChecklist(municipalityId, employeeChecklistId);
	}

	public void setMentor(final String municipalityId, final String employeeChecklistId, final Mentor mentor) {
		employeeChecklistIntegration.setMentor(municipalityId, employeeChecklistId, mentor);
	}

	public void deleteMentor(final String municipalityId, final String employeeChecklistId) {
		employeeChecklistIntegration.deleteMentor(municipalityId, employeeChecklistId);
	}

	public CustomTask createCustomTask(String municipalityId, String employeeChecklistId, String phaseId, CustomTaskCreateRequest request) {
		verifyUnlockedEmployeeChecklist(employeeChecklistIntegration.fetchEmployeeChecklist(municipalityId, employeeChecklistId));
		return toCustomTask(employeeChecklistIntegration.createCustomTask(municipalityId, employeeChecklistId, phaseId, request));
	}

	public CustomTask readCustomTask(final String municipalityId, final String employeeChecklistId, final String taskId) {
		return customTaskRepository.findById(taskId)
			.filter(customTask -> Objects.equals(employeeChecklistId, customTask.getEmployeeChecklist().getId()))
			.filter(customTask -> Objects.equals(municipalityId, customTask.getEmployeeChecklist().getChecklists().getFirst().getMunicipalityId()))
			.map(EmployeeChecklistMapper::toCustomTask)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CUSTOM_TASK_NOT_FOUND.formatted(employeeChecklistId, taskId)));
	}

	public CustomTask updateCustomTask(final String municipalityId, final String employeeChecklistId, final String taskId, final CustomTaskUpdateRequest request) {
		final var entity = customTaskRepository.findById(taskId)
			.filter(customTask -> Objects.equals(employeeChecklistId, customTask.getEmployeeChecklist().getId()))
			.filter(customTask -> Objects.equals(municipalityId, customTask.getEmployeeChecklist().getChecklists().getFirst().getMunicipalityId()))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CUSTOM_TASK_NOT_FOUND.formatted(employeeChecklistId, taskId)));

		verifyUnlockedEmployeeChecklist(entity.getEmployeeChecklist());
		updateCustomTaskEntity(entity, request);
		customTaskRepository.save(entity);

		return toCustomTask(entity);
	}

	@Transactional
	public void deleteCustomTask(final String municipalityId, final String employeeChecklistId, final String taskId) {
		final var entity = customTaskRepository.findById(taskId)
			.filter(customTask -> Objects.equals(employeeChecklistId, customTask.getEmployeeChecklist().getId()))
			.filter(customTask -> Objects.equals(municipalityId, customTask.getEmployeeChecklist().getChecklists().getFirst().getMunicipalityId()))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CUSTOM_TASK_NOT_FOUND.formatted(employeeChecklistId, taskId)));

		verifyUnlockedEmployeeChecklist(entity.getEmployeeChecklist());
		entity.getEmployeeChecklist().getCustomFulfilments()
			.removeIf(fulfilment -> Objects.equals(fulfilment.getCustomTask().getId(), taskId)); // Remove fulfilment for custom task if present

		// Remove custom task from checklist
		customTaskRepository.delete(entity);
	}

	public EmployeeChecklistPhase updateAllTasksInPhase(final String municipalityId, final String employeeChecklistId, final String phaseId, final EmployeeChecklistPhaseUpdateRequest request) {
		final var employeeChecklist = employeeChecklistIntegration.updateAllFulfilmentForAllTasksInPhase(municipalityId, employeeChecklistId, phaseId, request);

		return allTasksAsStream(employeeChecklist)
			.filter(task -> Objects.equals(task.getPhase().getId(), phaseId))
			.findAny()
			.map(TaskEntity::getPhase)
			.map(phase -> EmployeeChecklistMapper.toEmployeeChecklistPhase(phase, getTasksInPhase(allTasksAsStream(employeeChecklist).toList(), phaseId)))
			.map(phase -> decorateWithCustomTasks(phase, customTaskRepository.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(employeeChecklistId, municipalityId)))
			.map(phase -> decorateWithFulfilment(phase, employeeChecklist))
			.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, ERROR_READING_PHASE_FROM_EMPLOYEE_CHECKLIST.formatted(phaseId, employeeChecklistId)));
	}

	private Stream<TaskEntity> allTasksAsStream(final EmployeeChecklistEntity employeeChecklist) {
		return employeeChecklist.getChecklists().stream()
			.map(ChecklistEntity::getTasks)
			.flatMap(List::stream);
	}

	private List<TaskEntity> getTasksInPhase(final List<TaskEntity> tasks, final String phaseId) {
		return ofNullable(tasks).orElse(emptyList()).stream()
			.filter(task -> Objects.equals(task.getPhase().getId(), phaseId))
			.toList();
	}

	public EmployeeChecklistTask updateTaskFulfilment(final String municipalityId, final String employeeChecklistId, final String taskId, final EmployeeChecklistTaskUpdateRequest request) {
		final var employeeChecklist = employeeChecklistIntegration.fetchEmployeeChecklist(municipalityId, employeeChecklistId);
		verifyUnlockedEmployeeChecklist(employeeChecklist);

		if (calculateTaskType(employeeChecklist, taskId) == TaskType.COMMON) {
			final var fulfilment = employeeChecklistIntegration.updateCommonTaskFulfilment(municipalityId, employeeChecklistId, taskId, request);
			return findTask(taskId, employeeChecklist)
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

	private Optional<TaskEntity> findTask(final String taskId, final EmployeeChecklistEntity employeeChecklistEntity) {
		return allTasksAsStream(employeeChecklistEntity)
			.filter(task -> Objects.equals(task.getId(), taskId))
			.findAny();
	}

	private Optional<CustomTaskEntity> findCustomTask(final String taskId, final List<CustomTaskEntity> tasks) {
		return ofNullable(tasks).orElse(emptyList()).stream()
			.filter(task -> Objects.equals(task.getId(), taskId))
			.findAny();
	}

	public List<InitiationInformation> getInitiationInformation(final String municipalityId, boolean onlyLatest, boolean onlyErrors) {
		final var infos = toInitiationInformations(initiationRepository.findAllByMunicipalityId(municipalityId));

		// If request is to only return initiations with error, remove all detail rows that is not interpreted as error
		if (onlyErrors) {
			infos.forEach(info -> info.getDetails().removeIf(detail -> !HttpStatus.valueOf(detail.getStatus()).isError()));
		}

		// If request is to only return the latest execution, pick the first of list (or empty if no posts are present)
		if (onlyLatest) {
			return infos.isEmpty() ? emptyList() : List.of(infos.getFirst());
		}

		// Default return full response
		return infos;
	}

	/**
	 * Fetch a specific employee from employee integration (regardless of other data than uuid, for example if it is a new
	 * or old employee, what employment type the employee has, if he or she is a joiner or not) and initiate checklists for
	 * him or her.
	 */
	public EmployeeChecklistResponse initiateSpecificEmployeeChecklist(final String municipalityId, final String uuid) {
		LOGGER.info("Fetching employees by municipalityId: {} and personId: {}", municipalityId, uuid);

		final var employees = employeeIntegration.getEmployeeInformation(municipalityId, uuid);
		if (isEmpty(employees)) {
			return buildNoMatchResponse();
		}

		return processEmployees(municipalityId, employees, false);
	}

	/**
	 * Fetch new employees from employee integration and initiate checklists for them.
	 */
	public EmployeeChecklistResponse initiateEmployeeChecklists(final String municipalityId) {

		LOGGER.info("Fetching new employees by municipalityId: {} and hireDateFrom: {}", municipalityId, DEFAULT_HIRE_DATE_FROM_PARAMETER_VALUE);

		final var employees = employeeIntegration.getNewEmployees(municipalityId, DEFAULT_HIRE_DATE_FROM_PARAMETER_VALUE);
		if (isEmpty(employees)) {
			return buildNoMatchResponse();
		}

		return processEmployees(municipalityId, employees, true);
	}

	private EmployeeChecklistResponse processEmployees(final String municipalityId, final List<Employee> employees, final boolean verifyValidEmployment) {
		LOGGER.info("Found {} employees, creating checklists for these employees", employees.size());
		final var employeeChecklistResponse = createEmployeeChecklist(municipalityId, employees, verifyValidEmployment);
		final var errors = ofNullable(employeeChecklistResponse.getDetails()).orElse(emptyList())
			.stream()
			.filter(detail -> notEqual(OK, detail.getStatus()))
			.count();

		employeeChecklistResponse.setSummary(errors > 0 ? "%s potential problems occurred when importing %s employees".formatted(errors, employees.size()) : "Successful import of %s employees".formatted(employees.size()));

		initiationRepository.saveAll(employeeChecklistResponse.getDetails().stream()
			.map(detail -> toInitiationInfoEntity(municipalityId, detail))
			.toList());

		return employeeChecklistResponse;
	}

	private EmployeeChecklistResponse createEmployeeChecklist(final String municipalityId, final List<Employee> employees, final boolean verifyValidEmployment) {
		final var employeeChecklistResponse = new EmployeeChecklistResponse();

		employees.forEach(employee -> {
			try {
				// Verify that employee contains all mandatory information needed to create an employee checklist
				verifyMandatoryInformation(employee);
				if (verifyValidEmployment) {
					// Verify that the employment is valid for creating an employee checklist (this is only done
					// for automatic import of new employees, not when manually importing a specific employee)
					verifyValidEmployment(employee);
				}

				final var portalPersonData = employeeIntegration.getEmployeeByEmail(municipalityId, employee.getEmailAddress())
					.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ORGANIZATIONAL_STRUCTURE_DATA_NOT_FOUND.formatted(employee.getLoginname())));

				// Calculate employee orgtree from person data information (which does not include the root organization)
				final var employeeOrgTree = OrganizationTree.map(portalPersonData.getOrgTree());

				// We need to find the root organization connected to the top level in the employees org tree via mdviewer
				final var organizations = mdViewerClient.getOrganizationsForCompany(portalPersonData.getCompanyId());
				organizations.stream()
					.filter(organization -> organization.getOrgId() == Integer.parseInt(employeeOrgTree.getTree().firstEntry().getValue().getOrgId()))
					.map(Organization::getParentId)
					.filter(Objects::nonNull)
					.map(orgId -> organizations.stream().filter(parent -> parent.getOrgId().equals(orgId)).findFirst().orElse(null))
					.map(parent -> OrganizationLine.builder().withLevel(parent.getTreeLevel()).withOrgId(String.valueOf(parent.getOrgId())).withOrgName(parent.getOrgName()).build())
					.findAny()
					.ifPresent(employeeOrgTree::addOrg);

				// Initiate employee checklist
				final var result = employeeChecklistIntegration.initiateEmployee(municipalityId, employee, employeeOrgTree);
				employeeChecklistResponse.getDetails().add(toDetail(OK, result));
			} catch (final ThrowableProblem e) {
				employeeChecklistResponse.getDetails().add(toDetail(e.getStatus(), e.getMessage()));
			} catch (final Exception e) {
				LOGGER.error("Exception occurred when creating employee checklist", e);
				employeeChecklistResponse.getDetails().add(toDetail(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR.getReasonPhrase() + ": " + e.getMessage()));
			}
		});

		return employeeChecklistResponse;
	}

	private EmployeeChecklistResponse buildNoMatchResponse() {
		LOGGER.info("No employees found matching provided filter");
		return EmployeeChecklistResponse.builder()
			.withSummary("No employees found")
			.build();
	}

	public OngoingEmployeeChecklists getOngoingEmployeeChecklists(final OngoingEmployeeChecklistParameters parameters) {
		final var page = employeeChecklistIntegration.fetchAllOngoingEmployeeChecklists(parameters, toPageRequest(parameters));

		final var checklists = page.stream()
			.map(EmployeeChecklistMapper::mapToOngoingEmployeeChecklist)
			.toList();

		return OngoingEmployeeChecklists.builder()
			.withChecklists(checklists)
			.withMetadata(toPagingMetaData(page))
			.build();
	}

	/**
	 * Process ongoing checklists and update manager information where the information is out of sync
	 *
	 * @param  municipalityId the id of the municipality to filter checklists on
	 * @return                EmployeeChecklistResponse containing information of the execution
	 */
	public EmployeeChecklistResponse updateManagerInformation(String municipalityId, String username) {
		LOGGER.info("Processing checklists for municipalityId {} and updating those with outdated manager information", toSecureString(municipalityId));

		// If username is present, only fetch checklist for that person (disregarding if the checklist is completed or not).
		// Otherwise fetch all ongoing checklists.
		final var ongoingChecklists = isNull(username) ? employeeChecklistIntegration.findOngoingChecklists(municipalityId)
			: employeeChecklistIntegration.fetchOptionalEmployeeChecklist(municipalityId, username)
				.map(List::of)
				.orElse(emptyList());

		final List<Detail> updateResultDetails = new ArrayList<>();

		ongoingChecklists.stream()
			.map(EmployeeChecklistEntity::getEmployee)
			.forEach(localEmployee -> employeeIntegration.getEmployeeInformation(municipalityId, localEmployee.getId()).stream()
				.findAny()
				.ifPresent(remoteEmployee -> updateManagerInformation(updateResultDetails, localEmployee, remoteEmployee)));

		return toUpdateManagerResponse(ongoingChecklists.size(), updateResultDetails);
	}

	private void updateManagerInformation(final List<Detail> updateResultDetails, EmployeeEntity localEmployee, Employee remoteEmployee) {
		Detail detail = null;

		try {
			if (notEqual(remoteEmployee.getMainEmployment().getManager().getPersonId(), localEmployee.getManager().getPersonId())) {
				// First calculate information for response as local entity will be modified in next step
				detail = toDetail(OK, createUpdateManagerDetailString(localEmployee, remoteEmployee));
				// Update employee entity with new manager
				employeeChecklistIntegration.updateEmployeeInformation(localEmployee, remoteEmployee);
			}
		} catch (final Exception e) {
			LOGGER.error("Error when updating manager information for {} {} ({})", localEmployee.getFirstName(), localEmployee.getLastName(), localEmployee.getUsername(), e);
			detail = toDetail(INTERNAL_SERVER_ERROR, createUpdateManagerErrorString(localEmployee, e));
		} finally {
			ofNullable(detail).ifPresent(updateResultDetails::add);
		}
	}
}
