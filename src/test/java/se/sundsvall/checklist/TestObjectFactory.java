package se.sundsvall.checklist;

import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.EMAIL;
import static se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel.NO_COMMUNICATION;
import static se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus.SENT;
import static se.sundsvall.checklist.integration.db.model.enums.LifeCycle.CREATED;
import static se.sundsvall.checklist.integration.db.model.enums.Permission.ADMIN;
import static se.sundsvall.checklist.integration.db.model.enums.QuestionType.YES_OR_NO;
import static se.sundsvall.checklist.integration.db.model.enums.QuestionType.YES_OR_NO_WITH_TEXT;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.NEW_EMPLOYEE;
import static se.sundsvall.checklist.integration.db.model.enums.RoleType.MANAGER_FOR_NEW_EMPLOYEE;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import generated.se.sundsvall.employee.Employee;
import generated.se.sundsvall.employee.Employment;
import generated.se.sundsvall.employee.Manager;
import generated.se.sundsvall.employee.PortalPersonData;
import se.sundsvall.checklist.api.model.Checklist;
import se.sundsvall.checklist.api.model.ChecklistCreateRequest;
import se.sundsvall.checklist.api.model.ChecklistUpdateRequest;
import se.sundsvall.checklist.api.model.Organization;
import se.sundsvall.checklist.api.model.OrganizationCreateRequest;
import se.sundsvall.checklist.api.model.OrganizationUpdateRequest;
import se.sundsvall.checklist.api.model.Phase;
import se.sundsvall.checklist.api.model.PhaseCreateRequest;
import se.sundsvall.checklist.api.model.PhaseUpdateRequest;
import se.sundsvall.checklist.api.model.Task;
import se.sundsvall.checklist.api.model.TaskCreateRequest;
import se.sundsvall.checklist.api.model.TaskUpdateRequest;
import se.sundsvall.checklist.integration.db.model.ChecklistEntity;
import se.sundsvall.checklist.integration.db.model.CorrespondenceEntity;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.FulfilmentEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.model.MentorEntity;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.model.PhaseEntity;
import se.sundsvall.checklist.integration.db.model.TaskEntity;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;

public final class TestObjectFactory {

	private TestObjectFactory() {}

	public static EmployeeChecklistEntity createEmployeeChecklistEntity() {
		return createEmployeeChecklistEntity(false);
	}

	public static EmployeeChecklistEntity createEmployeeChecklistEntity(boolean locked) {
		return EmployeeChecklistEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withEmployee(createEmployeeEntity())
			.withCorrespondence(createCorrespondenceEntity())
			.withChecklist(createChecklistEntity())
			.withCustomTasks(new ArrayList<>(List.of(createCustomTaskEntity())))
			.withFulfilments(new ArrayList<>(List.of(createFulfilmentEntity())))
			.withEndDate(LocalDate.now().plusDays(5))
			.withStartDate(LocalDate.now().plusDays(3))
			.withExpirationDate(LocalDate.now().plusDays(10))
			.withLocked(locked)
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withUpdated(OffsetDateTime.now())
			.withMentor(MentorEntity.builder()
				.withUserId("someUserId")
				.withName("someName")
				.build())
			.build();
	}

	public static ChecklistEntity createChecklistEntity() {
		return ChecklistEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withName("Test checklist template")
			.withVersion(1)
			.withLifeCycle(CREATED)
			.withPhases(new ArrayList<>(List.of(createPhaseEntity(), createPhaseEntity())))
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withUpdated(OffsetDateTime.now())
			.build();
	}

	public static PhaseEntity createPhaseEntity() {
		return PhaseEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withCreated(OffsetDateTime.now())
			.withUpdated(OffsetDateTime.now())
			.withLastSavedBy("someUser")
			.withBodyText("Test body text")
			.withName("Test name")
			.withSortOrder(1)
			.withPermission(ADMIN)
			.withTasks(new ArrayList<>(List.of(createTaskEntity(), createTaskEntity())))
			.withTimeToComplete("Test time to complete")
			.build();
	}

	public static TaskEntity createTaskEntity() {
		return TaskEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withUpdated(OffsetDateTime.now())
			.withLastSavedBy("someUser")
			.withRoleType(NEW_EMPLOYEE)
			.withPermission(ADMIN)
			.withQuestionType(YES_OR_NO)
			.withHeading("Test heading")
			.withText("Test text")
			.build();
	}

