package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.test.context.jdbc.Sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.api.model.EmployeeChecklistPaginatedResponse;
import se.sundsvall.checklist.integration.db.model.CustomTaskEntity;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;
import se.sundsvall.checklist.integration.db.repository.CustomTaskRepository;
import se.sundsvall.checklist.integration.db.repository.DelegateRepository;
import se.sundsvall.checklist.integration.db.repository.EmployeeChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.ManagerRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/EmployeeChecklistIT/", classes = Application.class)
@Sql({
	"/sql/truncate.sql",
	"/sql/employeeChecklistIT-testdata.sql"
})
class EmployeeChecklistIT extends AbstractAppTest {

	private static final String PATH_PREFIX = "/employee-checklists";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Autowired
	private EmployeeChecklistRepository employeeChecklistRepository;

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
		final var filter = Example.of(ManagerEntity.builder().withUserName("fman4agr").build());

		// Verify that manager is not present in database before execution
		assertThat(managerRepository.findAll(filter)).isEmpty();

		setupCall()
			.withServicePath(PATH_PREFIX + "/manager/eman3agr")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

		// Verify that manager is created in database after execution
		assertThat(managerRepository.findAll(filter)).hasSize(1);
	}

	@Test
	void test05_deleteEmployeeChecklist() {
		final var employeeChecklistId = "8fcc1fc7-bcda-4db6-9375-ff99961ef011";
		final var filter = Example.of(EmployeeChecklistEntity.builder().withId(employeeChecklistId).build());

		assertThat(employeeChecklistRepository.count(filter)).isOne();

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(employeeChecklistRepository.count(filter)).isZero();
	}

	@Test
	void test06_removeDelegatedEmployeeChecklist() {
		final var employeeChecklistId = "e4474a9b-1a57-49b8-bec8-2e50db600fbb";
		final var delegateId = "fcfff6b0-d66f-4f09-a77c-7b02979fbe07";
		final var employeeChecklistFilter = Example.of(EmployeeChecklistEntity.builder().withId(employeeChecklistId).build());
		final var delegateFilter = Example.of(DelegateEntity.builder().withId(delegateId).build());

		assertThat(employeeChecklistRepository.count(employeeChecklistFilter)).isOne();
		assertThat(delegateRepository.count(delegateFilter)).isOne();

		setupCall()
			.withServicePath(PATH_PREFIX + "/" + employeeChecklistId)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(employeeChecklistRepository.count(employeeChecklistFilter)).isZero();
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
			.withExpectedResponseHeader(LOCATION, List.of("^/employee-checklists/" + employeeChecklistId + "/customtasks/(.+)$"))
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
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test14_initializeEmployeeChecklists() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/initialize")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test15_initializeChecklistForSpecificEmployee() {
		setupCall()
			.withServicePath(PATH_PREFIX + "/initialize/af920bfe-630a-44e9-b2e7-53f092cd0225")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Sql({
		"/sql/truncate.sql",
		"/sql/employeeChecklistIT-filterdata.sql"
	})
	@Test
	void test16_findEmployeeChecklistsBySearchString_employeeFirstName() throws JsonProcessingException {
		final var searchString = "?searchString=and";

		final var response = setupCall()
			.withServicePath(PATH_PREFIX + "/search" + searchString)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<EmployeeChecklistPaginatedResponse>() {});

		assertThat(response.getEmployeeChecklists()).isNotEmpty().allSatisfy(employeeChecklistInfor -> assertThat(employeeChecklistInfor.getEmployeeName()).containsIgnoringCase("and"));
	}

	@Sql({
		"/sql/truncate.sql",
		"/sql/employeeChecklistIT-filterdata.sql"
	})
	@Test
	void test17_findEmployeeChecklistsBySearchString_employeeLastName() throws JsonProcessingException {
		final var searchString = "?searchString=Persson";

		final var response = setupCall()
			.withServicePath(PATH_PREFIX + "/search" + searchString)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<EmployeeChecklistPaginatedResponse>() {});

		assertThat(response.getEmployeeChecklists()).isNotEmpty().allSatisfy(employeeChecklistInfo -> assertThat(employeeChecklistInfo.getEmployeeName()).containsIgnoringCase("Persson"));
	}

	@Sql({
		"/sql/truncate.sql",
		"/sql/employeeChecklistIT-filterdata.sql"
	})
	@Test
	void test18_findEmployeeChecklistsBySearchString_managerFirstName() throws JsonProcessingException {
		final var searchString = "?searchString=alle";

		final var response = setupCall()
			.withServicePath(PATH_PREFIX + "/search" + searchString)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<EmployeeChecklistPaginatedResponse>() {});

		assertThat(response.getEmployeeChecklists()).isNotEmpty().allSatisfy(employeeChecklistInfo -> assertThat(employeeChecklistInfo.getManagerName()).containsIgnoringCase("alle"));
	}

	@Sql({
		"/sql/truncate.sql",
		"/sql/employeeChecklistIT-filterdata.sql"
	})
	@Test
	void test19_findEmployeeChecklistsBySearchString_managerLastName() throws JsonProcessingException {
		final var searchString = "?searchString=llesson";

		final var response = setupCall()
			.withServicePath(PATH_PREFIX + "/search" + searchString)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<EmployeeChecklistPaginatedResponse>() {});

		assertThat(response.getEmployeeChecklists()).isNotEmpty().allSatisfy(employeeChecklistInfo -> assertThat(employeeChecklistInfo.getManagerName()).containsIgnoringCase("llesson"));
	}

	@Sql({
		"/sql/truncate.sql",
		"/sql/employeeChecklistIT-filterdata.sql"
	})
	@Test
	void test20_findEmployeeChecklistsBySearchString_organizationName() throws JsonProcessingException {
		final var searchString = "?searchString=5335";

		final var response = setupCall()
			.withServicePath(PATH_PREFIX + "/search" + searchString)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<EmployeeChecklistPaginatedResponse>() {});

		assertThat(response.getEmployeeChecklists()).isNotEmpty().allSatisfy(employeeChecklistInfo -> assertThat(employeeChecklistInfo.getOrganizationName()).containsIgnoringCase("5335"));
	}

	@Sql({
		"/sql/truncate.sql",
		"/sql/employeeChecklistIT-filterdata.sql"
	})
	@Test
	void test21_findEmployeeChecklistsBySearchString_employeeUserName() throws JsonProcessingException {
		final var searchString = "?searchString=loyee";

		final var response = setupCall()
			.withServicePath(PATH_PREFIX + "/search" + searchString)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<EmployeeChecklistPaginatedResponse>() {});

		assertThat(response.getEmployeeChecklists()).isNotEmpty().allSatisfy(employeeChecklistInfo -> assertThat(employeeChecklistInfo.getEmployeeUsername()).containsIgnoringCase("loyee"));
	}

	@Sql({
		"/sql/truncate.sql",
		"/sql/employeeChecklistIT-filterdata.sql"
	})
	@Test
	void test22_findEmployeeChecklistsBySearchString_delegateFirstName() throws JsonProcessingException {
		final var searchString = "?searchString=ohn";

		final var response = setupCall()
			.withServicePath(PATH_PREFIX + "/search" + searchString)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<EmployeeChecklistPaginatedResponse>() {});

		assertThat(response.getEmployeeChecklists().stream().flatMap(o -> o.getDelegatedTo().stream())).isNotEmpty().allSatisfy(delegateName -> assertThat(delegateName).containsIgnoringCase("ohn"));
	}

	@Sql({
		"/sql/truncate.sql",
		"/sql/employeeChecklistIT-filterdata.sql"
	})
	@Test
	void test23_findEmployeeChecklistsBySearchString_delegateLastName() throws JsonProcessingException {
		final var searchString = "?searchString=doe";

		final var response = setupCall()
			.withServicePath(PATH_PREFIX + "/search" + searchString)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(new TypeReference<EmployeeChecklistPaginatedResponse>() {});

		assertThat(response.getEmployeeChecklists().stream().flatMap(o -> o.getDelegatedTo().stream())).isNotEmpty().allSatisfy(delegateName -> assertThat(delegateName).containsIgnoringCase("doe"));
	}
}
