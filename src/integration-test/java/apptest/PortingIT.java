package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import se.sundsvall.checklist.Application;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;
import se.sundsvall.checklist.integration.db.repository.OrganizationRepository;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/PortingIT/", classes = Application.class)
@Sql({
	"/sql/truncate.sql",
	"/sql/portingIT-testdata.sql"
})
class PortingIT extends AbstractAppTest {

	private static final String REQUEST_FILE = "request.json";
	private static final String EXPECTED_FILE = "expected.json";

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private OrganizationRepository organizationRepository;

	private TransactionTemplate transactionTemplate;

	@BeforeEach
	void setUp() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Test
	void test01_exportLatestVersion() {
		setupCall()
			.withServicePath("/2281/export/1/EMPLOYEE")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_exportExplicitVersion() {
		setupCall()
			.withServicePath("/2281/export/1/EMPLOYEE?version=1")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_importAsNewVersionWhenActiveChecklistExist() {
		// Assert organization before import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(2).orElseThrow();
			assertThat(organization.getChecklists()).hasSize(1).allSatisfy(ch -> {
				assertThat(ch.getVersion()).isEqualTo(1);
				assertThat(ch.getName()).isEqualTo("TEST03_EMPLOYEE_CHECKLIST");
				assertThat(ch.getDisplayName()).isEqualTo("Active checklist for employee");
				assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
				assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.ACTIVE);
			});
		});

