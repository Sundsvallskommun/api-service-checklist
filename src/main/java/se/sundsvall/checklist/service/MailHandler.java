package se.sundsvall.checklist.service;

import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.EMAIL;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.ERROR;
import static se.sundsvall.checklist.integration.templating.TemplatingMapper.toRenderRequest;
import static se.sundsvall.checklist.service.mapper.CorrespondenceMapper.toCorrespondenceEntity;
import static se.sundsvall.checklist.service.mapper.CorrespondenceMapper.toCorrespondenceStatus;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.messaging.MessagingIntegration;
import se.sundsvall.checklist.integration.templating.TemplatingIntegration;

@Component
public class MailHandler {

	private final MessagingIntegration messagingIntegration;

	private final TemplatingIntegration templatingIntegration;

	private final EmployeeChecklistRepository employeeChecklistRepository;

	public MailHandler(MessagingIntegration messagingIntegration,
		TemplatingIntegration templatingIntegration,
		EmployeeChecklistRepository employeeChecklistRepository) {

		this.messagingIntegration = messagingIntegration;
		this.employeeChecklistRepository = employeeChecklistRepository;
		this.templatingIntegration = templatingIntegration;
	}

	@Transactional
	public void sendEmail(EmployeeChecklistEntity entity, String emailTemplate) {
		Optional.ofNullable(entity.getCorrespondence()).ifPresentOrElse(c -> { // Update existing object to reflect that email is the last used channel for communication together with managers email
			c.setCommunicationChannel(EMAIL);
			c.setRecipient(entity.getEmployee().getManager().getEmail());
		}, () -> entity.setCorrespondence(toCorrespondenceEntity(EMAIL, entity.getEmployee().getManager().getEmail()))); // Add correspondence object if it does not exist on entity

		templatingIntegration.renderTemplate(entity.getChecklists().getFirst().getMunicipalityId(), toRenderRequest(entity.getEmployee(), emailTemplate))
			.ifPresentOrElse(renderedTemplate -> sendManagerEmail(entity, renderedTemplate.getOutput()), () -> {
				entity.getCorrespondence().setCorrespondenceStatus(ERROR);
				entity.getCorrespondence().setAttempts(entity.getCorrespondence().getAttempts() + 1);
			});

		employeeChecklistRepository.save(entity);
	}

	private void sendManagerEmail(final EmployeeChecklistEntity entity, String renderedTemplate) {
		messagingIntegration.sendEmail(entity.getChecklists().getFirst().getMunicipalityId(), entity.getEmployee().getManager().getEmail(), renderedTemplate)
			.ifPresentOrElse(result -> {
				entity.getCorrespondence().setCorrespondenceStatus(toCorrespondenceStatus(result.getDeliveries()));
				entity.getCorrespondence().setMessageId(result.getMessageId().toString());
			}, () -> entity.getCorrespondence().setCorrespondenceStatus(ERROR));

		entity.getCorrespondence().setAttempts(entity.getCorrespondence().getAttempts() + 1);
	}

}
