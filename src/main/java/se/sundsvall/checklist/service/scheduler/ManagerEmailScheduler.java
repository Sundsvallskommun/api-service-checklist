package se.sundsvall.checklist.service.scheduler;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.ERROR;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.checklist.integration.db.model.CorrespondenceEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.service.CommunicationService;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

@Component
public class ManagerEmailScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagerEmailScheduler.class);
	private static final String LOG_SEND_MANAGER_EMAIL_STARTED = "Beginning sending of email to employee managers";
	private static final String LOG_SEND_MANAGER_EMAIL_ENDED = "Ending sending of email to employee managers";
	private final Consumer<String> emailSetUnHealthyConsumer;

	private final CommunicationService communicationService;
	private final ChecklistProperties checklistProperties;

	@Value("${checklist.manager-email.name}")
	private String schedulerName;

	public ManagerEmailScheduler(
		final ChecklistProperties checklistProperties,
		final CommunicationService communicationService,
		final Dept44HealthUtility dept44HealthUtility) {

		this.communicationService = communicationService;
		this.emailSetUnHealthyConsumer = msg -> dept44HealthUtility.setHealthIndicatorUnhealthy(schedulerName, "Communication service error: %s".formatted(msg));
		this.checklistProperties = checklistProperties;
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
		final var receivers = communicationService.fetchManagersToSendMailTo(municipalityId);
		final var successfulSends = receivers.stream()
			.map(this::sendEmail)
			.filter(BooleanUtils::isTrue)
			.count();

		if (successfulSends != receivers.size()) {
			LOGGER.error("{} of {} emails encountered an exception while being sent", receivers.size() - successfulSends, receivers.size());
			emailSetUnHealthyConsumer.accept("%s of %s emails encountered an exception while being sent".formatted(receivers.size() - successfulSends, receivers.size()));
		}
	}

	private boolean sendEmail(EmployeeChecklistEntity entity) {
		try {
			// Send email
			communicationService.sendEmail(entity);

			// As correspondence status is updated on entity when sending email, check and return signal that it is not equal to
			// ERROR (which indicates that a southbound exception has occurred)
			return Optional.ofNullable(entity.getCorrespondence())
				.map(CorrespondenceEntity::getCorrespondenceStatus)
				.filter(status -> Objects.equals(status, ERROR))
				.isEmpty();

		} catch (final Exception e) {
			// Belt-and-braces policy to log unhandled exceptions and keep execution going
			LOGGER.error("Error when sending manager email", e);
			return false;
		}
	}
}
