package se.sundsvall.checklist.service.mapper;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.Problem;

import generated.se.sundsvall.employee.PortalPersonData;
import se.sundsvall.checklist.integration.db.model.DelegateEntity;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;

public final class DelegateMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(DelegateMapper.class);

	private DelegateMapper() {}

	// -----------------------------
	// Entity mappings
	// -----------------------------

	public static DelegateEntity toDelegateEntity(final PortalPersonData portalPersonData, final EmployeeChecklistEntity employeeChecklist) {
		return DelegateEntity.builder()
			.withPartyId(portalPersonData.getPersonid().toString())
			.withEmail(portalPersonData.getEmail())
			.withUserName(getUserNameFromLoginName(portalPersonData.getLoginName()))
			.withFirstName(portalPersonData.getGivenname())
			.withLastName(portalPersonData.getLastname())
			.withEmployeeChecklist(employeeChecklist)
			.withDelegatedBy(employeeChecklist.getEmployee().getManager())
			.build();
	}

	static String getUserNameFromLoginName(final String loginName) {
		if (StringUtils.isNotBlank(loginName) && loginName.contains("\\")) {
			return loginName.substring(loginName.indexOf("\\") + 1);
		}
		LOGGER.error("Couldn't parse username from loginName: {}", loginName);
		throw Problem.builder()
			.withTitle("Couldn't parse username from loginName: " + loginName)
			.withStatus(INTERNAL_SERVER_ERROR)
			.build();
	}
}
