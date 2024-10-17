package se.sundsvall.checklist.service.scheduler;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;

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

	private final EmployeeChecklistRepository employeeChecklistRepository;

	public LockEmployeeChecklistsScheduler(EmployeeChecklistRepository employeeChecklistRepository) {
		this.employeeChecklistRepository = employeeChecklistRepository;
	}

	/**
	 * Locks old employee checklists once a day, the time is not that important, hence the "once a day".
	 * When creating an employee checklist, the expiration date is set in the column "expirationDate" in the
	 * database and the job locks all employee checklists that have an expiration date that is before todays
	 * date.
	 */
	@Transactional
	@Scheduled(fixedRateString = "${checklist.lock-employee-checklists.periodical-invocation}")
	@SchedulerLock(name = "lockEmployeeChecklists", lockAtMostFor = "${checklist.lock-employee-checklists.shedlock-lock-at-most-for}")
	public void execute() {
		LOGGER.info(LOG_LOCK_EMPLOYEE_CHECKLISTS_STARTED);

		employeeChecklistRepository.findAllByExpirationDateIsBeforeAndLockedIsFalse(LocalDate.now()).stream()
			.forEach(entity -> {
				LOGGER.info(LOG_LOCKING_EMPLOYEE_CHECKLIST, entity.getId());
				entity.setLocked(true);
				employeeChecklistRepository.save(entity);
			});

		LOGGER.info(LOG_LOCK_EMPLOYEE_CHECKLISTS_ENDED);
	}
}
