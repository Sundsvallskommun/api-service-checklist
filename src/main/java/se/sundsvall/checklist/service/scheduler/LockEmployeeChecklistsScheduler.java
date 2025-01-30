package se.sundsvall.checklist.service.scheduler;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

/**
 * Checks for expired employee checklists and locks them.
 */
@Component
@ConditionalOnProperty(name = "checklist.lock-employee-checklists.enabled", matchIfMissing = true)
public class LockEmployeeChecklistsScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(LockEmployeeChecklistsScheduler.class);
	private static final String LOG_LOCK_EMPLOYEE_CHECKLISTS_STARTED = "Beginning execution for locking expired employee checklists";
	private static final String LOG_LOCKING_EMPLOYEE_CHECKLIST = "Locking employee checklist with id: {}";
	private static final String LOG_LOCK_EMPLOYEE_CHECKLISTS_ENDED = "Ended execution for locking expired employee checklists";
	private static final String LOG_PROCESSING_MUNICIPALITY = "Processing municipality {}";

	private final EmployeeChecklistRepository employeeChecklistRepository;
	private final ChecklistProperties properties;

	public LockEmployeeChecklistsScheduler(EmployeeChecklistRepository employeeChecklistRepository, ChecklistProperties properties) {
		this.employeeChecklistRepository = employeeChecklistRepository;
		this.properties = properties;
	}

	/**
	 * Locks old employee checklists. When creating an employee checklist, the expiration date is set in the column
	 * "expirationDate" in the database and the job locks all employee checklists that have an expiration date that is
	 * before todays date.
	 */
	@Transactional
	@Dept44Scheduled(
		name = "${checklist.lock-employee-checklists.name}",
		cron = "${checklist.lock-employee-checklists.cron}",
		lockAtMostFor = "${checklist.lock-employee-checklists.lockAtMostFor}",
		maximumExecutionTime = "${checklist.lock-employee-checklists.maximumExecutionTime}")
	public void execute() {
		LOGGER.info(LOG_LOCK_EMPLOYEE_CHECKLISTS_STARTED);

		if (isEmpty(properties.managedMunicipalityIds())) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "No managed municipalities was found, please verify service properties.");
		}

		properties.managedMunicipalityIds()
			.forEach(this::lockChecklists);

		LOGGER.info(LOG_LOCK_EMPLOYEE_CHECKLISTS_ENDED);
	}

	private void lockChecklists(String municipalityId) {
		LOGGER.info(LOG_PROCESSING_MUNICIPALITY, municipalityId);

		employeeChecklistRepository.findAllByChecklistsMunicipalityIdAndExpirationDateIsBeforeAndLockedIsFalse(municipalityId, LocalDate.now())
			.forEach(entity -> {
				LOGGER.info(LOG_LOCKING_EMPLOYEE_CHECKLIST, entity.getId());
				entity.setLocked(true);
				employeeChecklistRepository.save(entity);
			});
	}
}
