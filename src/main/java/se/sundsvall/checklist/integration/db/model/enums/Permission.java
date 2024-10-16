package se.sundsvall.checklist.integration.db.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum Permission {
	SUPERADMIN,
	ADMIN
}
