package se.sundsvall.checklist.service;

import static org.zalando.problem.Status.NOT_FOUND;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

	public void sendEmail(String municipalityId, String employeeChecklistId) {
		final var entity = employeeChecklistRepository.findByIdAndChecklistsMunicipalityId(employeeChecklistId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Employee checklist with id %s not found within municipality %s.".formatted(employeeChecklistId, municipalityId)));

		mailHandler.sendEmail(entity, emailTemplate);
	}

	public void sendEmail(EmployeeChecklistEntity entity) {
		mailHandler.sendEmail(entity, emailTemplate);
	}
}
