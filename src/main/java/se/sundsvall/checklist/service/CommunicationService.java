package se.sundsvall.checklist.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.EMAIL;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.ERROR;
import static se.sundsvall.checklist.integration.templating.TemplatingMapper.toRenderRequest;
import static se.sundsvall.checklist.service.mapper.CorrespondenceMapper.toCorrespondenceEntity;
import static se.sundsvall.checklist.service.mapper.CorrespondenceMapper.toCorrespondenceStatus;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.checklist.api.model.Correspondence;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.messaging.MessagingIntegration;
import se.sundsvall.checklist.integration.templating.TemplatingIntegration;
import se.sundsvall.checklist.service.mapper.CorrespondenceMapper;

@Service
public class CommunicationService {

	private final MessagingIntegration messagingIntegration;

	private final TemplatingIntegration templatingIntegration;

	private final EmployeeChecklistRepository employeeChecklistRepository;

	private final String emailTemplate;

	public CommunicationService(
		MessagingIntegration messagingIntegration,
		TemplatingIntegration templatingIntegration,
		EmployeeChecklistRepository employeeChecklistRepository,
		@Value("${checklist.manager-email.email-template}") String emailTemplate) {

		this.messagingIntegration = messagingIntegration;
		this.employeeChecklistRepository = employeeChecklistRepository;
		this.templatingIntegration = templatingIntegration;
		this.emailTemplate = emailTemplate;
	}

	public Correspondence fetchCorrespondence(String employeeChecklistId) {
		return employeeChecklistRepository.findById(employeeChecklistId)
			.map(EmployeeChecklistEntity::getCorrespondence)
			.map(CorrespondenceMapper::toCorrespondence)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Employee checklist with id %s not found".formatted(employeeChecklistId)));
	}

	public void sendEmail(String employeeChecklistId) {
		final var entity = employeeChecklistRepository.findById(employeeChecklistId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Employee checklist with id %s not found".formatted(employeeChecklistId)));

		sendEmail(entity);
	}

	public void sendEmail(EmployeeChecklistEntity entity) {
		Optional.ofNullable(entity.getCorrespondence()).ifPresentOrElse(c -> { // Update existing object to reflect that email is the last used channel for communication together with managers email
			c.setCommunicationChannel(EMAIL);
			c.setRecipient(entity.getEmployee().getManager().getEmail());
		}, () -> entity.setCorrespondence(toCorrespondenceEntity(EMAIL, entity.getEmployee().getManager().getEmail()))); // Add correspondence object if it does not exist on entity

		templatingIntegration.renderTemplate(toRenderRequest(entity.getEmployee(), emailTemplate))
			.ifPresentOrElse(renderedTemplate -> sendManagerEmail(entity, renderedTemplate.getOutput()), () -> {
				entity.getCorrespondence().setCorrespondenceStatus(ERROR);
				entity.getCorrespondence().setAttempts(entity.getCorrespondence().getAttempts() + 1);
			});

		employeeChecklistRepository.save(entity);
	}

	private void sendManagerEmail(final EmployeeChecklistEntity entity, String renderedTemplate) {
		messagingIntegration.sendEmail(entity.getEmployee().getManager().getEmail(), renderedTemplate)
			.ifPresentOrElse(result -> {
				entity.getCorrespondence().setCorrespondenceStatus(toCorrespondenceStatus(result.getDeliveries()));
				entity.getCorrespondence().setMessageId(result.getMessageId().toString());
			}, () -> entity.getCorrespondence().setCorrespondenceStatus(ERROR));

		entity.getCorrespondence().setAttempts(entity.getCorrespondence().getAttempts() + 1);
	}
}
