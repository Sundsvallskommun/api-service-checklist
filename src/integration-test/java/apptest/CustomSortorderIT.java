package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.integration.db.model.SortorderEntity;
import se.sundsvall.checklist.integration.db.model.enums.ComponentType;
import se.sundsvall.checklist.integration.db.repository.SortorderRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/CustomSortorderIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/customSortorderIT-testdata.sql"
})
class CustomSortorderIT extends AbstractAppTest {
	private static final String REQUEST_FILE = "request.json";

	@Autowired
	private SortorderRepository repository;

	@Test
	void test01_saveCustomSortorder() {
		final var organizationNumber = 13;

		assertThat(repository.findAllByMunicipalityIdAndOrganizationNumber("2281", organizationNumber)).hasSize(4).satisfiesExactlyInAnyOrder(
			entity -> assertEntity(entity, "1455a5d4-1db8-4a25-a49f-92fdd0c60a14", ComponentType.PHASE, 1),
			entity -> assertEntity(entity, "2455a5d4-1db8-4a25-a49f-92fdd0c60a14", ComponentType.PHASE, 2),
			entity -> assertEntity(entity, "aba82aca-f841-4257-baec-d745e3ab78bf", ComponentType.TASK, 1),
			entity -> assertEntity(entity, "bba82aca-f841-4257-baec-d745e3ab78bf", ComponentType.TASK, 2));

		setupCall()
			.withServicePath("/2281/sortorder/%s".formatted(organizationNumber))
			.withHttpMethod(PUT)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(ACCEPTED)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(repository.findAllByMunicipalityIdAndOrganizationNumber("2281", organizationNumber)).hasSize(4).satisfiesExactlyInAnyOrder(
			entity -> assertEntity(entity, "1455a5d4-1db8-4a25-a49f-92fdd0c60a14", ComponentType.PHASE, 2),
			entity -> assertEntity(entity, "2455a5d4-1db8-4a25-a49f-92fdd0c60a14", ComponentType.PHASE, 1),
			entity -> assertEntity(entity, "aba82aca-f841-4257-baec-d745e3ab78bf", ComponentType.TASK, 2),
			entity -> assertEntity(entity, "bba82aca-f841-4257-baec-d745e3ab78bf", ComponentType.TASK, 1));
	}

	private void assertEntity(SortorderEntity entity, String componentId, ComponentType componentType, int position) {
		assertThat(entity.getComponentId()).isEqualTo(componentId);
		assertThat(entity.getComponentType()).isEqualTo(componentType);
		assertThat(entity.getPosition()).isEqualTo(position);

	}

	@Test
	@DirtiesContext
	void test02_deleteTask() {
		assertThat(repository.findAllByComponentId("aba82aca-f841-4257-baec-d745e3ab78bf")).isNotEmpty();

		setupCall()
			.withServicePath("/2281/checklists/15764278-50c8-4a19-af00-077bfc314fd2/phases/1455a5d4-1db8-4a25-a49f-92fdd0c60a14/tasks/aba82aca-f841-4257-baec-d745e3ab78bf")
			.withHeader("x-issuer", "username")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(repository.findAllByComponentId("aba82aca-f841-4257-baec-d745e3ab78bf")).isEmpty();
	}

	@Test
	@DirtiesContext
	void test03_deletePhase() {
		assertThat(repository.findAllByComponentId("2455a5d4-1db8-4a25-a49f-92fdd0c60a14")).isNotEmpty();

		setupCall()
			.withServicePath("/2281/phases/2455a5d4-1db8-4a25-a49f-92fdd0c60a14")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(repository.findAllByComponentId("2455a5d4-1db8-4a25-a49f-92fdd0c60a14")).isEmpty();
	}

	@Test
	@DirtiesContext
	void test04_deleteChecklist() {
		assertThat(repository.findAllByComponentId("3455a5d4-1db8-4a25-a49f-92fdd0c60a14")).isNotEmpty();
		assertThat(repository.findAllByComponentId("cba82aca-f841-4257-baec-d745e3ab78bf")).isNotEmpty();

		setupCall()
			.withServicePath("/2281/checklists/25764278-50c8-4a19-af00-077bfc314fd2")
			.withHeader("x-issuer", "username")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(repository.findAllByComponentId("3455a5d4-1db8-4a25-a49f-92fdd0c60a14")).isEmpty();
		assertThat(repository.findAllByComponentId("cba82aca-f841-4257-baec-d745e3ab78bf")).isEmpty();
	}

	@Test
	void test05_deleteOrganization() {
		final var organizationNumber = 578;

		assertThat(repository.findAllByMunicipalityIdAndOrganizationNumber("2281", organizationNumber)).isNotEmpty();

		setupCall()
			.withServicePath("/2281/organizations/047c78a2-aadc-40e5-8913-8623b1fecc35")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(repository.findAllByMunicipalityIdAndOrganizationNumber("2281", organizationNumber)).isEmpty();
	}
}
