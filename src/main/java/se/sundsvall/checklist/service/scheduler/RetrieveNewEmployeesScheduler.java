package se.sundsvall.checklist.service.scheduler;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static se.sundsvall.checklist.service.mapper.EmployeeChecklistMapper.toInitiationInfoEntity;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.checklist.integration.db.repository.InitiationRepository;
import se.sundsvall.checklist.service.EmployeeChecklistService;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

/**
 * Scheduler for fetching and persisting new employees from the employee service and creating a checklist for each new
 * employee.
 */
@Component
public class RetrieveNewEmployeesScheduler {
	private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveNewEmployeesScheduler.class);
	private static final String LOG_USER_IMPORT_STARTED = "Beginning import of new users from Employee to persistance layer";
	private static final String LOG_USER_IMPORT_ENDED = "Ending import of new users from Employee to persistance layer";
	private static final String LOG_PROCESSING_MUNICIPALITY = "Processing municipality {}";

	private final ChecklistProperties properties;
	private final EmployeeChecklistService employeeChecklistService;
	private final InitiationRepository initiationRepository;

	public RetrieveNewEmployeesScheduler(EmployeeChecklistService employeeChecklistService, ChecklistProperties properties, InitiationRepository initiationRepository) {
		this.employeeChecklistService = employeeChecklistService;
		this.properties = properties;
		this.initiationRepository = initiationRepository;
	}

	@Dept44Scheduled(
		name = "${checklist.new-employees.name}",
		cron = "${checklist.new-employees.cron}",
		lockAtMostFor = "${checklist.new-employees.lockAtMostFor}",
		maximumExecutionTime = "${checklist.new-employees.maximumExecutionTime}")
	public void execute() {
		LOGGER.info(LOG_USER_IMPORT_STARTED);

		if (isEmpty(properties.managedMunicipalityIds())) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "No managed municipalities was found, please verify service properties.");
		}

		properties.managedMunicipalityIds()
			.forEach(this::handleInitiationForEmployees);

		LOGGER.info(LOG_USER_IMPORT_ENDED);
	}

	private void handleInitiationForEmployees(String municipalityId) {
		LOGGER.info(LOG_PROCESSING_MUNICIPALITY, municipalityId);

		final var result = employeeChecklistService.initiateEmployeeChecklists(municipalityId);

		var initiationResults = result.getDetails().stream()
			.map(detail -> toInitiationInfoEntity(municipalityId, detail))
			.toList();

		initiationRepository.saveAll(initiationResults);
		LOGGER.info(result.getSummary());
		ofNullable(result.getDetails()).orElse(emptyList()).stream()
			.filter(d -> !Objects.equals(d.getStatus(), Status.OK)) // Only log NOK results
			.forEach(d -> LOGGER.info("%s %s".formatted(d.getStatus(), d.getInformation())));
	}
}
