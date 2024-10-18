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
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.integration.db.model.OrganizationEntity;
import se.sundsvall.checklist.integration.db.repository.ChecklistRepository;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/OrganizationIT/", classes = Application.class)
@Sql({
	"/sql/truncate.sql",
	"/sql/organizationIT-testdata.sql"
})
class OrganizationIT extends AbstractAppTest {

	private static final String PATH = "/2281/organizations";
	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private ChecklistRepository checklistRepository;

	@Test
	void test1_fetchOrganizations() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

		assertThat(organizationRepository.findAll()).hasSize(4);
	}

	@Test
	void test2_fetchOrganizationById() {
		final var organizationId = "15764278-50c8-4a19-af00-077bfc314fd2";

		setupCall()
			.withServicePath(PATH + "/" + organizationId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

		assertThat(organizationRepository.findById(organizationId)).isPresent();
	}

	@Test
	void test3_createOrganization() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/organizations/(.+)$"))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		final var organizationId = organizationRepository.findAll().stream()
			.filter(entity -> entity.getOrganizationNumber() == 1234)
			.findAny()
			.map(OrganizationEntity::getId)
			.orElseThrow();

		assertThat(organizationRepository.findById(organizationId)).isPresent();
	}

	@Test
	void test4_updateOrganization() {
		final var organizationId = "15764278-50c8-4a19-af00-077bfc314fd2";

		setupCall()
			.withServicePath(PATH + "/" + organizationId)
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

		final var updatedEntity = organizationRepository.findById(organizationId)
			.orElseThrow();

		assertThat(updatedEntity.getOrganizationName()).isEqualTo("NEW NAME");
	}

	@Test
	void test5_deleteOrganization() {
		final var organizationId = "45764278-50c8-4a19-af00-077bfc314fd2";

		assertThat(organizationRepository.existsById(organizationId)).isTrue();
		assertThat(checklistRepository.existsByName("CHECKLIST_SHOULD_BE_DELETED")).isTrue();

		setupCall()
			.withServicePath(PATH + "/" + organizationId)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(organizationRepository.existsById(organizationId)).isFalse();
		assertThat(checklistRepository.existsByName("CHECKLIST_SHOULD_BE_DELETED")).isFalse();
	}

}