		setupCall()
			.withServicePath("/2281/import/add/2/Organization_2")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/checklists/(.+)$"))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		// Assert organization after import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(2).orElseThrow();
			assertThat(organization.getChecklists()).hasSize(2)
				.anySatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(1);
					assertThat(ch.getName()).isEqualTo("TEST03_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Active checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.ACTIVE);
				})
				.anySatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(2);
					assertThat(ch.getName()).isEqualTo("TEST03_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Imported checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.CREATED);
				});
		});
	}

	@Test
	void test04_importAsNewVersionWhenCreatedChecklistExist() {
		// Assert organization before import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(3).orElseThrow();
			assertThat(organization.getChecklists()).hasSize(2)
				.anySatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(1);
					assertThat(ch.getName()).isEqualTo("TEST04_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Active checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.ACTIVE);
				})
				.anySatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(2);
					assertThat(ch.getName()).isEqualTo("TEST04_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Draft checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.CREATED);
				});
		});

		setupCall()
			.withServicePath("/2281/import/add/3/Organization_3")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CONFLICT)
			.withExpectedResponse(EXPECTED_FILE)
			.sendRequestAndVerifyResponse();

		// Assert organization after import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(3).orElseThrow();
			assertThat(organization.getChecklists()).hasSize(2)
				.anySatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(1);
					assertThat(ch.getName()).isEqualTo("TEST04_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Active checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.ACTIVE);
				})
				.anySatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(2);
					assertThat(ch.getName()).isEqualTo("TEST04_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Draft checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.CREATED);
				});
		});
	}

	@Test
	void test05_importAsNewVersionWhenNoChecklistsExists() {
		// Assert organization before import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(4).orElseThrow();
			assertThat(organization.getChecklists()).isEmpty();
		});

		setupCall()
			.withServicePath("/2281/import/add/4/Organization_4")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/checklists/(.+)$"))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		// Assert organization after import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(4).orElseThrow();
			assertThat(organization.getChecklists()).hasSize(1)
				.allSatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(1);
					assertThat(ch.getName()).isEqualTo("TEST05_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Imported checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.CREATED);
				});
		});
	}

	@Test
	void test06_importAndOverwriteExistingActiveChecklist() {
		final var checklistIdWithCreatedStatus = "3880efec-e66e-42a5-a3ac-ea2d08da0e5c";

		// Assert organization before import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(5).orElseThrow();
			assertThat(organization.getChecklists()).hasSize(1)
				.allSatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(1);
					assertThat(ch.getName()).isEqualTo("TEST06_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Active checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.ACTIVE);
					assertThat(ch.getId()).isEqualTo(checklistIdWithCreatedStatus);
				});
		});

		setupCall()
			.withServicePath("/2281/import/replace/5/Organization_5")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/checklists/%s$".formatted(checklistIdWithCreatedStatus))) // No new version should be created, i.e. the id should be intact
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		// Assert organization after import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(5).orElseThrow();
			assertThat(organization.getChecklists()).hasSize(1)
				.allSatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(1);
					assertThat(ch.getName()).isEqualTo("TEST06_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Imported checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.ACTIVE);
					assertThat(ch.getId()).isEqualTo(checklistIdWithCreatedStatus);
				});
		});
	}

	@Test
	void test07_importAndOverwriteExistingCreatedChecklist() {
		final var checklistIdWithCreatedStatus = "2914e06d-5ca5-4ea7-9052-23228be56cca";

		// Assert organization before import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(6).orElseThrow();
			assertThat(organization.getChecklists()).hasSize(2)
				.anySatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(1);
					assertThat(ch.getName()).isEqualTo("TEST07_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Active checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.ACTIVE);
				})
				.anySatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(2);
					assertThat(ch.getName()).isEqualTo("TEST07_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Draft checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.CREATED);
					assertThat(ch.getId()).isEqualTo(checklistIdWithCreatedStatus);
				});
		});

		setupCall()
			.withServicePath("/2281/import/replace/6/Organization_6")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/checklists/%s$".formatted(checklistIdWithCreatedStatus))) // No new version should be created, i.e. the id should be intact
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		// Assert organization after import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(6).orElseThrow();
			assertThat(organization.getChecklists()).hasSize(2)
				.anySatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(1);
					assertThat(ch.getName()).isEqualTo("TEST07_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Active checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.ACTIVE);
				})
				.anySatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(2);
					assertThat(ch.getName()).isEqualTo("TEST07_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Imported checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.CREATED);
					assertThat(ch.getId()).isEqualTo(checklistIdWithCreatedStatus);
				});
		});
	}

	@Test
	void test08_importAndOverwriteWhenNoChecklistsExists() {
		// Assert organization before import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(7).orElseThrow();
			assertThat(organization.getChecklists()).isEmpty();
		});

		setupCall()
			.withServicePath("/2281/import/replace/7/Organization_7")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/checklists/(.+)$"))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		// Assert organization after import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(7).orElseThrow();
			assertThat(organization.getChecklists()).hasSize(1)
				.allSatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(1);
					assertThat(ch.getName()).isEqualTo("TEST08_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Imported checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.CREATED);
				});
		});
	}

	@Test
	void test09_importForNonExistingOrganization() {
		// Assert organization before import
		transactionTemplate.executeWithoutResult(status -> {
			assertThat(organizationRepository.findByOrganizationNumber(8)).isEmpty();
		});

		setupCall()
			.withServicePath("/2281/import/replace/8/Organization_8")
			.withHttpMethod(POST)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/2281/checklists/(.+)$"))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		// Assert organization after import
		transactionTemplate.executeWithoutResult(status -> {
			final var organization = organizationRepository.findByOrganizationNumber(8).orElseThrow();
			assertThat(organization.getOrganizationNumber()).isEqualTo(8);
			assertThat(organization.getOrganizationName()).isEqualTo("Organization_8");
			assertThat(organization.getChecklists()).hasSize(1)
				.allSatisfy(ch -> {
					assertThat(ch.getVersion()).isEqualTo(1);
					assertThat(ch.getName()).isEqualTo("TEST09_EMPLOYEE_CHECKLIST");
					assertThat(ch.getDisplayName()).isEqualTo("Imported checklist for employee");
					assertThat(ch.getRoleType()).isEqualTo(RoleType.EMPLOYEE);
					assertThat(ch.getLifeCycle()).isEqualTo(LifeCycle.CREATED);
				});
		});
	}
}
