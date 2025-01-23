package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/ChecklistIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/checklistIT-testdata.sql"
})
class ChecklistIT extends AbstractAppTest {

	private static final String CHECKLIST_ID = "15764278-50c8-4a19-af00-077bfc314fd2";

	private static final String PATH = "/2281/checklists";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Autowired
	private ChecklistRepository checklistRepository;

	@Test
	void test1_fetchAllChecklists() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_fetchChecklistById() {
		setupCall()
			.withServicePath(PATH + "/" + CHECKLIST_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_createChecklist() {
		// Create new checklist
		setupCall()
			.withServicePath(PATH)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/checklists/(.+)$"))
			.withExpectedResponseBodyIsNull()
			.sendRequest();

		// Fetch checklists for the organization to verify that new checklist has been added to it
		setupCall()
			.withServicePath("/2281/organizations/59dddb61-9a7b-423f-a873-94049e17cbee")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

	}

	@Test
	void test4_createNewVersion() {
		final var checklistId = "35764278-50c8-4a19-af00-077bfc314fd2";

		// Create new version of checklist
		setupCall()
			.withServicePath(PATH + "/" + checklistId + "/version")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/checklists/(.+)$"))
			.withExpectedResponseBodyIsNull()
			.sendRequest();

		// Fetch checklists for the organization to verify that new checklist has been added to it and is using same sort order
		// as the previous version
		setupCall()
			.withServicePath("/2281/organizations?organizationFilter=3&applySortFor=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_activateChecklist() {
		final var createdChecklistId = "45764278-50c8-4a19-af00-077bfc314fd2";
		setupCall()
			.withServicePath(PATH + "/" + createdChecklistId + "/activate")
			.withHttpMethod(PATCH)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_updateChecklist() {
		setupCall()
			.withServicePath(PATH + "/" + CHECKLIST_ID)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test7_deleteChecklist() {
		assertThat(checklistRepository.existsById(CHECKLIST_ID)).isTrue();

		setupCall()
			.withServicePath(PATH + "/" + CHECKLIST_ID)
			.withHeader("x-userid", "someUser")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(checklistRepository.existsById(CHECKLIST_ID)).isFalse();
	}

	@Test
	void test8_getChecklistEvents() {
		setupCall()
			.withServicePath("/2281/checklists/" + CHECKLIST_ID + "/events?page=0&size=10")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

}
