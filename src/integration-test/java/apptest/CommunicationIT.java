package apptest;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/CommunicationIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/communicationIT-testdata.sql"
})
class CommunicationIT extends AbstractAppTest {

	private static final String PATH = "/2281/employee-checklists";
	private static final String EXPECTED_FILE = "expected.json";
	private static final String EMPLOYEE_CHECKLIST_ID = "1fb37edc-eb16-4ac3-a436-02971f020b28";

	@Test
	void test1_fetchCorrespondence() {
		setupCall()
			.withServicePath(PATH + "/" + EMPLOYEE_CHECKLIST_ID + "/correspondence")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_sendEmail() {
		setupCall()
			.withServicePath(PATH + "/" + EMPLOYEE_CHECKLIST_ID + "/email")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse();
	}

}
