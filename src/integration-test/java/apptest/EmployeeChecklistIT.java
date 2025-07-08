package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.DelegateRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeRepository;
import se.sundsvall.checklist.integration.db.repository.ManagerRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/EmployeeChecklistIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/employeeChecklistIT-testdata.sql"
})
class EmployeeChecklistIT extends AbstractAppTest {

	private static final String PATH_PREFIX = "/2281/employee-checklists";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Autowired
	private EmployeeChecklistRepository employeeChecklistRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private CustomTaskRepository customTaskRepository;

	@Autowired
	private DelegateRepository delegateRepository;

	@Autowired
	private ManagerRepository managerRepository;

	@Test
	void test01_fetchChecklistAsEmployee() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/employee/aemp0loyee")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_fetchChecklistAsEmployeeWhenManagerChanged() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/employee/cemp3loyee")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_fetchChecklistsAsManager() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/manager/aman0agr")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_fetchChecklistsAsManagerWhenEmployeeChangedManager() {
		final var filter = Example.of(ManagerEntity.builder().withUsername("fman4agr").build());

		// Verify that manager is not present in database before execution
		assertThat(managerRepository.count(filter)).isZero();

		setupCall()
			.withServicePath(PATH_PREFIX + "/manager/eman3agr")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

		// Verify that manager is created in database after execution
		assertThat(managerRepository.count(filter)).isOne();
	}

	@Test
	void test05_deleteEmployeeChecklist() {
		final var employeeChecklistId = "8fcc1fc7-bcda-4db6-9375-ff99961ef011";
		final var employeeId = "bfd69468-bd32-4b84-a3b0-c5e1742a5a34";
		final var managerId = "9d2adcf6-9234-4faf-a6c9-0c1c7518b534";
		final var filter = Example.of(EmployeeChecklistEntity.builder().withCompleted(true).withId(employeeChecklistId).build());

		assertThat(employeeChecklistRepository.count(filter)).isOne();
		assertThat(employeeRepository.existsById(employeeId)).isTrue();
		assertThat(managerRepository.existsById(managerId)).isTrue();

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(ALL_VALUE))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(employeeChecklistRepository.count(filter)).isZero();
		assertThat(employeeRepository.existsById(employeeId)).isFalse();
		assertThat(managerRepository.existsById(managerId)).isFalse();
	}

	@Test
	void test06_removeDelegatedEmployeeChecklist() {
		final var employeeChecklistId = "e4474a9b-1a57-49b8-bec8-2e50db600fbb";
		final var employeeId = "87b0d9c2-c06e-409d-b77e-63f427e0dbc2";
		final var managerId = "f59918bc-a8f1-4f97-abe3-9f80f26e6bf2";
		final var delegateId = "fcfff6b0-d66f-4f09-a77c-7b02979fbe07";
		final var employeeChecklistFilter = Example.of(EmployeeChecklistEntity.builder().withCompleted(true).withId(employeeChecklistId).build());
		final var delegateFilter = Example.of(DelegateEntity.builder().withId(delegateId).build());

		assertThat(employeeChecklistRepository.count(employeeChecklistFilter)).isOne();
		assertThat(employeeRepository.existsById(employeeId)).isTrue();
		assertThat(managerRepository.existsById(managerId)).isTrue();
		assertThat(delegateRepository.count(delegateFilter)).isOne();

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(ALL_VALUE))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(employeeChecklistRepository.count(employeeChecklistFilter)).isZero();
		assertThat(employeeRepository.existsById(employeeId)).isFalse();
		assertThat(managerRepository.existsById(managerId)).isTrue();
		assertThat(delegateRepository.count(delegateFilter)).isZero();
	}

	@Test
	void test07_createCustomTask() {
		final var employeeChecklistId = "855e7d4e-af50-4fd3-b81d-a71299f38d1a";
		final var phaseId = "3e9780a7-96f3-4d07-80ee-a9634b786a38";

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId + "/phases/" + phaseId + "/customtasks")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/employee-checklists/" + employeeChecklistId + "/customtasks/(.+)$"))
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_readCustomTask() {
		final var employeeChecklistId = "f5960058-fad8-4825-85f3-b0fdb518adc5";
		final var customTaskId = "1b3bfe66-0e6c-4e92-a410-7c620a5461f4";

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId + "/customtasks/" + customTaskId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_updateCustomTask() {
		final var employeeChecklistId = "855e7d4e-af50-4fd3-b81d-a71299f38d1a";
		final var customTaskId = "677a9efd-55bf-468d-81e8-efc913b9f956";

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId + "/customtasks/" + customTaskId)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_deleteCustomTask() {
		final var employeeChecklistId = "51ca9112-001b-4f12-b866-8d59ef1c25c4";
		final var customTaskId = "8b4eafc8-46ec-4fe3-9c2e-11625f144b10";
		final var filter = Example.of(CustomTaskEntity.builder().withId(customTaskId).build());

		assertThat(customTaskRepository.count(filter)).isOne();

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId + "/customtasks/" + customTaskId)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(ALL_VALUE))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(customTaskRepository.count(filter)).isZero();
	}

	@Test
	void test11_updateAllTasksInPhase() {
		final var employeeChecklistId = "855e7d4e-af50-4fd3-b81d-a71299f38d1a";
		final var phaseId = "7272d1fc-540e-4394-afe2-e133ca642e91";

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId + "/phases/" + phaseId)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test12_updateCommonTaskFulfilment() {
		final var employeeChecklistId = "855e7d4e-af50-4fd3-b81d-a71299f38d1a";
		final var taskId = "d250a20c-a616-4147-bfe0-19a0d12f3df0";

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId + "/tasks/" + taskId)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

	}

	@Test
	void test13_updateCustomTaskFulfilment() {
		final var employeeChecklistId = "855e7d4e-af50-4fd3-b81d-a71299f38d1a";
		final var taskId = "677a9efd-55bf-468d-81e8-efc913b9f956";

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId + "/tasks/" + taskId)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test14_initializeEmployeeChecklists() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/initialize")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test15_initializeChecklistForSpecificEmployee() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/initialize/af920bfe-630a-44e9-b2e7-53f092cd0225")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test16_fetchChecklistWithMentorSetAsEmployee() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/employee/jemp7loyee")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test17_setMentor() {
		final var employeeChecklistId = "cca064da-ab15-4276-8fbb-e8ba07b28718";

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId + "/mentor")
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(ACCEPTED)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(ALL_VALUE))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test18_deleteMentor() {
		final var employeeChecklistId = "cca064da-ab15-4276-8fbb-e8ba07b28718";

		var checklist = employeeChecklistRepository.findById(employeeChecklistId);
		assertThat(checklist).hasValueSatisfying(employeeChecklistEntity -> assertThat(employeeChecklistEntity.getMentor()).isNotNull());

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId + "/mentor")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(ALL_VALUE))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		checklist = employeeChecklistRepository.findById(employeeChecklistId);
		assertThat(checklist).hasValueSatisfying(employeeChecklistEntity -> assertThat(employeeChecklistEntity.getMentor()).isNull());
	}

	@Test
	void test19_fetchChecklistWithInheritedSortorder() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/employee/kemp8loyee")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

	}

	/**
	 * Fetch ongoing checklists with default pagination and sorting.
	 */
	@Test
	void test20_fetchOngoingChecklists_1() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/ongoing")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Fetch ongoing checklists, test that pagination works as expected.
	 */
	@Test
	void test21_fetchOngoingChecklists_2() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/ongoing?page=2&limit=2")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Fetch ongoing checklists, test that sort direction works as expected.
	 */
	@Test
	void test22_fetchOngoingChecklists_3() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/ongoing?sortDirection=DESC")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Fetch ongoing checklists, test that sort by works as expected.
	 */
	@Test
	void test23_fetchOngoingChecklists_4() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/ongoing?sortBy=employeeName&sortDirection=DESC")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Fetch ongoing checklists, test that filter by employee name works as expected.
	 */
	@Test
	void test24_fetchOngoingChecklists_5() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/ongoing?employeeName=Fred")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test25_fetchInitiationInfo() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/initiationinfo")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test26_fetchInitiationInfoFiltered() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/initiationinfo?onlyErrors=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	@DirtiesContext
	void test27_updateManagerOnOngoingChecklists() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/update-manager")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	@DirtiesContext
	void test28_updateManagerForSpecificEmployee() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/update-manager?username=bemp0loyee")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

}
