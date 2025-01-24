package se.sundsvall.checklist.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for an event")
public class Event {

	private String logKey;

	private String eventType;

	private String municipalityId;

	private String message;

	private String owner;

	private String historyReference;

	private String sourceType;

	private OffsetDateTime created;

	private OffsetDateTime expires;

	private List<Metadata> metadata;
}
