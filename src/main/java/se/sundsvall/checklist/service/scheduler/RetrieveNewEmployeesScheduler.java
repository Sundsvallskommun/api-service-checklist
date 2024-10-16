package se.sundsvall.checklist.service.scheduler;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zalando.problem.Status;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import se.sundsvall.checklist.service.EmployeeChecklistService;
import se.sundsvall.dept44.requestid.RequestId;

/**
 * Scheduler for fetching and persisting new employees from the employee service and creating a checklist
 * for each new employee.
 */
@Component
public class RetrieveNewEmployeesScheduler {
	private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveNewEmployeesScheduler.class);
	private static final String LOG_USER_IMPORT_STARTED = "Beginning import of new users from Employee to persistance layer";
	private static final String LOG_USER_IMPORT_ENDED = "Ending import of new users from Employee to persistance layer";

	private final EmployeeChecklistService employeeChecklistService;

	public RetrieveNewEmployeesScheduler(EmployeeChecklistService employeeChecklistService) {
		this.employeeChecklistService = employeeChecklistService;
	}

	@Scheduled(cron = "${checklist.new-employees.cron:-}")
	@SchedulerLock(name = "fetchNewEmployees", lockAtMostFor = "${checklist.new-employees.shedlock-lock-at-most-for}")
	public void execute() {
		RequestId.init();
		LOGGER.info(LOG_USER_IMPORT_STARTED);
		handleInitiationForEmployees();
		LOGGER.info(LOG_USER_IMPORT_ENDED);
	}

	private void handleInitiationForEmployees() {
		final var result = employeeChecklistService.initiateEmployeeChecklists();
		LOGGER.info(result.getSummary());
		ofNullable(result.getDetails()).orElse(emptyList()).stream()
			.filter(d -> !Objects.equals(d.getStatus(), Status.OK)) // Only log NOK results
			.forEach(d -> LOGGER.info("%s %s".formatted(d.getStatus(), d.getInformation())));
	}
}
