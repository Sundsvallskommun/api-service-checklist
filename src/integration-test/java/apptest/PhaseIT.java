package apptest;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/PhaseIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/phaseIT-testdata.sql"
})
class PhaseIT extends AbstractAppTest {

	private static final String PHASE_ID = "28f2b2cc-1fc8-42ee-a752-fae751c1a858";
	private static final String PATH = "/2281/phases";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Test
	void test1_fetchPhases() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_fetchPhase() {
		setupCall()
			.withServicePath(PATH + "/" + PHASE_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_createPhase() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/phases/(.+)$"))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_updatePhase() {
		setupCall()
			.withServicePath(PATH + "/" + PHASE_ID)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_deletePhaseWithTasks() {
		setupCall()
			.withServicePath(PATH + "/" + PHASE_ID)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(CONFLICT)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_deletePhaseWithoutTasks() {
		setupCall()
			.withServicePath("/2282/phases/38f2b2cc-1fc8-42ee-a752-fae751c1a858")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}
}
