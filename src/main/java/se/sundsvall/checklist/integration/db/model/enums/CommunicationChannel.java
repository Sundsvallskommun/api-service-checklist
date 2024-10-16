package se.sundsvall.checklist.integration.db.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Communication channel initially only EMAIL, might expand on this later.
 */
@Schema(enumAsRef = true)
public enum CommunicationChannel {
	EMAIL,
	NO_COMMUNICATION
}
