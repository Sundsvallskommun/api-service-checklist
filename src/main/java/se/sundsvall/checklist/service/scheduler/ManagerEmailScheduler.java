package se.sundsvall.checklist.service.scheduler;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.service.CommunicationService;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

@Component
public class ManagerEmailScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagerEmailScheduler.class);
	private static final String LOG_SEND_MANAGER_EMAIL_STARTED = "Beginning sending of email to employee managers";
	private static final String LOG_SEND_MANAGER_EMAIL_ENDED = "Ending sending of email to employee managers";
	private static final String HEALTH_MESSAGE = "Communication service error: %s email has encountered exception while being processed and needs to be investigated";

	private final CommunicationService communicationService;
	private final ChecklistProperties checklistProperties;
	private final Consumer<Integer> emailHealthConsumer;

	@Value("${checklist.manager-email.name}")
	private String schedulerName;

	public ManagerEmailScheduler(
		final ChecklistProperties checklistProperties,
		final CommunicationService communicationService,
		final Dept44HealthUtility dept44HealthUtility) {

		this.communicationService = communicationService;
		this.checklistProperties = checklistProperties;
		this.emailHealthConsumer = errorCount -> {
			if (errorCount == 0) {
				dept44HealthUtility.setHealthIndicatorHealthy(schedulerName);
			} else {
				dept44HealthUtility.setHealthIndicatorUnhealthy(schedulerName, HEALTH_MESSAGE.formatted(errorCount));
			}
		};
	}

	@Dept44Scheduled(
		name = "${checklist.manager-email.name}",
		cron = "${checklist.manager-email.cron}",
		lockAtMostFor = "${checklist.manager-email.lockAtMostFor}",
		maximumExecutionTime = "${checklist.manager-email.maximumExecutionTime}")
	public void execute() {
		LOGGER.info(LOG_SEND_MANAGER_EMAIL_STARTED);

		if (isEmpty(checklistProperties.managedMunicipalityIds())) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "No managed municipalities was found, please verify service properties.");
		}

		checklistProperties.managedMunicipalityIds()
			.forEach(this::handleEmailCommunication);

		LOGGER.info(LOG_SEND_MANAGER_EMAIL_ENDED);
	}

	private void handleEmailCommunication(String municipalityId) {
		// Send email to all new employees that haven't sent any email yet and where the company has opted in to send emails and
		// to all employees where previous send request didn't succeed

		communicationService.fetchManagersToSendMailTo(municipalityId)
			.forEach(this::sendEmail);

		// Set health depending on count of correspondences with error status
		emailHealthConsumer.accept(communicationService.countCorrespondenceWithErrors());
	}

	private void sendEmail(EmployeeChecklistEntity entity) {
		try {
			communicationService.sendEmail(entity);
		} catch (final Exception e) {
			// Belt-and-braces policy to log unhandled exceptions and keep execution going
			LOGGER.error("Error when sending manager email", e);
		}
	}
}
