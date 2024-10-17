package se.sundsvall.checklist.integration.db.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum QuestionType {
	YES_OR_NO,
	YES_OR_NO_WITH_TEXT,
	COMPLETED_OR_NOT_RELEVANT,
	COMPLETED_OR_NOT_RELEVANT_WITH_TEXT
}
