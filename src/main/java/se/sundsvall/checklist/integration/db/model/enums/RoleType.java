package se.sundsvall.checklist.integration.db.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum with possible roles in a checklist
 */
@Schema(enumAsRef = true)
public enum RoleType {
	NEW_EMPLOYEE,
	NEW_MANAGER,
	MANAGER_FOR_NEW_EMPLOYEE,
	MANAGER_FOR_NEW_MANAGER;
}
