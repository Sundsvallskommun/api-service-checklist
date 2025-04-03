package se.sundsvall.checklist.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.EMAIL;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.ERROR;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.NOT_SENT;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.WILL_NOT_SEND;
import static se.sundsvall.checklist.service.mapper.CorrespondenceMapper.toCorrespondenceEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.checklist.api.model.Correspondence;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.service.mapper.CorrespondenceMapper;

@Service
public class CommunicationService {
	private final MailHandler mailHandler;

	private final EmployeeChecklistRepository employeeChecklistRepository;

	private final String emailTemplate;

	public CommunicationService(
		MailHandler mailHandler,
		EmployeeChecklistRepository employeeChecklistRepository,
		@Value("${checklist.manager-email.email-template}") String emailTemplate) {

		this.mailHandler = mailHandler;
		this.employeeChecklistRepository = employeeChecklistRepository;
		this.emailTemplate = emailTemplate;
	}

	public Correspondence fetchCorrespondence(String municipalityId, String employeeChecklistId) {
		return employeeChecklistRepository.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)
			.map(EmployeeChecklistEntity::getCorrespondence)
			.map(CorrespondenceMapper::toCorrespondence)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Employee checklist with id %s not found within municipality %s.".formatted(employeeChecklistId, municipalityId)));
	}

	@Transactional
	public List<EmployeeChecklistEntity> fetchManagersToSendMailTo(String municipalityId) {
		final var recipients = new ArrayList<>(employeeChecklistRepository.findAllByChecklistsMunicipalityIdAndCorrespondenceIsNull(municipalityId));
		recipients.addAll(employeeChecklistRepository.findAllByChecklistsMunicipalityIdAndCorrespondenceCorrespondenceStatus(municipalityId, NOT_SENT));

		return recipients.stream()
			.filter(this::filterByCompanyWithEmailAsCommunicationChannel)
			.toList();
	}

	@Transactional
	public int countCorrespondenceWithErrors() {
		return employeeChecklistRepository.countByCorrespondenceCorrespondenceStatus(ERROR);
	}

	boolean filterByCompanyWithEmailAsCommunicationChannel(EmployeeChecklistEntity entity) {
		if (entity.getEmployee().getDepartment().getCommunicationChannels().contains(EMAIL)) {
			return true;
		}
		Optional.ofNullable(entity.getCorrespondence()).ifPresentOrElse(correspondence -> { // Update existing object to reflect that email is the last used channel for communication together with managers email
			correspondence.setCommunicationChannel(EMAIL);
			correspondence.setRecipient(entity.getEmployee().getManager().getEmail());
		}, () -> entity.setCorrespondence(toCorrespondenceEntity(EMAIL, entity.getEmployee().getManager().getEmail()))); // Add correspondence object if it does not exist on entity

		// Finally set status to WILL_NOT_SEND as company has opted out of email correspondence
		entity.getCorrespondence().setCorrespondenceStatus(WILL_NOT_SEND);
		employeeChecklistRepository.save(entity);
		return false;
	}

	public void sendEmail(String municipalityId, String employeeChecklistId) {
		final var entity = employeeChecklistRepository.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Employee checklist with id %s not found within municipality %s.".formatted(employeeChecklistId, municipalityId)));

		sendEmail(entity);
	}

	public void sendEmail(EmployeeChecklistEntity entity) {
		mailHandler.sendEmail(entity, emailTemplate);
	}
}
