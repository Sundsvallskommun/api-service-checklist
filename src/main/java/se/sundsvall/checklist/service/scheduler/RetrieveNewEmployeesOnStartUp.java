package se.sundsvall.checklist.service.scheduler;

import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * We want the service to fetch and persist new employees on startup. This is not possible
 * with the @Scheduled annotation (because we use a cron pattern) and we also don't want it
 * to run directly on startup since that will mess up a lot of tests.
 *
 * If property <code>employee.new-employees.fetch-on-startup</code> is set to false the component will not
 * be instansiated and no fetch executed.
 */
@Component
@ConditionalOnProperty(name = "checklist.new-employees.fetch-on-startup", matchIfMissing = true)
public class RetrieveNewEmployeesOnStartUp {
	private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveNewEmployeesOnStartUp.class);

	private final RetrieveNewEmployeesScheduler retrieveNewEmployeesScheduler;

	public RetrieveNewEmployeesOnStartUp(
		RetrieveNewEmployeesScheduler retriveNewEmployeesScheduler,
		@Value("${checklist.new-employees.delay-on-startup:PT5S}") Duration onstartupDelay) {

		this.retrieveNewEmployeesScheduler = retriveNewEmployeesScheduler;
		checkForNewEmployees(onstartupDelay);
	}

	private void checkForNewEmployees(Duration onstartupDelay) {
		ScheduledExecutorService executor = null;
		try {
			executor = newSingleThreadScheduledExecutor();
			executor.schedule(() -> {
				LOGGER.info("Fetching new employees on application startup.");
				retrieveNewEmployeesScheduler.execute();
			}, onstartupDelay.getSeconds(), SECONDS);
		} finally {
			if (nonNull(executor)) {
				executor.shutdown();
			}
		}
	}
}
