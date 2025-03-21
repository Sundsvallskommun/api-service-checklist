package apptest;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/DelegationIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/delegationIT-testdata.sql"
})
class DelegationIT extends AbstractAppTest {

	private static final String EMPLOYEE_CHECKLIST_ID = "1fb37edc-eb16-4ac3-a436-02971f020b28";
	private static final String EMAIL = "test@test.com";
	private static final String USERNAME = "username";
	private static final String PATH = "/2281/employee-checklists/";
	private static final String EXPECTED_FILE = "expected.json";

	@Test
	void test01_delegateEmployeeChecklist() {
		setupCall()
			.withServicePath(PATH + EMPLOYEE_CHECKLIST_ID + "/delegate-to/" + EMAIL)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_fetchDelegatedEmployeeChecklists() {
		setupCall()
			.withServicePath(PATH + "delegated-to/" + USERNAME)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_removeEmployeeChecklistDelegation() {
		setupCall()
			.withServicePath(PATH + EMPLOYEE_CHECKLIST_ID + "/delegated-to/" + EMAIL)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_delegateEmployeeChecklistWithExistingDelegation() {
		setupCall()
			.withServicePath(PATH + EMPLOYEE_CHECKLIST_ID + "/delegate-to/test@test5.com")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CONFLICT)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}
}
