package se.sundsvall.checklist.integration.db.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.integration.db.model.enums.QuestionType.COMPLETED_OR_NOT_RELEVANT;
import static se.sundsvall.checklist.integration.db.model.enums.QuestionType.COMPLETED_OR_NOT_RELEVANT_WITH_TEXT;
import static se.sundsvall.checklist.integration.db.model.enums.QuestionType.YES_OR_NO;
import static se.sundsvall.checklist.integration.db.model.enums.QuestionType.YES_OR_NO_WITH_TEXT;

import org.junit.jupiter.api.Test;

class QuestionTypeTest {

	@Test
	void enums() {
		assertThat(QuestionType.values()).containsExactlyInAnyOrder(COMPLETED_OR_NOT_RELEVANT, COMPLETED_OR_NOT_RELEVANT_WITH_TEXT, YES_OR_NO, YES_OR_NO_WITH_TEXT);
	}

	@Test
	void enumValues() {
		assertThat(COMPLETED_OR_NOT_RELEVANT).hasToString("COMPLETED_OR_NOT_RELEVANT");
		assertThat(COMPLETED_OR_NOT_RELEVANT_WITH_TEXT).hasToString("COMPLETED_OR_NOT_RELEVANT_WITH_TEXT");
		assertThat(YES_OR_NO).hasToString("YES_OR_NO");
		assertThat(YES_OR_NO_WITH_TEXT).hasToString("YES_OR_NO_WITH_TEXT");
	}
}
