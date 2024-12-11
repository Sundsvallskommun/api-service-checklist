package se.sundsvall.checklist.service.scheduler;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.EMAIL;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.NOT_SENT;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.WILL_NOT_SEND;
import static se.sundsvall.checklist.service.mapper.CorrespondenceMapper.toCorrespondenceEntity;

import java.util.Optional;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.service.CommunicationService;
import se.sundsvall.dept44.requestid.RequestId;

@Component
public class ManagerEmailScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagerEmailScheduler.class);
	private static final String LOG_SEND_MANAGER_EMAIL_STARTED = "Beginning sending of email to employee managers";
	private static final String LOG_SEND_MANAGER_EMAIL_ENDED = "Ending sending of email to employee managers";

	private final EmployeeChecklistRepository employeeChecklistRepository;
	private final CommunicationService communicationService;
	private final ChecklistProperties properties;

	public ManagerEmailScheduler(final EmployeeChecklistRepository employeeChecklistRepository, final CommunicationService communicationService, final ChecklistProperties properties) {
		this.employeeChecklistRepository = employeeChecklistRepository;
		this.communicationService = communicationService;
		this.properties = properties;
	}

	@Scheduled(cron = "${checklist.manager-email.cron}")
	@SchedulerLock(name = "sendEmail", lockAtMostFor = "${checklist.manager-email.shedlock-lock-at-most-for}")
	public void execute() {
		try {
			RequestId.init();
			LOGGER.info(LOG_SEND_MANAGER_EMAIL_STARTED);

			if (isEmpty(properties.managedMunicipalityIds())) {
				throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "No managed municipalities was found, please verify service properties.");
			}

			properties.managedMunicipalityIds()
				.forEach(this::handleEmailCommunication);

			LOGGER.info(LOG_SEND_MANAGER_EMAIL_ENDED);
		} finally {
			RequestId.reset();
		}
	}

	private void handleEmailCommunication(String municipalityId) {
		// Send email to all new employees that haven't sent any email yet and where the company has opted in to send emails
		employeeChecklistRepository
			.findAllByChecklistsMunicipalityIdAndCorrespondenceIsNull(municipalityId)
			.stream()
			.filter(this::filterByCompanyWithEmailAsCommunicationChannel)
			.forEach(communicationService::sendEmail);

		// Send email to all employees where previous send request didn't succeed
		employeeChecklistRepository
			.findAllByChecklistsMunicipalityIdAndCorrespondenceCorrespondenceStatus(municipalityId, NOT_SENT)
			.stream()
			.filter(this::filterByCompanyWithEmailAsCommunicationChannel)
			.forEach(communicationService::sendEmail);
	}

	boolean filterByCompanyWithEmailAsCommunicationChannel(EmployeeChecklistEntity entity) {
		if (entity.getEmployee().getDepartment().getCommunicationChannels().contains(EMAIL)) {
			return true;
		}
		Optional.ofNullable(entity.getCorrespondence()).ifPresentOrElse(c -> { // Update existing object to reflect that email is the last used channel for communication together with managers email
			c.setCommunicationChannel(EMAIL);
			c.setRecipient(entity.getEmployee().getManager().getEmail());
		}, () -> entity.setCorrespondence(toCorrespondenceEntity(EMAIL, entity.getEmployee().getManager().getEmail()))); // Add correspondence object if it does not exist on entity

		// Finally set status to WILL_NOT_SEND as company has opted out of email correspondence
		entity.getCorrespondence().setCorrespondenceStatus(WILL_NOT_SEND);
		employeeChecklistRepository.save(entity);
		return false;
	}
}
