package se.sundsvall.checklist.integration.db.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum CorrespondenceStatus {
	SENT,
	NOT_SENT,
	ERROR,
	WILL_NOT_SEND
}
