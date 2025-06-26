package se.sundsvall.checklist.service.scheduler;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.checklist.api.model.EmployeeChecklistResponse;
import se.sundsvall.checklist.service.EmployeeChecklistService;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

/**
 * Scheduler for updating employees with correct manager where this information is out of date.
 */
@Component
public class UpdateEmployeeManagerScheduler {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateEmployeeManagerScheduler.class);
	private static final String LOG_PROCESS_STARTED = "Starting to process ongoing checklists for update of employee manager where information is out of date";
	private static final String LOG_PROCESS_ENDED = "Finished processing ongoing checklists to update employee manager where information is out of date";
	private static final String LOG_PROCESS_EXCEPTION = "{} occurred when processing ongoing checklists within municipality {}";
	private static final String LOG_PROCESSING_MUNICIPALITY = "Processing checklists for municipality {}";

	private final EmployeeChecklistService employeeChecklistService;
	private final ChecklistProperties properties;

	public UpdateEmployeeManagerScheduler(EmployeeChecklistService employeeChecklistService, ChecklistProperties properties) {
		this.employeeChecklistService = employeeChecklistService;
		this.properties = properties;
	}

	@Dept44Scheduled(
		name = "${checklist.update-manager.name}",
		cron = "${checklist.update-manager.cron}",
		lockAtMostFor = "${checklist.update-manager.lockAtMostFor}",
		maximumExecutionTime = "${checklist.update-manager.maximumExecutionTime}")
	public void execute() {
		LOGGER.info(LOG_PROCESS_STARTED);

		if (isEmpty(properties.managedMunicipalityIds())) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "No managed municipalities was found, please verify service properties.");
		}

		properties.managedMunicipalityIds()
			.stream()
			.map(this::processMunicipality)
			.filter(Objects::nonNull)
			.forEach(this::logResponse);

		LOGGER.info(LOG_PROCESS_ENDED);
	}

	private EmployeeChecklistResponse processMunicipality(String municipalityId) {
		LOGGER.info(LOG_PROCESSING_MUNICIPALITY, municipalityId);
		try {
			return employeeChecklistService.updateManagerInformation(municipalityId, null);
		} catch (final Exception e) {
			LOGGER.error(LOG_PROCESS_EXCEPTION, e.getClass().getSimpleName(), municipalityId, e);
		}
		return null;
	}

	private void logResponse(EmployeeChecklistResponse response) {
		LOGGER.info(response.getSummary());
		ofNullable(response.getDetails()).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.forEach(detail -> LOGGER.info("%s %s".formatted(detail.getStatus(), detail.getInformation())));
	}
}
