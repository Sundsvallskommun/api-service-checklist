package se.sundsvall.checklist.service;

import static org.zalando.problem.Status.CONFLICT;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.integration.employee.EmployeeFilterBuilder.buildUuidEmployeeFilter;
import static se.sundsvall.checklist.service.mapper.DelegateMapper.toDelegateEntity;
import static se.sundsvall.checklist.service.util.EmployeeChecklistDecorator.decorateWithCustomTasks;
import static se.sundsvall.checklist.service.util.EmployeeChecklistDecorator.decorateWithFulfilment;
import static se.sundsvall.checklist.service.util.ServiceUtils.fetchEntity;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.checklist.api.model.DelegatedEmployeeChecklistResponse;
import se.sundsvall.checklist.api.model.EmployeeChecklist;
import se.sundsvall.checklist.integration.db.EmployeeChecklistIntegration;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.DelegateRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.employee.EmployeeIntegration;
import se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper;
import se.sundsvall.checklist.service.util.ServiceUtils;

@Service
public class DelegationService {

	private static final String EMPLOYEE_CHECKLIST_NOT_FOUND = "Employee checklist with id %s was not found.";
	private static final String EMPLOYEE_NOT_FOUND = "Employee with email %s was not found.";

	private final EmployeeChecklistRepository employeeChecklistRepository;
	private final DelegateRepository delegateRepository;
	private final EmployeeIntegration employeeIntegration;
	private final EmployeeChecklistIntegration employeeChecklistIntegration;
	private final CustomTaskRepository customTaskRepository;
	private final Duration employeeInformationUpdateInterval;

	public DelegationService(final EmployeeChecklistRepository employeeChecklistRepository,
		final DelegateRepository delegateRepository,
		final EmployeeIntegration employeeIntegration,
		final EmployeeChecklistIntegration employeeChecklistIntegration,
		final CustomTaskRepository customTaskRepository,
		@Value("${checklist.employee-update-interval}") Duration employeeInformationUpdateInterval) {

		this.employeeChecklistRepository = employeeChecklistRepository;
		this.delegateRepository = delegateRepository;
		this.employeeIntegration = employeeIntegration;
		this.employeeChecklistIntegration = employeeChecklistIntegration;
		this.customTaskRepository = customTaskRepository;
		this.employeeInformationUpdateInterval = employeeInformationUpdateInterval;
	}

	public void delegateEmployeeChecklist(final String municipalityId, final String employeeChecklistId, final String email) {
		final var employeeChecklist = employeeChecklistRepository.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, EMPLOYEE_CHECKLIST_NOT_FOUND.formatted(employeeChecklistId)));

		final var employeeData = employeeIntegration.getEmployeeByEmail(email)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, EMPLOYEE_NOT_FOUND.formatted(email)));

		delegateRepository.findByEmployeeChecklistAndEmail(employeeChecklist, email).ifPresentOrElse(o -> {
			throw Problem.valueOf(CONFLICT, "Employee checklist with id %s is already delegated to %s".formatted(employeeChecklistId, email));
		}, () -> {
			final var delegate = toDelegateEntity(employeeData, employeeChecklist);
			employeeChecklist.getDelegates().add(delegate);
			employeeChecklistRepository.save(employeeChecklist);
		});
	}

	public DelegatedEmployeeChecklistResponse fetchDelegatedEmployeeChecklistsByUsername(final String municipalityId, final String username) {
		final var delegatedEmployeeChecklistEntities = delegateRepository.findAllByUsername(username)
			.stream()
			.map(DelegateEntity::getEmployeeChecklist)
			.toList();

		return DelegatedEmployeeChecklistResponse.builder()
			.withEmployeeChecklists(toEmployeeChecklists(municipalityId, delegatedEmployeeChecklistEntities))
			.build();
	}

	private List<EmployeeChecklist> toEmployeeChecklists(String municipalityId, List<EmployeeChecklistEntity> delegatedEmployeeChecklistEntities) {
		return delegatedEmployeeChecklistEntities.stream()
			.map(this::handleUpdatedEmployeeInformation)
			.map(EmployeeChecklistMapper::toEmployeeChecklist)
			.map(ob -> decorateWithCustomTasks(ob, customTaskRepository.findAllByEmployeeChecklistIdAndEmployeeChecklistChecklistsMunicipalityId(ob.getId(), municipalityId)))
			.map(ob -> decorateWithFulfilment(ob, fetchEntity(delegatedEmployeeChecklistEntities, ob.getId())))
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

	@Transactional
	public void removeEmployeeChecklistDelegation(final String municipalityId, final String employeeChecklistId, final String email) {
		final var employeeChecklist = employeeChecklistRepository.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, EMPLOYEE_CHECKLIST_NOT_FOUND.formatted(employeeChecklistId)));

		if (delegateRepository.existsByEmployeeChecklistAndEmail(employeeChecklist, email)) {
			delegateRepository.deleteByEmployeeChecklistAndEmail(employeeChecklist, email);
		}
	}
}
