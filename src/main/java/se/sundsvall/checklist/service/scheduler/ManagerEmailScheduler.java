package se.sundsvall.checklist.service.scheduler;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.service.CommunicationService;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Component
public class ManagerEmailScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagerEmailScheduler.class);
	private static final String LOG_SEND_MANAGER_EMAIL_STARTED = "Beginning sending of email to employee managers";
	private static final String LOG_SEND_MANAGER_EMAIL_ENDED = "Ending sending of email to employee managers";

	private final CommunicationService communicationService;
	private final ChecklistProperties properties;

	public ManagerEmailScheduler(final CommunicationService communicationService, final ChecklistProperties properties) {
		this.communicationService = communicationService;
		this.properties = properties;
	}

	@Dept44Scheduled(
		name = "${checklist.manager-email.name}",
		cron = "${checklist.manager-email.cron}",
		lockAtMostFor = "${checklist.manager-email.lockAtMostFor}",
		maximumExecutionTime = "${checklist.manager-email.maximumExecutionTime}")
	public void execute() {
		LOGGER.info(LOG_SEND_MANAGER_EMAIL_STARTED);

		if (isEmpty(properties.managedMunicipalityIds())) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "No managed municipalities was found, please verify service properties.");
		}

		properties.managedMunicipalityIds()
			.forEach(this::handleEmailCommunication);

		LOGGER.info(LOG_SEND_MANAGER_EMAIL_ENDED);
	}

	private void handleEmailCommunication(String municipalityId) {
		// Send email to all new employees that haven't sent any email yet and where the company has opted in to send emails and
		// to all employees where previous send request didn't succeed
		communicationService.fetchManagersToSendMailTo(municipalityId)
			.forEach(this::sendEmail);
	}

	private void sendEmail(EmployeeChecklistEntity entity) {
		try {
			communicationService.sendEmail(entity);
		} catch (final Exception e) {
			// Log exception but don't interrupt execution
			LOGGER.error("Exception occured when sending email", e);
		}
	}
}
