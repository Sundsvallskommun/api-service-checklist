package se.sundsvall.checklist.integration.db.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum with possible component types in a checklist
 */
@Schema(enumAsRef = true)
public enum ComponentType {
	PHASE,
	TASK;
}
