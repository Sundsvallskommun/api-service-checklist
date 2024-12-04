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
	"/db/scripts/truncate.sql",
	"/db/scripts/organizationIT-testdata.sql"
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
	void test01_fetchOrganizationsWithoutApplyingSortorder() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

		assertThat(organizationRepository.findAllByMunicipalityId("2281")).hasSize(5);
	}

	@Test
	void test02_fetchOrganizationById() {
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
	void test03_createOrganization() {
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
	void test04_updateOrganization() {
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
	void test05_fetchOrganizationsWithFilters() {
		setupCall()
			.withServicePath(PATH + "?organizationFilter=1&organizationFilter=11&organizationFilter=111")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_fetchOrganizationsWithFiltersAndSpecificSortorder() {
		setupCall()
			.withServicePath(PATH + "?organizationFilter=1&organizationFilter=11&organizationFilter=111&applySortFor=111")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_fetchOrganizationsWithFiltersAndInheritedSortorder() {
		setupCall()
			.withServicePath(PATH + "?organizationFilter=1&organizationFilter=11&organizationFilter=112&applySortFor=112") // 112 doesn't have own sortorder and should inherit from closest parent (11)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_deleteOrganization() {
		final var organizationId = "65764278-50c8-4a19-af00-077bfc314fd2";

		assertThat(organizationRepository.existsById(organizationId)).isTrue();
		assertThat(checklistRepository.existsByNameAndMunicipalityId("Retired checklist for root organization B", "2262")).isTrue();

		setupCall()
			.withServicePath("/2262/organizations/" + organizationId)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(organizationRepository.existsById(organizationId)).isFalse();
		assertThat(checklistRepository.existsByNameAndMunicipalityId("Retired checklist for root organization B", "2262")).isFalse();
	}

}
