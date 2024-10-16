package se.sundsvall.checklist.service.scheduler;

import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.EMAIL;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.NOT_SENT;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.WILL_NOT_SEND;
import static se.sundsvall.checklist.service.mapper.CorrespondenceMapper.toCorrespondenceEntity;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.service.CommunicationService;

@Component
public class ManagerEmailScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagerEmailScheduler.class);
	private static final String LOG_SEND_MANAGER_EMAIL_STARTED = "Beginning sending of email to employee managers";
	private static final String LOG_SEND_MANAGER_EMAIL_ENDED = "Ending sending of email to employee managers";

	private final EmployeeChecklistRepository employeeChecklistRepository;

	private final CommunicationService communicationService;

	public ManagerEmailScheduler(final EmployeeChecklistRepository employeeChecklistRepository, final CommunicationService communicationService) {
		this.employeeChecklistRepository = employeeChecklistRepository;
		this.communicationService = communicationService;
	}

	@Scheduled(cron = "${checklist.manager-email.cron}")
	@SchedulerLock(name = "sendEmail", lockAtMostFor = "${checklist.manager-email.shedlock-lock-at-most-for}")
	public void execute() {
		LOGGER.info(LOG_SEND_MANAGER_EMAIL_STARTED);

		// Send email to all new employees that haven't sent any email yet and where the company has opted in to send emails
		employeeChecklistRepository
			.findAllByCorrespondenceIsNull()
			.stream()
			.filter(this::filterByCompanyWithEmailAsCommunicationChannel)
			.forEach(communicationService::sendEmail);

		// Send email to all employees where previous send request didn't succeed
		employeeChecklistRepository
			.findAllByCorrespondenceCorrespondenceStatus(NOT_SENT)
			.stream()
			.filter(this::filterByCompanyWithEmailAsCommunicationChannel)
			.forEach(communicationService::sendEmail);

		LOGGER.info(LOG_SEND_MANAGER_EMAIL_ENDED);
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