	public static CorrespondenceEntity createCorrespondenceEntity() {
		return CorrespondenceEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withMessageId("Test message id")
			.withAttempts(1)
			.withCorrespondenceStatus(SENT)
			.withRecipient("Test recipient")
			.withSent(OffsetDateTime.now())
			.build();
	}

	public static OrganizationEntity createOrganizationEntity() {
		return OrganizationEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withOrganizationName("Test organization")
			.withOrganizationNumber(1234)
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withUpdated(OffsetDateTime.now())
			.withCommunicationChannels(Set.of(EMAIL))
			.withChecklists(new ArrayList<>(List.of()))
			.build();
	}

	public static CustomTaskEntity createCustomTaskEntity() {
		return CustomTaskEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withHeading("Test heading")
			.withQuestionType(YES_OR_NO)
			.withPhase(createPhaseEntity())
			.withText("Test text")
			.withUpdated(OffsetDateTime.now())
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withLastSavedBy("someUser")
			.withRoleType(NEW_EMPLOYEE)
			.withSortOrder(1)
			.build();
	}

	public static DelegateEntity createDelegateEntity() {
		return DelegateEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withPartyId(UUID.randomUUID().toString())
			.withEmail("email")
			.withUsername("Test user name")
			.withFirstName("Test first name")
			.withLastName("Test last name")
			.withEmployeeChecklist(createEmployeeChecklistEntity())
			.build();
	}

	public static EmployeeEntity createEmployeeEntity() {
		return EmployeeEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withFirstName("Test first name")
			.withLastName("Test last name")
			.withEmail("Test email")
			.withCompany(createOrganizationEntity())
			.withTitle("Test title")
			.withUsername("Test user name")
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withUpdated(OffsetDateTime.now())
			.withManager(createManagerEntity())
			.build();
	}

	public static OrganizationCreateRequest createOrganizationCreateRequest() {
		return OrganizationCreateRequest.builder()
			.withOrganizationName("Test organization")
			.withOrganizationNumber(1234)
			.withCommunicationChannels(Set.of(EMAIL))
			.build();
	}

	public static OrganizationUpdateRequest createOrganizationUpdateRequest() {
		return OrganizationUpdateRequest.builder()
			.withOrganizationName("new organization name")
			.withCommunicationChannels(Set.of(NO_COMMUNICATION))
			.build();
	}

	public static Organization createOrganization() {
		return Organization.builder()
			.withId(UUID.randomUUID().toString())
			.withOrganizationName("Test organization")
			.withOrganizationNumber(1234)
			.withCommunicationChannels(Set.of(EMAIL))
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withUpdated(OffsetDateTime.now())
			.build();
	}

	public static ManagerEntity createManagerEntity() {
		return ManagerEntity.builder()
			.withPersonId(UUID.randomUUID().toString())
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withUpdated(OffsetDateTime.now())
			.withFirstName("Test first name")
			.withLastName("Test last name")
			.build();
	}

	public static FulfilmentEntity createFulfilmentEntity() {
		return FulfilmentEntity.builder()
			.withId(UUID.randomUUID().toString())
			.withTask(createTaskEntity())
			.withResponseText("Test response text")
			.withCompleted(FulfilmentStatus.TRUE)
			.build();
	}

	public static Checklist createChecklist() {
		return Checklist.builder()
			.withId(UUID.randomUUID().toString())
			.withName("Test checklist template")
			.withVersion(1)
			.withLifeCycle(CREATED)
			.withPhases(new ArrayList<>(List.of(createPhase(), createPhase())))
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withUpdated(OffsetDateTime.now())
			.build();
	}

	public static PhaseCreateRequest createPhaseCreateRequest(final Consumer<PhaseCreateRequest> modifier) {
		final var request = PhaseCreateRequest.builder()
			.withName("name")
			.withBodyText("bodyText")
			.withTimeToComplete("P2W")
			.withPermission(ADMIN)
			.withSortOrder(1)
			.withCreatedBy("someUser")
			.build();

		if (modifier != null) {
			modifier.accept(request);
		}
		return request;
	}

	public static PhaseCreateRequest createPhaseCreateRequest() {
		return createPhaseCreateRequest(null);
	}

