package apptest;

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
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/ChecklistIT/", classes = Application.class)
@Sql({
	"/sql/truncate.sql",
	"/sql/checklistIT-testdata.sql"
})
class ChecklistIT extends AbstractAppTest {

	private static final String CHECKLIST_ID = "15764278-50c8-4a19-af00-077bfc314fd2";

	private static final String PATH = "/checklists";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

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
		setupCall()
			.withServicePath(PATH)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/checklists/(.+)$"))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_createNewVersion() {
		final var retiredChecklistId = "35764278-50c8-4a19-af00-077bfc314fd2";
		setupCall()
			.withServicePath(PATH + "/" + retiredChecklistId + "/version")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/checklists/(.+)$"))
			.withExpectedResponseBodyIsNull()
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
		setupCall()
			.withServicePath(PATH + "/" + CHECKLIST_ID)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

}