	public static PhaseUpdateRequest createPhaseUpdateRequest() {
		return PhaseUpdateRequest.builder()
			.withName("new name")
			.withBodyText("new body text")
			.withTimeToComplete("P2W")
			.withPermission(ADMIN)
			.withSortOrder(1)
			.withUpdatedBy("someUser")
			.build();
	}

	public static ChecklistCreateRequest createChecklistCreateRequest() {
		return ChecklistCreateRequest.builder()
			.withName("Test checklist template")
			.withOrganizationNumber(1)
			.withDisplayName("Test display name")
			.withCreatedBy("someUser")
			.build();
	}

	public static ChecklistUpdateRequest createChecklistUpdateRequest() {
		return ChecklistUpdateRequest.builder()
			.withDisplayName("Updated displayname")
			.withUpdatedBy("someUser")
			.build();
	}

	public static TaskCreateRequest createTaskCreateRequest(final Consumer<TaskCreateRequest> modifier) {
		final var request = TaskCreateRequest.builder()
			.withHeading("Test heading")
			.withText("Test text")
			.withRoleType(MANAGER_FOR_NEW_EMPLOYEE)
			.withPermission(ADMIN)
			.withQuestionType(YES_OR_NO)
			.withSortOrder(1)
			.withCreatedBy("someUser")
			.build();

		if (modifier != null) {
			modifier.accept(request);
		}
		return request;
	}

	public static TaskCreateRequest createTaskCreateRequest() {
		return createTaskCreateRequest(null);
	}

	public static TaskUpdateRequest createTaskUpdateRequest() {
		return TaskUpdateRequest.builder()
			.withHeading("new heading")
			.withText("new text")
			.withRoleType(NEW_EMPLOYEE)
			.withPermission(ADMIN)
			.withQuestionType(YES_OR_NO_WITH_TEXT)
			.withSortOrder(1)
			.withUpdatedBy("someUser")
			.build();
	}

	public static Phase createPhase(final Consumer<Phase> modifier) {
		final var phase = Phase.builder()
			.withId(UUID.randomUUID().toString())
			.withCreated(OffsetDateTime.now())
			.withUpdated(OffsetDateTime.now())
			.withBodyText("Test body text")
			.withName("Test name")
			.withSortOrder(1)
			.withTasks(new ArrayList<>(List.of(createTask(), createTask())))
			.withPermission(ADMIN)
			.withTimeToComplete("P2W")
			.build();

		if (modifier != null) {
			modifier.accept(phase);
		}

		return phase;
	}

	public static Phase createPhase() {
		return createPhase(null);
	}

	public static Task createTask(final Consumer<Task> modifier) {
		final var task = Task.builder()
			.withCreated(OffsetDateTime.now().minusWeeks(1))
			.withUpdated(OffsetDateTime.now())
			.withRoleType(NEW_EMPLOYEE)
			.withQuestionType(YES_OR_NO)
			.withPermission(ADMIN)
			.withHeading("Test heading")
			.withText("Test text")
			.build();

		if (modifier != null) {
			modifier.accept(task);
		}

		return task;
	}

	public static Task createTask() {
		return createTask(null);
	}

	public static Employee generateEmployee(final UUID uuid) {
		return new generated.se.sundsvall.employee.Employee()
			.personId(uuid)
			.givenname("Test")
			.lastname("Testsson")
			.loginname("tes10tes")
			.isManager(false)
			.employments(generateEmployments());
	}

	public static List<Employment> generateEmployments() {
		return new ArrayList<>(List.of(generateEmployment()));
	}

	public static Employment generateEmployment() {
		return new generated.se.sundsvall.employee.Employment()
			.companyId(1)
			.isMainEmployment(true)
			.isManual(false)
			.manager(generateManager(UUID.randomUUID()));
	}

	public static Manager generateManager(final UUID uuid) {
		return new generated.se.sundsvall.employee.Manager()
			.givenname("manager")
			.lastname("managersson")
			.loginname("man10man")
			.emailAddress("manager@sundsvall.se")
			.personId(uuid);
	}

	public static PortalPersonData generatePortalPersonData(final UUID uuid) {
		return new PortalPersonData()
			.personid(uuid)
			.companyId(1)
			.givenname("Test")
			.lastname("Testsson")
			.loginName("PERSONAL\\tes10tes")
			.email("test@test.com")
			.isManager(false);
	}
}
